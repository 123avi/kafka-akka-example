package com.example

import java.util

import org.apache.kafka.common.serialization.{Deserializer, Serializer, StringDeserializer, StringSerializer}
import play.api.libs.json.{Format, Json, Reads, Writes}

object PingPongProtocol{
  case class PingPongMessage(text: String)
  implicit val PingPongMsgFrmt: Format[PingPongMessage] = Json.format[PingPongMessage]
}

class JsonDeserializer[A: Reads] extends Deserializer[A] {

  private val stringDeserializer = new StringDeserializer

  override def configure(configs: util.Map[String, _], isKey: Boolean) =
    stringDeserializer.configure(configs, isKey)

  override def deserialize(topic: String, data: Array[Byte]) =
    Json.parse(stringDeserializer.deserialize(topic, data)).as[A]

  override def close() =
    stringDeserializer.close()

}

class JsonSerializer[A: Writes] extends Serializer[A] {

  private val stringSerializer = new StringSerializer

  override def configure(configs: util.Map[String, _], isKey: Boolean) =
    stringSerializer.configure(configs, isKey)

  override def serialize(topic: String, data: A) =
    stringSerializer.serialize(topic, Json.stringify(Json.toJson(data)))

  override def close() =
    stringSerializer.close()

}