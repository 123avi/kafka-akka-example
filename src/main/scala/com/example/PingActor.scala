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
          counter += 1
          if (counter > 3) {
            log.info(s"${self.path.name} is ending the game")
            submitMsg(PongActor.topics, PingPongMessage("GameOver"))
            self ! PoisonPill
          } else {
            submitMsg(PongActor.topics, PingPongMessage("pong"))
          }
          kafkaConsumerActor ! Confirm(consumerRecords.offsets)
          log.info(s"In PingActor - id:$id, msg: $pongMessage, offsets ${consumerRecords.offsets}")
      }

    case unknown =>
      log.error(s"got Unknown message: $unknown")
  }
}

object PingActor {
  def props(config: Config) = Props( new PingActor(config))
  val topics = List("ping")
}