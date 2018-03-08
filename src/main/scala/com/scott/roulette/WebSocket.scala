/**
  * @author cottinisimone
  */
package com.scott.roulette

import java.util.UUID

import akka.NotUsed
import akka.actor.{ActorRef, ActorSystem, PoisonPill}
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.stream.OverflowStrategy
import akka.stream.scaladsl.{Flow, Sink, Source}
import com.scott.roulette.actors.User
import com.scott.roulette.enums.SignalType
import io.circe.generic.auto._
import io.circe.syntax._
import io.circe.{Json, parser}

object WebSocket {

  sealed trait WebSocketMessage

  /**
    *
    * @param signal string representing the signal from client
    * @param payload can contain two objects. First one is the offer/answer stringified object. Second one is candidate
    *                stringified object.
    *                 offer => `{"type":"offer", "sdp": "v=0\r\no=- 6714962886526132990 2 IN IP4 127.0.0.1\.."}`
    *                 answer => `{"type":"answer", "sdp": "v=0\r\no=- 6714962886526132990 2 IN IP4 127.0.0.1\.."}`
    *                 candidate => `{"candidate":"candidate:2818769553 1 tcp 1..","sdpMid":"audio","sdpMLineIndex":0}"}`
    */
  case class Signal(signal: SignalType, payload: Json = Json.obj()) extends WebSocketMessage

  /**
    *
    * @param actorRef the websocket
    */
  case class WS(actorRef: ActorRef)

  private val InvalidWebsocketMessageEncode: String = "Invalid websocket message encode"

  def apply(room: ActorRef)(implicit system: ActorSystem): Flow[Message, Message, NotUsed] = {

    val userId: String = UUID.randomUUID.toString
    val user: ActorRef = system.actorOf(User.props(userId, room), userId)

    val incomingMessages: Sink[Message, NotUsed] = Flow[Message]
      .map {
        case TextMessage.Strict(text) => decode(text)
        case _                        => Signal(SignalType.Error, error(InvalidWebsocketMessageEncode))
      }
      .to(Sink.actorRef[WebSocketMessage](user, PoisonPill))

    val outgoingMessages: Source[Message, NotUsed] = Source
      .actorRef[Signal](10, OverflowStrategy.fail)
      .mapMaterializedValue { outActor =>
        user ! WS(outActor)
        NotUsed
      }
      .map(value => TextMessage(value.asJson.noSpaces))

    Flow.fromSinkAndSource(incomingMessages, outgoingMessages)
  }

  private def error(s: String): Json = Json.obj("error" -> Json.fromString(s))

  private def decode(text: String): WebSocketMessage = parser.decode[Signal](text) match {
    case Right(signal) => signal
    case Left(ex)      => Signal(SignalType.Error, error(ex.getMessage))
  }
}
