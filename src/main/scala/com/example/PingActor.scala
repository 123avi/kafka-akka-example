package com.example

import akka.actor.{Actor, ActorLogging, PoisonPill, Props}
import cakesolutions.kafka.akka.KafkaConsumerActor.Confirm
import com.typesafe.config.Config

class PingActor(val config: Config) extends Actor
  with ActorLogging with PingPongConsumer with PingPongProducer{
  import PingActor._
  import PingPongProtocol._


  var counter = 0

  override def preStart() = {
    super.preStart()
    subscribe(topics)
  }

  def receive = playingPingPong

  def playingPingPong: Receive = {

    case msgExtractor(consumerRecords) =>

      consumerRecords.pairs.foreach {
        case (None, pongMessage) =>
          log.error(s"Received unkeyed message: $pongMessage")

        case (Some(id), pongMessage) =>
          println(pongMessage.text)
          kafkaConsumerActor ! Confirm(consumerRecords.offsets)
          log.info(s"In PingActor - id:$id, msg: $pongMessage, counter:$counter, offsets ${consumerRecords.offsets}")
          counter += 1
          if (counter >= 3) {
            log.info(s"${self.path.name} is ending the game")
            submitMsg(PongActor.topics, PingPongMessage("GameOver"))
            // DO NOT do this in production, this is just to make sure that our kafkaConsumerActor is not terminated before placing the game over message
            Thread.sleep(1000)
            self ! PoisonPill
          } else {
            submitMsg(PongActor.topics, PingPongMessage("PONG"))
          }
      }

    case unknown =>
      log.error(s"got Unknown message: $unknown")
  }
}

object PingActor {
  def props(config: Config) = Props( new PingActor(config))
  val topics = List("ping")
}