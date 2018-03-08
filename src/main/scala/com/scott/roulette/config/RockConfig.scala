/**
  *  @author cottinisimone
  *  @version 1.0, 08/03/2018
  */
package com.scott.roulette.config

import com.scott.roulette.config.RockConfig.RoomConfig
import com.typesafe.config.ConfigFactory
import pureconfig.loadConfigOrThrow

import scala.concurrent.duration.FiniteDuration

case class RockConfig(room: RoomConfig)

object RockConfig {
  val conf: RockConfig = loadConfigOrThrow[RockConfig](ConfigFactory.load, "rockroulette")

  case class RoomConfig(updateInterval: FiniteDuration)
}
