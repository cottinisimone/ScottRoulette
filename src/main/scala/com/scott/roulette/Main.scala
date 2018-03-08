/**
  *  @author cottinisimone
  *  @version 1.0, 08/03/2018
  */
package com.scott.roulette

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import akka.util.Timeout
import com.scott.roulette.actors.Room
import com.scott.roulette.config.RockConfig
import com.scott.roulette.tagging.Tag.RoomRefTag
import com.scott.roulette.tagging.{RoomRef, tag}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.io.StdIn

object Main extends App with Routes {

  implicit val system: ActorSystem                = ActorSystem("rockroulette-system")
  implicit val materializer: ActorMaterializer    = ActorMaterializer()
  implicit val executionContext: ExecutionContext = system.dispatcher

  implicit val timeout: Timeout = 30.seconds

  private val chat: RoomRef = tag[RoomRefTag](system.actorOf(Room.props(RockConfig.conf), "rockroulette"))

  val bindingFuture = Http().bindAndHandle(routes(chat), "localhost", 8080)
  println(s"Press RETURN to stop...")
  StdIn.readLine()
  bindingFuture
    .flatMap(_.unbind())
    .onComplete(_ => system.terminate())
}
