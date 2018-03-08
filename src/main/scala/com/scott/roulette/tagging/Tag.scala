/**
  *  @author cottinisimone
  *  @version 1.0, 08/03/2018
  */
package com.scott.roulette.tagging

sealed trait Tag

/**
  * Classes used to tag classes
  */
object Tag {
  trait RoomRefTag      extends Tag
  trait UserRefTag      extends Tag
  trait WebSocketRefTag extends Tag
}
