/**
  *  @author cottinisimone
  *  @version 1.0, 08/03/2018
  */
package com.scott.roulette.enums

import io.circe.{Decoder, Encoder, Json}

import scala.util.Try

sealed trait SignalType {
  def value: String
  override def toString: String = value
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
    override val value: String = OnlineValue
  }

  case object Ready extends SignalType {
    override val value: String = ReadyValue
  }

  case object Paired extends SignalType {
    override val value: String = PairedValue
  }

  case object Update extends SignalType {
    override val value: String = UpdateValue
  }

  case object Offer extends SignalType {
    override val value: String = OfferValue
  }

  case object Answer extends SignalType {
    override val value: String = AnswerValue
  }

  case object Candidate extends SignalType {
    override val value: String = CandidateValue
  }

  case object Error extends SignalType {
    override val value: String = ErrorValue
  }

  def fromString(signal: String): SignalType = {
    signal.toLowerCase match {
      case OnlineValue    => Online
      case ReadyValue     => Ready
      case PairedValue    => Paired
      case UpdateValue    => Update
      case OfferValue     => Offer
      case AnswerValue    => Answer
      case CandidateValue => Candidate
      case ErrorValue     => Error
      case _              => throw new IllegalArgumentException(s"$signal is not a known Signal value")
    }
  }

  implicit object OriginEncoder extends Encoder[SignalType] {
    override def apply(a: SignalType): Json = Json.fromString(a.toString)
  }

  implicit val originDecoder: Decoder[SignalType] =
    Decoder.decodeString.flatMap(value => Decoder.instanceTry(_ => Try(fromString(value))))
}
