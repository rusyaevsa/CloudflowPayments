package com.payments.http

import spray.json._
import com.payments.datamodel._

object ParticipantJson extends DefaultJsonProtocol {

  implicit object ParticipantInfoJson extends RootJsonFormat[Participant] {
    override def write(obj: Participant): JsValue =
      JsObject(
        "participantID" -> JsString(obj.participantID),
        "balance"       -> JsNumber(obj.balance),
      )

    override def read(json: JsValue): Participant =
      json.asJsObject.getFields("participantID", "balance") match {
        case Seq(JsString(id), JsNumber(balance)) =>
          println(balance)
          println(id)
          Participant(id, balance.toFloat)
      }
  }
}

