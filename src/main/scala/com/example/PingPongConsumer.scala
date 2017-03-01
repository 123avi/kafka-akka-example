package com.example

import akka.actor.Actor
import cakesolutions.kafka.KafkaConsumer.Conf
import cakesolutions.kafka.akka.KafkaConsumerActor.Subscribe
import cakesolutions.kafka.akka.{ConsumerRecords, KafkaConsumerActor, KafkaProducerActor, ProducerRecords}
import cakesolutions.kafka.{KafkaConsumer, KafkaProducer, KafkaProducerRecord}
import com.example.PingPongProtocol.PingPongMessage
import com.typesafe.config.Config
import org.apache.kafka.common.serialization.{StringDeserializer, StringSerializer}

import scala.util.Random

trait KafkaConfig{
  def config:Config
  def randomString(len: Int= 5): String = Random.alphanumeric.take(len).mkString("")
}

trait PingPongConsumer extends KafkaConfig{
  this: Actor =>

  val pongExtractor = ConsumerRecords.extractor[java.lang.String, PingPongMessage]

  val kafkaConfig: Conf[String, PingPongMessage] = KafkaConsumer.Conf(
    config,
    keyDeserializer = new StringDeserializer(),
    valueDeserializer = new JsonDeserializer[PingPongMessage]
  )

  val kafkaConsumerActor = context.actorOf(
    KafkaConsumerActor.props(config,new StringDeserializer(), new JsonDeserializer[PingPongMessage], self),
    "PingKafkaConsumerActor"
  )

  def subscribe(topics: List[String]) =
    kafkaConsumerActor ! Subscribe.AutoPartition(topics)

}

trait PingPongProducer  extends KafkaConfig{
  this: Actor =>

  val kafkaProducerConf = KafkaProducer.Conf(
    bootstrapServers = config.getString("bootstrap.servers"),
    keySerializer = new StringSerializer(),
    valueSerializer = new JsonSerializer[PingPongMessage])


  val kafkaProducerActor = context.actorOf(KafkaProducerActor.props( kafkaProducerConf))

  def submitMsg(topics: List[String], msg: PingPongMessage) =
      topics.foreach(topic => kafkaProducerActor ! ProducerRecords(List(KafkaProducerRecord(topic, randomString(3) ,msg))))
}
