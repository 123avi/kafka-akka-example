package com.example.smaple

import akka.testkit.TestKit
import cakesolutions.kafka.testkit.KafkaServer
import org.scalatest.{BeforeAndAfterAll, FlatSpecLike, Matchers, Suite}
/*
Source : https://github.com/cakesolutions/scala-kafka-client/wiki/Scala-Kafka-Client
 */


trait KafkaIntSpec
  extends FlatSpecLike
  with Matchers
  with BeforeAndAfterAll {
  this: TestKit with Suite =>


  val kafkaServer = new KafkaServer()
  val kafkaPort = kafkaServer.kafkaPort

  override def beforeAll() = {
    kafkaServer.startup()
  }

  override def afterAll() = {
    kafkaServer.close()
    TestKit.shutdownActorSystem(system)
  }
}
