package com.example

import akka.actor.ActorSystem
import com.typesafe.config.{Config, ConfigFactory}
import cakesolutions.kafka.testkit.KafkaServer

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.Random

object ApplicationMain extends App {
  val system = ActorSystem("MyActorSystem")
  private val kafkaServer = new KafkaServer()
  private def randomString: String = Random.alphanumeric.take(5).mkString("")


  val config: Config = ConfigFactory.parseString(
    s"""
       | bootstrap.servers = "localhost:${kafkaServer.kafkaPort}",
       | group.id = "$randomString"
       | enable.auto.commit = false
       | auto.offset.reset = "earliest"
       | schedule.interval = 1 second
       | unconfirmed.timeout = 3 seconds
       | max.redeliveries = 3
        """.stripMargin
  )

  val pongActor = system.actorOf(PongActor.props(config), "pingActor")
  pongActor ! PongActor.Start
//   This example app will ping pong 3 times and thereafter terminate the ActorSystem -
//   see counter logic in PingActor
  Await.ready(system.whenTerminated, 10 seconds)
}