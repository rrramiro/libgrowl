package fr.ramiro.scala.growl

import java.net.{ InetAddress, Socket }
import java.nio.charset.{ Charset, StandardCharsets }
import java.util.Scanner

import Encryption.EncryptionType
import MessageHeader._

import scala.concurrent.{ ExecutionContext, Future }
import org.slf4j.LoggerFactory

object Message {
  val SOFTWARE_NAME = "scala-growl"
  val SOFTWARE_VERSION = "0.1"
  val GNTP_VERSION = "GNTP/1.0"
  val LINE_BREAK = "\r\n"
  private val MACHINE_NAME: String = InetAddress.getLocalHost.getHostName
  private val PLATFORM_VERSION = System.getProperty("os.version")
  private val PLATFORM_NAME = System.getProperty("os.name")
  private val logger = LoggerFactory.getLogger("Message")
  val ENCODING: Charset = StandardCharsets.UTF_8

  def parse(s: String): MessageResponse = {
    val split: Array[String] = s.split(Message.LINE_BREAK)
    assert(split.nonEmpty, "Empty message received from Growl")
    val iter: Iterator[String] = split.iterator
    val statusLine: String = iter.next()
    assert(statusLine.startsWith(Message.GNTP_VERSION), "Unknown protocol version")
    val statusLineIterable: Array[String] = statusLine.split(' ')
    val messageTypeText: String = statusLineIterable(1).stripPrefix("-")
    val messageType: MessageType.Value = MessageType.withName(messageTypeText)
    val headersMap = new collection.mutable.HashMap[String, String]
    while (iter.hasNext) {
      val line = iter.next()
      line.split(":", 2).toList match {
        case headerName :: headerValue :: Nil =>
          headersMap.put(headerName, headerValue.trim)
        case _ => // ignore empty lines
      }
    }
    implicit val headers = headersMap.toMap

    messageType match {
      case MessageType.OK =>
        OkMessage(
          NOTIFICATION_INTERNAL_ID.getOptionalLong,
          RESPONSE_ACTION.getMessageType,
          NOTIFICATION_ID.getOptionalString)
      case MessageType.CALLBACK =>
        CallbackMessage(
          NOTIFICATION_INTERNAL_ID.getOptionalLong,
          NOTIFICATION_ID.getOptionalString,
          NOTIFICATION_CALLBACK_RESULT.getCallbackResult,
          NOTIFICATION_CALLBACK_CONTEXT.getRequiredString,
          NOTIFICATION_CALLBACK_CONTEXT_TYPE.getRequiredString,
          NOTIFICATION_CALLBACK_TIMESTAMP.getDate)
      case MessageType.ERROR =>
        ErrorMessage(
          NOTIFICATION_INTERNAL_ID.getOptionalLong,
          RESPONSE_ACTION.getMessageType,
          ERROR_CODE.getErrorCode,
          ERROR_DESCRIPTION.getRequiredString)
      case _ =>
        throw new IllegalStateException("Unknown response message type: " + messageType)
    }
  }

  def send(socket: Socket, messageBytes: Array[Byte])(implicit ec: ExecutionContext): (MessageResponse, Option[Future[MessageResponse]]) = {
    val in = socket.getInputStream
    val out = socket.getOutputStream
    val scanner: Scanner = new java.util.Scanner(in).useDelimiter(Message.LINE_BREAK * 2)
    out.write(messageBytes)
    out.flush()
    val responseMessage = if (scanner.hasNext) {
      val msg = scanner.next()
      logger.debug(s"responseraw:$msg")
      parse(msg)
    } else {
      ErrorMessage(None, MessageType.ERROR, None, "No repsonse.")
    }

    def closeAll() = {
      out.close()
      in.close()
      socket.close()
    }

    val callbackFuture = if (scanner.hasNext) {
      Some(Future {
        val callbackResponse = parse(scanner.next())
        closeAll()
        callbackResponse
      })
    } else {
      closeAll()
      None
    }
    responseMessage -> callbackFuture
  }

  private def baseMessage(implicit messageBuilder: MessageBuilder) = {
    ORIGIN_MACHINE_NAME(Message.MACHINE_NAME)
    ORIGIN_SOFTWARE_NAME(Message.SOFTWARE_NAME)
    ORIGIN_SOFTWARE_VERSION(Message.SOFTWARE_VERSION)
    ORIGIN_PLATFORM_NAME(Message.PLATFORM_NAME)
    ORIGIN_PLATFORM_VERSION(Message.PLATFORM_VERSION)
  }

  def registerMessage(application: Application, notificationTypes: Seq[NotificationType], encryption: EncryptionType): Array[Byte] = {
    implicit val messageBuilder = new MessageBuilder
    baseMessage
    APPLICATION_NAME(application.name)
    application.icon.foreach { APPLICATION_ICON(_) }
    NOTIFICATION_COUNT(notificationTypes.length)
    for (notificationType <- notificationTypes) {
      messageBuilder.buffer.append(Message.LINE_BREAK)
      NOTIFICATION_NAME(notificationType.notificationTypeId)
      NOTIFICATION_DISPLAY_NAME(notificationType.displayName)
      NOTIFICATION_ENABLED(notificationType.enabled)
      notificationType.icon.foreach { NOTIFICATION_ICON(_) }
    }
    messageBuilder.buildMessage(MessageType.REGISTER, encryption)
  }

  def notifyMessage(notification: Notification, encryption: EncryptionType): Array[Byte] = {
    implicit val messageBuilder = new MessageBuilder
    baseMessage
    APPLICATION_NAME(notification.application.name)
    NOTIFICATION_NAME(notification.notificationType.notificationTypeId)
    notification.id.foreach { NOTIFICATION_ID(_) }
    NOTIFICATION_TITLE(notification.title)
    notification.text.foreach { NOTIFICATION_TEXT(_) }
    NOTIFICATION_STICKY(notification.sticky)
    NOTIFICATION_PRIORITY(notification.priority)
    notification.icon.foreach { NOTIFICATION_ICON(_) }
    notification.coalescingId.foreach { NOTIFICATION_COALESCING_ID(_) }
    notification.urlCallback.fold {
      notification.id.foreach {
        id =>
          NOTIFICATION_CALLBACK_CONTEXT(id)
          NOTIFICATION_CALLBACK_CONTEXT_TYPE("int")
      }
    } {
      NOTIFICATION_CALLBACK_TARGET(_)
    }
    notification.id.foreach { NOTIFICATION_INTERNAL_ID(_) }
    messageBuilder.buildMessage(MessageType.NOTIFY, encryption)
  }
}
