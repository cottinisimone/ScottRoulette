/**
  * @author cottinisimone
  */
package com.scott.roulette.config

import com.typesafe.config.ConfigFactory
import pureconfig.loadConfigOrThrow

import scala.concurrent.duration.FiniteDuration

case class ChatConfig(updateInterval: FiniteDuration)

object ChatConfig {
  val conf: ChatConfig = loadConfigOrThrow[ChatConfig](ConfigFactory.load, "chat")
}
