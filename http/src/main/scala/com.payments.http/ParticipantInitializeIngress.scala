package com.payments.http

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import cloudflow.akkastream._
import cloudflow.streamlets._
import cloudflow.streamlets.avro._
import cloudflow.akkastream.util.scaladsl._
import com.payments.datamodel._
import ParticipantJson._
import akka.http.scaladsl.common.{ EntityStreamingSupport, JsonEntityStreamingSupport }

class ParticipantInitializeIngress extends AkkaServerStreamlet {
  val out: AvroOutlet[Participant] = AvroOutlet[Participant]("out").withPartitioner(RoundRobinPartitioner)
  def shape: StreamletShape        = StreamletShape.withOutlets(out)

  implicit val entityStreamingSupport: JsonEntityStreamingSupport = EntityStreamingSupport.json()
  override def createLogic: HttpServerLogic                       = HttpServerLogic.defaultStreaming(this, out)
}