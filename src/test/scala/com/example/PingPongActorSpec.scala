package com.example

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import cakesolutions.kafka.{KafkaConsumer, KafkaProducer, KafkaProducerRecord}
import cakesolutions.kafka.testkit.KafkaServer
import com.typesafe.config.{Config, ConfigFactory}
import org.apache.kafka.common.serialization.{StringDeserializer, StringSerializer}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.duration._
import scala.util.Random

class PingPongActorSpec(_system: ActorSystem) extends TestKit(_system) with ImplicitSender
  with WordSpecLike with Matchers with BeforeAndAfterAll {
  import PingPongProtocol._

  def this() = this(ActorSystem("MySpec"))

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

  override def beforeAll() = kafkaServer.startup()

  override def afterAll {
    kafkaServer.close()
//    TestKit.shutdownActorSystem(system)
  }

    val pongActor = system.actorOf(PongActor.props(config), "PongTest")

  val pingActor = system.actorOf(PingActor.props(config, testActor), "PingTest")

  def kafkaProducer(kafkaHost: String, kafkaPort: Int): KafkaProducer[String, PingPongMessage] =
    KafkaProducer(KafkaProducer.Conf(new StringSerializer(), new JsonSerializer[PingPongMessage], bootstrapServers = kafkaHost + ":" + kafkaPort))

  "A Ping actor" must {
    "send back a ping on a pong" in {
      Thread.sleep(5000)
      pongActor ! PongActor.Start
      expectMsg( 20 seconds, PingActor.GameOver)
    }
  }



}
