/**
  *  @author cottinisimone
  *  @version 1.0, 08/03/2018
  */
package com.scott.roulette.actors

import akka.actor.{Actor, ActorLogging, Props}
import com.scott.roulette.WebSocket.{Signal, WS}
import com.scott.roulette.actors.Room._
import com.scott.roulette.enums.SignalType
import com.scott.roulette.tagging.Tag.UserRefTag
import com.scott.roulette.tagging._

/**
  * @param userId user's identifier
  * @param room main room
  */
class User private (userId: String, room: RoomRef) extends Actor with ActorLogging {

  var websocket: Option[WebSocketRef] = None

  /**
    *
    */
  override def postStop(): Unit = {
    super.postStop()
    println(s"$userId - User => Offline!")
  }

  /**
    *
    * @return
    */
  def receive: Receive = {
    case WS(ws) =>
      println(s"$userId - User => Online!")
      context.become(online(ws))
  }

  /**
    * @param ws open websocket
    * @return
    */
  def online(ws: WebSocketRef): Receive = {

    websocket = Option(ws)
    room ! Online(tag[UserRefTag](self))

    {
      case sig @ Signal(SignalType.Online, _) if sender == room =>
        println(s"$userId - User => User online sent to client")
        websocket.foreach(_ ! sig)

      case Signal(SignalType.Ready, _) =>
        println(s"$userId - User => User is ready to be paired")
        room ! Ready

      case sig @ Signal(SignalType.Paired, _) if sender == room =>
        println(s"$userId - User => Signalling user that has been paired with another user")
        websocket.foreach(_ ! sig)

      case sig @ Signal(SignalType.Offer, _) if sender == room =>
        println(s"$userId - User => Forwarding media offer to client")
        websocket.foreach(_ ! sig)

      case sig @ Signal(SignalType.Offer, _) =>
        println(s"$userId - User => Received media offer. Forward to random user")
        room ! MediaOffer(sig)

      case sig @ Signal(SignalType.Answer, _) if sender == room =>
        println(s"$userId - User => Forwarding media answer to client")
        websocket.foreach(_ ! sig)

      case sig @ Signal(SignalType.Answer, _) =>
        println(s"$userId - User => Received media offer. Forward to random user")
        room ! MediaAnswer(sig)

      case sig @ Signal(SignalType.Candidate, _) if sender == room =>
        println(s"$userId - User => Forwarding candidate information to client")
        websocket.foreach(_ ! sig)

      case sig @ Signal(SignalType.Candidate, _) =>
        println(s"$userId - User => Exchanging candidate information with paired user")
        room ! Candidate(sig)

      case sig @ Signal(SignalType.Update, _) if sender == room =>
        println(s"$userId - User => Sending update message to client")
        websocket.foreach(_ ! sig)

      case x =>
        log.error(s"$userId - User => Dead letter for message: {}", x)
    }
  }
}

object User {

  /** Factory method
    * @param userId online user identifier
    * @param room main room
    * @return
    */
  def props(userId: String, room: RoomRef): Props = Props(new User(userId, room))
}
