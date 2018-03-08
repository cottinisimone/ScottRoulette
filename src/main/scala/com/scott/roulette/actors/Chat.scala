/**
  * @author cottinisimone
  */
package com.scott.roulette.actors

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSelection, Terminated}
import com.scott.roulette.WebSocket.Signal
import com.scott.roulette.actors.Chat._
import com.scott.roulette.config.ChatConfig
import com.scott.roulette.enums.SignalType
import io.circe.Json
import io.circe.generic.auto._
import io.circe.syntax._

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

/**
  *
  * @param config The chat config
  */
class Chat(config: ChatConfig)(implicit ec: ExecutionContext) extends Actor with ActorLogging {

  var users: Set[String]          = Set()
  var idles: Set[String]          = Set()
  var paired: Map[String, String] = Map.empty

  context.system.scheduler.schedule(0.seconds, config.updateInterval)(
    users.foreach(user(_) ! Signal(SignalType.Update, Update(users.size).asJson))
  )

  /**
    *
    * @return
    */
  override def receive: Receive = {

    case Online(user) =>
      val userId = sender.path.name
      println(s"$userId - Connects to chat")
      users += userId
      context.watch(user) ! Signal(SignalType.Online, DefaultPayload)

    case Ready =>
      val userId = sender.path.name
      println(s"$userId - User is ready to be paired")
      pairOrIdle(userId)

    case Next =>
      val userId = sender.path.name
      println(s"$userId - User terminate previous session and skip to next user")
      unpair(userId, paired(userId))
      pairOrIdle(userId)

    case MediaOffer(sig) =>
      println(s"${sender.path.name} - Chat => Forwarding media offer to user")
      paired.get(sender.path.name).foreach(user(_) ! sig)

    case MediaAnswer(sig) =>
      println(s"${sender.path.name} - Chat => Forwarding media answer to user")
      paired.get(sender.path.name).foreach(user(_) ! sig)

    case Candidate(sig) =>
      println(s"${sender.path.name} - Chat => Forwarding candidate message to user")
      paired.get(sender.path.name).foreach(user(_) ! sig)

    case Terminated(user) =>
      offline(user.path.name)

    case x => log.error("Chat - Dead letter for message: {}", x)
  }

  /**
    *
    * @param userId user to pair
    */
  private def pairOrIdle(userId: String): Unit = {
    idles.headOption match {
      case Some(otherUserId) =>
        pair(userId, otherUserId)
        user(userId) ! Signal(SignalType.Paired)
      case None =>
        idles = idles + userId
    }
  }

  /**
    * Method to pair two users
    * @param userId ready user
    * @param otherUserId idle user that was waiting for another idle user
    */
  private def pair(userId: String, otherUserId: String): Unit = {
    println(s"$userId - Paired with user $otherUserId")
    paired += userId      -> otherUserId
    paired += otherUserId -> userId
    idles -= userId
    idles -= otherUserId
  }

  /**
    *
    * @param userId the user that requires to skip to next user
    */
  private def unpair(userId: String, otherUserId: String): Unit = {
    println(s"$userId - Unpaired from user $otherUserId")
    paired -= userId
    paired -= otherUserId
    idles += userId
    idles += otherUserId
  }

  /**
    * Remove all reference of the offline user
    * @param userId user gone offline
    */
  private def offline(userId: String): Unit = {
    println(s"$userId - Chat => User switched to Offline")
    users -= userId
    paired -= userId
    idles -= userId
    paired.get(userId).foreach { otherUserId =>
      paired -= otherUserId
    }
  }

  /**
    *
    * @param userId identifier to get the corresponding user's ActorRef
    * @return
    */
  private def user(userId: String): ActorSelection = context.system.actorSelection(s"/user/$userId")
}

object Chat {

  val DefaultPayload: Json = Json.obj()

  /** User sends message to room to Join
    * @param user Reference to User actor
    */
  case class Online(user: ActorRef)
  case object Ready
  private final case class Pair(userId: String)

  case class Next(signal: Signal)

  case class MediaOffer(signal: Signal)
  case class MediaAnswer(signal: Signal)
  case class Candidate(signal: Signal)

  case class Update(onlineUsers: Int, time: Long = System.currentTimeMillis)

  case object UserNotFound
}
