/**
  *  @author cottinisimone
  *  @version 1.0, 08/03/2018
  */
package com.scott.roulette

import akka.actor.ActorSystem
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import akka.util.Timeout
import com.scott.roulette.tagging.RoomRef

trait Routes {

  implicit val system: ActorSystem
  val materializer: ActorMaterializer

  implicit val timeout: Timeout

  private val resourceDir: String = "web"

  private val index: Route     = path("")(getFromDirectory(s"$resourceDir/index.html"))
  private val resources: Route = path(Remaining)(asset => getFromDirectory(s"$resourceDir/$asset"))

  def routes(room: RoomRef): Route = {
    path("chat") {
      get {
        handleWebSocketMessages(WebSocket(room))
      }
    } ~ index ~ resources
  }
}
