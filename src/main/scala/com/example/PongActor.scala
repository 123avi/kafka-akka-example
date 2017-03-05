package com.example

import akka.actor.{Actor, ActorLogging, Props}
import cakesolutions.kafka.akka.KafkaConsumerActor.{Confirm, Unsubscribe}
import com.typesafe.config.Config

class PongActor(val config: Config) extends Actor
  with ActorLogging  with PingPongConsumer with PingPongProducer{
  import PingPongProtocol._
  import PongActor._

  override def postStop() = {
    kafkaConsumerActor ! Unsubscribe
    super.postStop()
  }

  override def preStart(): Unit = {
    super.preStart()
    subscribe(topics)
  }


  def receive = {
  	case Start =>
  	  log.info("In PongActor - received start message - let the games begin")
  	  submitMsg(PingActor.topics,PingPongMessage("ping"))
      context.become(playingPingPong)
  }

  def playingPingPong: Receive ={
    case pongExtractor(consumerRecords) =>
      consumerRecords.pairs.foreach {
        case (None, msg) =>
          log.error(s"Received unkeyed submit sample command: $msg")
        case (Some(id), pongMessage) =>
          log.debug("PING")
          println("PING")

          submitMsg(PingActor.topics, PingPongMessage("ping"))
          // By committing *after* processing we get at-least-once-processing, but that's OK here because we can identify duplicates by their timestamps
          kafkaConsumerActor ! Confirm(consumerRecords.offsets)
          log.info(s"In PongActor - id:$id, msg: $pongMessage, offsets ${consumerRecords.offsets}")
      }

    case unknown =>
      log.error(s"PongActor got Unknown message: $unknown")
  }
}

object PongActor {
  def props(config: Config) = Props(new PongActor(config))
  val topics = List("pong")
  case object Start

}
