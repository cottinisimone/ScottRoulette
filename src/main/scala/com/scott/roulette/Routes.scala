/**
  * @author cottinisimone
  */
package com.scott.roulette

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import akka.util.Timeout
import com.scott.roulette.config.ChatConfig

import scala.concurrent.ExecutionContext

trait Routes {

  implicit val system: ActorSystem
  val materializer: ActorMaterializer

  implicit val timeout: Timeout

  private val resourceDir: String = "web"

  private val index: Route     = path("")(getFromDirectory(s"$resourceDir/index.html"))
  private val resources: Route = path(Remaining)(asset => getFromDirectory(s"$resourceDir/$asset"))

  def routes(chat: ActorRef): Route = {
    path("chat") {
      get {
        handleWebSocketMessages(WebSocket(chat))
      }
    } ~ index ~ resources
  }
}
