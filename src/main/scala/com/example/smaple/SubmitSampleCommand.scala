package com.example.smaple

import java.util.UUID

import play.api.libs.functional.syntax._
import play.api.libs.json.JsPath
import play.api.libs.json.Reads._

/*
Source : https://github.com/cakesolutions/scala-kafka-client/wiki/Scala-Kafka-Client
 */

case class SubmitSampleCommand(timestamp: Long, power: Double) {
  require(timestamp > 0)
}

object SubmitSampleCommand {

  implicit val SubmitSampleCommandFormat = (
    (JsPath \ "timestamp").format[Long](min(0L)) and
      (JsPath \ "power").format[Double]
    ) (SubmitSampleCommand.apply, unlift(SubmitSampleCommand.unapply))

}

case class MeterId(id: UUID)

object MeterId {

  def generate = MeterId(UUID.randomUUID)

}