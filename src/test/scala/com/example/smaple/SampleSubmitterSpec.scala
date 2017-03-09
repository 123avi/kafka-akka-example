package com.example.smaple

import cakesolutions.kafka.KafkaConsumer
import cakesolutions.kafka.testkit.KafkaServer
import com.typesafe.config.{Config, ConfigFactory}
import org.apache.kafka.common.serialization.StringDeserializer
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpec}
import play.api.libs.json.Json

import scala.collection.JavaConverters._
import scala.concurrent.duration._
import scala.util.Random
/*
Source : https://github.com/cakesolutions/scala-kafka-client/wiki/Scala-Kafka-Client
 */

class SampleSubmitterSpec extends WordSpec with Matchers with BeforeAndAfterAll {

  private val kafkaServer = new KafkaServer

  private val baseConfig = ConfigFactory.parseString(
    s"""
       |{
       |  bootstrap.servers = "localhost:${kafkaServer.kafkaPort}"
       |}
       """.stripMargin)

  private val topic = "samples"

  val consumerConf: Config =  ConfigFactory.parseString(
    s"""
       |{
       |  topics = ["$topic"]
       |  group.id = "testing-abcde"
       |  auto.offset.reset = "earliest"
       |}
          """.stripMargin).withFallback(baseConfig)

  private val consumer = KafkaConsumer(KafkaConsumer.Conf(
   consumerConf,
    keyDeserializer = new StringDeserializer(),
    valueDeserializer = new StringDeserializer()
  ))

  override def beforeAll() = kafkaServer.startup()

  override def afterAll() = kafkaServer.close()

  "A Sample Submitter" must {
    "forward commands to Kafka" in {
      consumer.subscribe(List(topic).asJava)

      val sampleSubmitter = new SampleSubmitter(
        ConfigFactory.parseString(
          s"""
             |{
             |  topic = "$topic"
             |}
             """.stripMargin).withFallback(baseConfig)
      )

      val meterId = MeterId.generate
      val submitSampleCommand = SubmitSampleCommand(
        timestamp = System.currentTimeMillis,
        power = {
          val bedrooms = Random.nextInt(4) + 1
          Math.random * bedrooms * 1000
        }
      )
      sampleSubmitter.submitSample(
        meterId,
        submitSampleCommand
      )

      val records = consumer.poll(30.seconds.toMillis)
      records.count shouldBe 1
      val record = records.asScala.toList.head
      record.key shouldBe meterId.id.toString
      record.value shouldBe Json.stringify(Json.toJson(submitSampleCommand))

      sampleSubmitter.close()

      consumer.close()
    }
  }

}