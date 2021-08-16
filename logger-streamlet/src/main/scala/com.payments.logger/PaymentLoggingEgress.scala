package com.payments.logger

import akka.kafka.ConsumerMessage
import akka.stream.scaladsl.RunnableGraph
import cloudflow.akkastream.scaladsl.{FlowWithCommittableContext, RunnableGraphStreamletLogic}
import cloudflow.akkastream._
import cloudflow.streamlets._
import cloudflow.streamlets.avro._
import com.payments.datamodel._

class PaymentLoggingEgress extends AkkaStreamlet{
  val in: AvroInlet[LogMessage] = AvroInlet[LogMessage]("in")

  override def shape(): StreamletShape = StreamletShape.withInlets(in)

  override protected def createLogic(): AkkaStreamletLogic = new RunnableGraphStreamletLogic() {

    def flow: FlowWithCommittableContext[LogMessage, LogMessage]#Repr[Unit, ConsumerMessage.Committable] = {
      FlowWithCommittableContext[LogMessage].map {
        case LogMessage("INFO", msg) => system.log.info(s"[INFO] - $msg")
        case LogMessage("WARN", msg) => system.log.warning(s"[WARN] - $msg")
        case _ => log.error("Uncorrect message!")
      }
    }

    def runnableGraph: RunnableGraph[_] = sourceWithCommittableContext(in).via(flow).to(committableSink)
  }
}
