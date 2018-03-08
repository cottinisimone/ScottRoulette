/**
  *  @author cottinisimone
  *  @version 1.0, 08/03/2018
  */
package com.scott.roulette

import akka.actor.ActorRef
import com.scott.roulette.tagging.Tag.{RoomRefTag, UserRefTag, WebSocketRefTag}
import shapeless.tag.@@

package object tagging {

  type RoomRef      = ActorRef @@ RoomRefTag
  type UserRef      = ActorRef @@ UserRefTag
  type WebSocketRef = ActorRef @@ WebSocketRefTag

  /** This method sickly exists because IntelliJ is not
    *   IntelliJent enough to understand that shapeless.tag
    *   import is used. Organizing imports erase the shapeless.tag
    *   import statement..
    *
    * @param value the object to tag
    * @tparam T tagging trait
    * @return the class tagged (thanks to Miles Sabin)
    */
  def tag[T <: Tag](value: ActorRef): ActorRef @@ T = shapeless.tag[T](value)
  def tag[T <: Tag](value: String): String @@ T     = shapeless.tag[T](value)
}
