package com.example

import akka.actor.{Actor, ActorLogging, PoisonPill, Props}
import cakesolutions.kafka.akka.KafkaConsumerActor.Confirm
import com.typesafe.config.Config

class PongActor(val config: Config) extends Actor
  with ActorLogging  with PingPongConsumer with PingPongProducer{
  import PingPongProtocol._
  import PongActor._

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
    case msgExtractor(consumerRecords) =>
      consumerRecords.pairs.foreach {
        case (_, PingPongMessage("GameOver")) =>
          kafkaConsumerActor ! Confirm(consumerRecords.offsets)
          log.info(s"Bye Bye ${self.path.name}")
          self ! PoisonPill
        case (None, msg) =>
          log.error(s"Received unkeyed submit sample command: $msg")

        case (Some(id), pongMessage) =>
          kafkaConsumerActor ! Confirm(consumerRecords.offsets)
          log.info(s"In PongActor - id:$id, msg: $pongMessage, offsets ${consumerRecords.offsets}")
          println(pongMessage.text)
          submitMsg(PingActor.topics, PingPongMessage("ping"))
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
