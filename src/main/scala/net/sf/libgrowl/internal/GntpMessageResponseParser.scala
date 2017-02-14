package net.sf.libgrowl.internal


import java.text.SimpleDateFormat
import java.util.Date

import net.sf.libgrowl.GntpCallbackResult.GntpCallbackResult
import net.sf.libgrowl.GntpErrorStatus.GntpErrorStatus
import net.sf.libgrowl.{GntpCallbackResult, GntpErrorStatus, MessageType}
import net.sf.libgrowl.MessageType.MessageType

import scala.util.{Failure, Success, Try}

object GntpMessageResponseParser {
  val DATE_TIME_FORMAT: String = "yyyy-MM-dd'T'HH:mm:ssZ"
  val DATE_TIME_FORMAT_ALTERNATE: String = "yyyy-MM-dd HH:mm:ss'Z'"
  val DATE_TIME_FORMAT_GROWL_1_3: String = "yyyy-MM-dd"

  private val dateFormats = Seq(
    DATE_TIME_FORMAT,
    DATE_TIME_FORMAT_ALTERNATE,
    DATE_TIME_FORMAT_GROWL_1_3
  )

  def parse(s: String): GntpMessageResponse = {
    val splitted: Array[String] = s.split(IProtocol.LINE_BREAK) //separatorSplitter.split(s)
    assert(splitted.nonEmpty, "Empty message received from Growl")
    val iter: Iterator[String] = splitted.iterator
    val statusLine: String = iter.next()
    assert(statusLine.startsWith(IProtocol.GNTP_VERSION), "Unknown protocol version")
    val statusLineIterable: Array[String] = statusLine.split(' ') //statusLineSplitter.split(statusLine)
    val messageTypeText: String = statusLineIterable(1).stripPrefix("-")
    val messageType: MessageType = MessageType.withName(messageTypeText)
    val headers = new collection.mutable.HashMap[String, String]
    while (iter.hasNext) {
      val splitedHeader: Array[String] = iter.next().split(":", 2)
      headers.put(splitedHeader(0), splitedHeader(1).trim)
    }
    messageType match {
      case MessageType.OK =>
        createOkMessage(headers.toMap)
      case MessageType.CALLBACK =>
        createCallbackMessage(headers.toMap)
      case MessageType.ERROR =>
        createErrorMessage(headers.toMap)
      case _ =>
        throw new IllegalStateException("Unknown response message type: " + messageType)
    }
  }

  private def createOkMessage(headers: Map[String, String]): GntpOkMessage = {
    GntpOkMessage(
      headers.getNotificationInternalId,
      headers.getRespondingType,
      headers.getNotificationId
    )
  }

  private def createCallbackMessage(headers: Map[String, String]): GntpCallbackMessage = {
    GntpCallbackMessage(
      headers.getNotificationInternalId,
      headers.getNotificationId,
      headers.getNotificationCallbackResult,
      headers.getNotificationCallbackContext,
      headers.getNotificationCallbackContextType,
      headers.getNotificationCallbackTimestamp
    )
  }

  private def createErrorMessage(headers: Map[String, String]): GntpErrorMessage = {
    GntpErrorMessage(
      headers.getNotificationInternalId,
      headers.getRespondingType,
      headers.getErrorCode,
      headers.getErrorDescription
    )
  }

  implicit class HeaderMapWrapper(headers: Map[String, String]) extends Headers{
    private def getRequiredValue(gntpMessageHeader: MessageHeader): String = {
      headers.getOrElse(gntpMessageHeader.toString, throw new RuntimeException(s"Required header ${gntpMessageHeader.toString} not found"))
    }

    def getRespondingType: MessageType = MessageType.withName(getRequiredValue(RESPONSE_ACTION))

    def getNotificationInternalId: Option[Long] = headers.get(NOTIFICATION_INTERNAL_ID.toString).map(_.toLong)

    def getNotificationId: Option[String] = headers.get(NOTIFICATION_ID.toString)

    def getErrorCode: GntpErrorStatus = GntpErrorStatus(getRequiredValue(ERROR_CODE).toInt)

    def getErrorDescription: String = getRequiredValue(ERROR_DESCRIPTION)

    def getNotificationCallbackResult: GntpCallbackResult = GntpCallbackResult.withName(getRequiredValue(NOTIFICATION_CALLBACK_RESULT))

    def getNotificationCallbackContext: String = getRequiredValue(NOTIFICATION_CALLBACK_CONTEXT)

    def getNotificationCallbackContextType: String = getRequiredValue(NOTIFICATION_CALLBACK_CONTEXT_TYPE)

    def getNotificationCallbackTimestamp: Date = parseTimestamp(getRequiredValue(NOTIFICATION_CALLBACK_TIMESTAMP), dateFormats)

    private def parseTimestamp(timestampText: String, dateFormats: Seq[String]): Date = {
      dateFormats match {
        case format :: tail =>
          Try(new SimpleDateFormat(format).parse(timestampText)) match {
            case Failure(e) => parseTimestamp(timestampText, tail)
            case Success(timestamp) => timestamp
          }
        case Nil =>
          throw new RuntimeException("Timestamp Bad Format")
      }
    }
  }

}
