package com.payments.checker

import cloudflow.flink._
import cloudflow.streamlets.{StreamletShape, StringConfigParameter}
import cloudflow.streamlets.avro.{AvroInlet, AvroOutlet}
import org.apache.flink.api.scala._
import org.apache.flink.streaming.api.scala.OutputTag

import com.payments.datamodel._

class PaymentCheckingStreamlet extends FlinkStreamlet{
  @transient val in: AvroInlet[PaymentString] = AvroInlet[PaymentString]("in")
  @transient val outProcessing: AvroOutlet[Payment] = AvroOutlet[Payment]("out-processing")
  @transient val outLogger: AvroOutlet[LogMessage] = AvroOutlet[LogMessage]("out-logger")

  @transient val maskConf: StringConfigParameter = StringConfigParameter("mask")
  override def configParameters = Vector(maskConf)

  override def shape(): StreamletShape = StreamletShape.withInlets(in).withOutlets(outLogger, outProcessing)

  override protected def createLogic(): FlinkStreamletLogic = new FlinkStreamletLogic() {
    override def buildExecutionGraph(): Unit = {
      val outputLogger  = OutputTag[LogMessage](outLogger.name)
      val mask = maskConf.value.r
      val stream = readStream(in).process(new MaskChecker(mask, outputLogger))

      writeStream(outProcessing, stream)
      writeStream(outLogger, stream.getSideOutput(outputLogger))
    }
  }
}
