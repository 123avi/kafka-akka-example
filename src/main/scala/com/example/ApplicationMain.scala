package com.example

import akka.actor.ActorSystem
import com.typesafe.config.{Config, ConfigFactory}

import scala.util.Random

object ApplicationMain extends App {
  val system = ActorSystem("MyActorSystem")
  private def randomString: String = Random.alphanumeric.take(5).mkString("")


  val config: Config = ConfigFactory.parseString(
    s"""
       | bootstrap.servers = "localhost:9092",
       | group.id = "$randomString"
       | enable.auto.commit = false
       | auto.offset.reset = "latest"
       | schedule.interval = 1 second
       | unconfirmed.timeout = 3 seconds
       | max.redeliveries = 3
        """.stripMargin
  )
//
//  val config: Config = ConfigFactory.parseString(
//    s"""
//       | bootstrap.servers = "localhost:2181",
//       | group.id = "$randomString"
//       | auto.offset.reset = "earliest"
//        """.stripMargin
//  )



  val pongActor = system.actorOf(PongActor.props(config), "pongActor")
  val pingActor = system.actorOf(PingActor.props(config), "PingActor")
Thread.sleep(5000)
  pongActor ! PongActor.Start
  //   This example app will ping pong 3 times and thereafter terminate the ActorSystem -
  //   see counter logic in PingActor
//  Await.ready(system.whenTerminated, 10 seconds)
}