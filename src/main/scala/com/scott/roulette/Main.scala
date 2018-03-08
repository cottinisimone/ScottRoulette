/**
  * @author cottinisimone
  */
package com.scott.roulette

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import akka.util.Timeout
import com.scott.roulette.actors.Chat
import com.scott.roulette.config.ChatConfig

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.io.StdIn

object Main extends App with Routes {

  implicit val system: ActorSystem                = ActorSystem("chat-system")
  implicit val materializer: ActorMaterializer    = ActorMaterializer()
  implicit val executionContext: ExecutionContext = system.dispatcher

  implicit val timeout: Timeout = 30.seconds

  private val chat: ActorRef = system.actorOf(Props(new Chat(ChatConfig.conf)), "chat")

  val bindingFuture = Http().bindAndHandle(routes(chat), "localhost", 8080)
  println(s"Press RETURN to stop...")
  StdIn.readLine()
  bindingFuture
    .flatMap(_.unbind())
    .onComplete(_ => system.terminate())
}
