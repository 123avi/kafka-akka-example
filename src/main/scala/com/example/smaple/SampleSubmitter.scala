package com.example.smaple

import cakesolutions.kafka.{KafkaProducer, KafkaProducerRecord}
import com.example.JsonSerializer
import com.typesafe.config.Config
import org.apache.kafka.common.serialization.StringSerializer

class SampleSubmitter(config: Config) {

  private val producer = KafkaProducer(
    KafkaProducer.Conf(
      config,
      keySerializer = new StringSerializer,
      valueSerializer = new JsonSerializer[SubmitSampleCommand])
  )

  private val topic = config.getString("topic")

  def submitSample(meterId: MeterId, submitSampleCommand: SubmitSampleCommand) = producer.send(
    KafkaProducerRecord(topic, meterId.id.toString, submitSampleCommand)
  )

  def close() = producer.close()

}