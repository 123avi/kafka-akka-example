package com.example

import akka.actor.{ActorSystem, Terminated}
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import cakesolutions.kafka.testkit.KafkaServer
import cakesolutions.kafka.{KafkaProducer, KafkaProducerRecord}
import com.example.PongActor.Start
import com.typesafe.config.{Config, ConfigFactory}
import org.apache.kafka.common.serialization.{StringDeserializer, StringSerializer}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.duration._
import scala.util.Random

class PingPongActorSpec(_system: ActorSystem) extends TestKit(_system) with ImplicitSender
  with WordSpecLike with Matchers with BeforeAndAfterAll {
  import PingPongProtocol._

  def this() = this(ActorSystem("MySpec"))

  val kafkaServer = new KafkaServer()
  def randomString: String = Random.alphanumeric.take(5).mkString("")


  val config: Config = ConfigFactory.parseString(
    s"""
       | bootstrap.servers = "localhost:${kafkaServer.kafkaPort}",
       | auto.offset.reset = "earliest",
       | group.id = "$randomString"
        """.stripMargin
  )

  override def beforeAll() = {

    kafkaServer.startup()
  }


  override def afterAll() = {
    TestKit.shutdownActorSystem(system)
    producer.close()
    kafkaServer.close()
  }
  val keySerializer =   new StringSerializer()
  val valueSerializer =  new JsonSerializer[PingPongMessage]
  val keyDeserializer = new StringDeserializer()
  val valueDeserializer = new JsonDeserializer[PingPongMessage]


  val producer =
    KafkaProducer(KafkaProducer.Conf(keySerializer, valueSerializer, bootstrapServers = s"localhost:${kafkaServer.kafkaPort}"))

  def submitMsg(times: Int, topic: String, msg: PingPongMessage) = {
    for(i <- 1 to times) {
      producer.send(KafkaProducerRecord(topic, randomString, msg))
      producer.flush()
    }
  }

  def kafkaProducer(kafkaHost: String, kafkaPort: Int): KafkaProducer[String, PingPongMessage] =
    KafkaProducer(KafkaProducer.Conf(new StringSerializer(), new JsonSerializer[PingPongMessage], bootstrapServers = kafkaHost + ":" + kafkaPort))

  "A Ping Pong Game " must {
    "terminate after 3 ping messages" in {
      val pongActor = system.actorOf(PongActor.props(config), "PongTest")
      val pingActor = system.actorOf(PingActor.props(config), "PingTest")
      val tester = TestProbe()
      tester.watch(pingActor)
      tester.watch(pongActor)
      pongActor ! PongActor.Start
      tester.expectMsgAllOf(10 seconds, Terminated(pongActor)(true, true), Terminated(pingActor)(true, true))
    }
  }

  "A Ping actor" must {
    "terminate after 3 messages" in {
      val pingActor = system.actorOf(PingActor.props(config), "PingTest")
      val tester = TestProbe()
      tester.watch(pingActor)
      submitMsg(3, PingActor.topics.head, PingPongMessage("PING"))
      tester.expectTerminated(pingActor, 10 seconds)
    }
    "should place 2 Pong messages and GameOver on Pong topic message after 3 PingPong messages" in {
      val pingActor = system.actorOf(PingActor.props(config), "PingTest")
      submitMsg(3, PingActor.topics.head, PingPongMessage("PING"))
      val tester = TestProbe()
      tester.watch(pingActor)
      tester.expectTerminated(pingActor, 10 seconds)
      val results: Seq[PingPongMessage] = kafkaServer.consume(PongActor.topics.head, 3, 2000, keyDeserializer, valueDeserializer).map(_._2)
      results.take(2) should contain theSameElementsAs Seq.fill(2)(PingPongMessage("PONG"))
      results.last shouldEqual PingPongMessage("GameOver")
    }
  }

  "A Pong actor" must {
    "terminate getting GameOver message" in {
      val pongActor = system.actorOf(PongActor.props(config), "PongTest")
      pongActor ! Start
      val tester = TestProbe()
      tester.watch(pongActor)
      tester.expectTerminated(pongActor, 10 seconds)
      submitMsg(3, PongActor.topics.head, PingPongMessage("PONG"))
      submitMsg(1, PongActor.topics.head, PingPongMessage("GameOver"))
    }
  }
}
