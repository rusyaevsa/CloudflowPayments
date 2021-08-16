package com.payments.checker

import cloudflow.flink._
import cloudflow.streamlets.{StreamletShape, StringConfigParameter}
import cloudflow.streamlets.avro.{AvroInlet, AvroOutlet}
import org.apache.flink.api.scala._
import org.apache.flink.streaming.api.scala.OutputTag

class PaymentCheckingStreamlet extends FlinkStreamlet{
  @transient val in: AvroInlet[PaymentString] = AvroInlet[PaymentString]("in")
  @transient val outProcessing: AvroOutlet[Payment] = AvroOutlet[Payment]("outProcessing")
  @transient val outLogger: AvroOutlet[LogMessage] = AvroOutlet[LogMessage]("outLogger")

  @transient val maskConf: StringConfigParameter = StringConfigParameter("mask")
  override def configParameters = Vector(maskConf)

  override def shape(): StreamletShape = StreamletShape.withInlets(in).withOutlets(outLogger, outProcessing)

  override protected def createLogic(): FlinkStreamletLogic = new FlinkStreamletLogic() {
    override def buildExecutionGraph() = {
      val outputLogger  = OutputTag[LogMessage](outLogger.name)
      val mask = maskConf.value
      readStream(in).print()
    }
  }
}
