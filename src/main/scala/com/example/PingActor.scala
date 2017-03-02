package com.example

import akka.actor.{Actor, ActorLogging, ActorRef, PoisonPill, Props}
import cakesolutions.kafka.akka.KafkaConsumerActor.{Confirm, Unsubscribe}
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

  override def postStop() = {
    kafkaConsumerActor ! Unsubscribe
    super.postStop()
  }

  def receive = playingPingPong

  def playingPingPong: Receive = {

    case pongExtractor(consumerRecords) =>

      consumerRecords.pairs.foreach {
        case (None, submitSampleCommand) =>
          log.error(s"Received unkeyed submit sample command: $submitSampleCommand")

        case (Some(id), pongMessage) =>
          counter += 1
          if (counter > 3) {
            log.info(s"========> Sending poison pill to ${self.path.name}")
            self ! PoisonPill
          } else {
            submitMsg(PongActor.topics, PingPongMessage("pong"))
          }

          // By committing *after* processing we get at-least-once-processing, but that's OK here because we can identify duplicates by their timestamps
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
  case object GameOver
}