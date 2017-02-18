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
    val split: Array[String] = s.split(Message.LINE_BREAK)
    assert(split.nonEmpty, "Empty message received from Growl")
    val iter: Iterator[String] = split.iterator
    val statusLine: String = iter.next()
    assert(statusLine.startsWith(Message.GNTP_VERSION), "Unknown protocol version")
    val statusLineIterable: Array[String] = statusLine.split(' ')
    val messageTypeText: String = statusLineIterable(1).stripPrefix("-")
    val messageType: MessageType = MessageType.withName(messageTypeText)
    val headers = new collection.mutable.HashMap[String, String]
    while (iter.hasNext) {
      val line = iter.next()
      line.split(":", 2).toList match {
        case headerName :: headerValue :: Nil =>
          headers.put(headerName, headerValue.trim)
        case _ => // ignore empty lines
      }
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

  implicit class HeaderMapWrapper(headers: Map[String, String]){
    import MessageHeader._
    private def getRequiredValue(gntpMessageHeader: MessageHeader): String = {
      headers.getOrElse(gntpMessageHeader.toString, throw new RuntimeException(s"Required header ${gntpMessageHeader.toString} not found"))
    }

    def getRespondingType: MessageType = Try(MessageType.withName(getRequiredValue(RESPONSE_ACTION))).getOrElse(MessageType.ERROR)

    def getNotificationInternalId: Option[Long] = headers.get(NOTIFICATION_INTERNAL_ID.toString).map(_.toLong)

    def getNotificationId: Option[String] = headers.get(NOTIFICATION_ID.toString)

    def getErrorCode: Option[GntpErrorStatus] = headers.get(ERROR_CODE.toString).map{ errorCode => GntpErrorStatus(errorCode.toInt) }

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
