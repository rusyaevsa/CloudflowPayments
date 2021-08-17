package com.payments.checker

import com.payments.datamodel._
import org.apache.flink.streaming.api.functions.ProcessFunction
import org.apache.flink.streaming.api.scala.OutputTag
import org.apache.flink.util.Collector

import scala.util.matching.Regex

class MaskChecker(val mask: Regex, val log: OutputTag[LogMessage]) extends ProcessFunction[PaymentString, Payment]{
  override def processElement(
                               value: PaymentString,
                               ctx: ProcessFunction[PaymentString, Payment]#Context,
                               out: Collector[Payment]
                             ): Unit = {
        value match {
          case PaymentString(mask(sender, receiver, amount)) => out.collect(Payment(sender, receiver, amount.toFloat))
          case PaymentString(transfer) => ctx.output(log, LogMessage("WARN", s"The transfer $transfer was not completed"))
        }
  }
}
