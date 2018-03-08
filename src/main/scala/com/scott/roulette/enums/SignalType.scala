/**
  * @author cottinisimone
  */
package com.scott.roulette.enums

import io.circe.{Decoder, Encoder, Json}

import scala.util.Try

sealed trait SignalType {
  def value: String
}

object SignalType {
  // Signals used for functionality
  private val OnlineValue: String = "online"
  private val ReadyValue: String  = "ready"
  private val PairedValue: String = "paired"
  private val UpdateValue: String = "update"

  // WebRTC protocol signals
  private val OfferValue: String     = "offer"
  private val AnswerValue: String    = "answer"
  private val CandidateValue: String = "candidate"

  // Signal to handle errors
  private val ErrorValue: String = "error"

  case object Online extends SignalType {
    override val value: String    = OnlineValue
    override val toString: String = value
  }

  case object Ready extends SignalType {
    override val value: String    = ReadyValue
    override val toString: String = value
  }

  case object Paired extends SignalType {
    override val value: String    = PairedValue
    override val toString: String = value
  }

  case object Update extends SignalType {
    override val value: String    = UpdateValue
    override val toString: String = value
  }

  case object Offer extends SignalType {
    override val value: String    = OfferValue
    override val toString: String = value
  }

  case object Answer extends SignalType {
    override val value: String    = AnswerValue
    override val toString: String = value
  }

  case object Candidate extends SignalType {
    override val value: String    = CandidateValue
    override val toString: String = value
  }

  case object Error extends SignalType {
    override val value: String    = ErrorValue
    override val toString: String = value
  }

  def fromString(str: String): SignalType = {
    str.toLowerCase match {
      case OnlineValue    => Online
      case ReadyValue     => Ready
      case PairedValue    => Paired
      case UpdateValue    => Update
      case OfferValue     => Offer
      case AnswerValue    => Answer
      case CandidateValue => Candidate
      case ErrorValue     => Error
      case _              => throw new IllegalArgumentException(s"$str is not a known Signal value")
    }
  }

  implicit object OriginEncoder extends Encoder[SignalType] {
    override def apply(a: SignalType): Json = Json.fromString(a.toString)
  }

  implicit val originDecoder: Decoder[SignalType] = Decoder.decodeString.flatMap { str =>
    Decoder.instanceTry { _ =>
      Try(fromString(str))
    }
  }
}
