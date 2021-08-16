package com.payments.reader

import akka.stream.alpakka.file.scaladsl.DirectoryChangesSource
import akka.stream.scaladsl.{FileIO, Framing}
import akka.util.ByteString
import cloudflow.akkastream.{AkkaStreamlet, AkkaStreamletLogic}
import cloudflow.streamlets.{StreamletShape, StringConfigParameter}
import cloudflow.streamlets.avro.AvroOutlet
import com.payments.datamodel._

import java.nio.file.{FileSystem, FileSystems, Path}
import scala.concurrent.duration.DurationInt

class FilePaymentsIngress extends AkkaStreamlet{
  val out: AvroOutlet[PaymentString] = AvroOutlet[PaymentString]("out")

  def shape: StreamletShape = StreamletShape.withOutlets(out)

  val fs: FileSystem = FileSystems.getDefault

  val directoryConf: StringConfigParameter           = StringConfigParameter("directory")
  val maskConf: StringConfigParameter                = StringConfigParameter("mask")

  override def configParameters = Vector(directoryConf, maskConf)

  override protected def createLogic(): AkkaStreamletLogic = new AkkaStreamletLogic() {
    override def run(): Unit = {
      val dir = directoryConf.value
      val mask = maskConf.value
      println(dir)
      val streamPay = DirectoryChangesSource(fs.getPath(dir), pollInterval = 1.second, maxBufferSize = 1000)
        .map(_._1)
        .filter(name => name.getFileName.toString.matches(mask))
        .map(name => Path.of(s"$name"))
        .flatMapConcat(path => FileIO.fromPath(path))
        .via(Framing.delimiter(ByteString("\r\n"), maximumFrameLength = 1024))
        .map(x => PaymentString(x.utf8String))
      streamPay.to(plainSink(out)).run()
    }
  }
}
