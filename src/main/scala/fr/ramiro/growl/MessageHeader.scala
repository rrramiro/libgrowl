package fr.ramiro.growl

import java.text.SimpleDateFormat
import java.util.Date

import scala.util.{ Failure, Success, Try }

object MessageHeader {
  val ORIGIN_MACHINE_NAME: MessageHeader = new MessageHeader("Origin-Machine-Name")
  val ORIGIN_PLATFORM_NAME: MessageHeader = new MessageHeader("Origin-Platform-Name")
  val ORIGIN_PLATFORM_VERSION: MessageHeader = new MessageHeader("Origin-Platform-Version")
  val ORIGIN_SOFTWARE_NAME: MessageHeader = new MessageHeader("Origin-Software-Name")
  val ORIGIN_SOFTWARE_VERSION: MessageHeader = new MessageHeader("Origin-Software-Version")
  val APPLICATION_NAME: MessageHeader = new MessageHeader("Application-Name")
  val APPLICATION_ICON: MessageHeader = new MessageHeader("Application-Icon")
  val NOTIFICATION_COUNT: MessageHeader = new MessageHeader("Notifications-Count")
  val NOTIFICATION_INTERNAL_ID: MessageHeader = new MessageHeader("X-Data-Internal-Notification-ID")
  val NOTIFICATION_ID: MessageHeader = new MessageHeader("Notification-ID")
  val NOTIFICATION_NAME: MessageHeader = new MessageHeader("Notification-Name")
  val NOTIFICATION_DISPLAY_NAME: MessageHeader = new MessageHeader("Notification-Display-Name")
  val NOTIFICATION_TITLE: MessageHeader = new MessageHeader("Notification-Title")
  val NOTIFICATION_ENABLED: MessageHeader = new MessageHeader("Notification-Enabled")
  val NOTIFICATION_ICON: MessageHeader = new MessageHeader("Notification-Icon")
  val NOTIFICATION_TEXT: MessageHeader = new MessageHeader("Notification-Text")
  val NOTIFICATION_STICKY: MessageHeader = new MessageHeader("Notification-Sticky")
  val NOTIFICATION_PRIORITY: MessageHeader = new MessageHeader("Notification-Priority")
  val NOTIFICATION_COALESCING_ID: MessageHeader = new MessageHeader("Notification-Coalescing-ID")

  val NOTIFICATION_CALLBACK_TARGET: MessageHeader = new MessageHeader("Notification-Callback-Target")
  val NOTIFICATION_CALLBACK_CONTEXT: MessageHeader = new MessageHeader("Notification-Callback-Context")
  val NOTIFICATION_CALLBACK_CONTEXT_TYPE: MessageHeader = new MessageHeader("Notification-Callback-Context-Type")
  val NOTIFICATION_CALLBACK_RESULT: MessageHeader = new MessageHeader("Notification-Callback-Result")
  val NOTIFICATION_CALLBACK_TIMESTAMP: MessageHeader = new MessageHeader("Notification-Callback-Timestamp")

  val RESPONSE_ACTION: MessageHeader = new MessageHeader("Response-Action")
  val ERROR_CODE: MessageHeader = new MessageHeader("Error-Code")
  val ERROR_DESCRIPTION: MessageHeader = new MessageHeader("Error-Description")
  val IDENTIFIER: MessageHeader = new MessageHeader("Identifier")
  val LENGTH: MessageHeader = new MessageHeader("Length")

  private val X_GROWL_RESOURCE = "x-growl-resource://"

  private val DATE_FORMATS = Seq(
    "yyyy-MM-dd'T'HH:mm:ssZ",
    "yyyy-MM-dd HH:mm:ss'Z'",
    "yyyy-MM-dd"
  )
}

class MessageHeader(headerName: String) {
  def apply(value: Boolean)(implicit messageBuilder: MessageBuilder): Unit = apply(value.toString.toLowerCase.capitalize)

  def apply(value: Priority.Value)(implicit messageBuilder: MessageBuilder): Unit = apply(value.id)

  def apply(value: Int)(implicit messageBuilder: MessageBuilder): Unit = apply(String.valueOf(value))

  def apply(value: String)(implicit messageBuilder: MessageBuilder): Unit = {
    messageBuilder.buffer.append(s"$headerName: ${sanitize(value)}${Message.LINE_BREAK}")
  }

  def apply(value: Icon)(implicit messageBuilder: MessageBuilder): Unit = value match {
    case UrlIcon(url) => apply(url)
    case r @ ResourceIcon(_) =>
      apply(MessageHeader.X_GROWL_RESOURCE + r.resourceId)
      messageBuilder.resources.put(r.resourceId, r.imageData)
  }

  private def getRequiredValue(gntpMessageHeader: MessageHeader)(implicit headers: Map[String, String]): String = {
    headers.getOrElse(gntpMessageHeader.toString, throw new RuntimeException(s"Required header ${gntpMessageHeader.toString} not found"))
  }

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

  def getMessageType(implicit headers: Map[String, String]): MessageType.Value = Try(MessageType.withName(getRequiredValue(this))).getOrElse(MessageType.ERROR)

  def getOptionalLong(implicit headers: Map[String, String]): Option[Long] = headers.get(this.toString).map(_.toLong)

  def getOptionalString(implicit headers: Map[String, String]): Option[String] = headers.get(this.toString)

  def getErrorCode(implicit headers: Map[String, String]): Option[ErrorStatus.Value] = headers.get(this.toString).map { errorCode => ErrorStatus(errorCode.toInt) }

  def getRequiredString(implicit headers: Map[String, String]): String = getRequiredValue(this)

  def getCallbackResult(implicit headers: Map[String, String]): CallbackResult.Value = CallbackResult.withName(getRequiredValue(this))

  def getDate(implicit headers: Map[String, String]): Date = parseTimestamp(getRequiredValue(this), MessageHeader.DATE_FORMATS)

  private def sanitize(value: String): String = value.replaceAll(Message.LINE_BREAK, "\n")

  override def toString: String = headerName
}