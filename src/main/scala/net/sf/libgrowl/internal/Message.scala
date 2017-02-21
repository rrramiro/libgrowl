package net.sf.libgrowl.internal

import java.net.InetAddress
import java.net.Socket
import java.nio.charset.{Charset, StandardCharsets}
import java.util.Scanner

import net.sf.libgrowl.{Application, MessageType, Notification, NotificationType}
import net.sf.libgrowl.internal.Encryption.EncryptionType
import net.sf.libgrowl.internal.MessageHeader.{APPLICATION_ICON, APPLICATION_NAME, NOTIFICATION_COALESCING_ID, NOTIFICATION_COUNT, NOTIFICATION_DISPLAY_NAME, NOTIFICATION_ENABLED, NOTIFICATION_ICON, NOTIFICATION_ID, NOTIFICATION_INTERNAL_ID, NOTIFICATION_NAME, NOTIFICATION_PRIORITY, NOTIFICATION_STICKY, NOTIFICATION_TEXT, NOTIFICATION_TITLE, _}

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

object Message {
  val SOFTWARE_NAME = "libgrowl"
  val SOFTWARE_VERSION = "0.1"
  val GNTP_VERSION = "GNTP/1.0"
  val LINE_BREAK = "\r\n"
  private val MACHINE_NAME: String = InetAddress.getLocalHost.getHostName
  private val PLATFORM_VERSION = System.getProperty("os.version")
  private val PLATFORM_NAME = System.getProperty("os.name")

  val ENCODING: Charset = StandardCharsets.UTF_8

  def send(socket: Socket, messageBytes: Array[Byte])(implicit ec: ExecutionContext): (GntpMessageResponse, Option[Future[GntpMessageResponse]]) = {
    val in = socket.getInputStream
    val out = socket.getOutputStream

    def closeAll() = {
      out.close()
      in.close()
      socket.close()
    }

    out.write(messageBytes)
    out.flush()
    val scanner: Scanner = new java.util.Scanner(in).useDelimiter(Message.LINE_BREAK * 2)

    val responseMessage = if (scanner.hasNext) {
      val msg = scanner.next()
      GntpMessageResponseParser.parse(msg)
    } else {
      GntpErrorMessage(None, MessageType.ERROR, None, "No repsonse.")
    }

    val callbackFuture = if (scanner.hasNext) {
      Some(Future {
        val callbackResponse = GntpMessageResponseParser.parse(scanner.next())
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

  def registerMessage(application: Application, notificationTypes: Seq[NotificationType], encryption: EncryptionType) = {
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

  def notifyMessage(notification: Notification, encryption: EncryptionType) = {
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

    NOTIFICATION_CALLBACK_CONTEXT("Hello")
    NOTIFICATION_CALLBACK_CONTEXT_TYPE("string")

    //  notification.urlCallback.fold{
    //    notification.id.foreach{
    //      id =>
    //        NOTIFICATION_CALLBACK_CONTEXT(id)
    //        NOTIFICATION_CALLBACK_CONTEXT_TYPE("int")
    //    }
    //  }{
    //    NOTIFICATION_CALLBACK_TARGET(_)
    //  }
    notification.id.foreach { NOTIFICATION_INTERNAL_ID(_) }
    messageBuilder.buildMessage(MessageType.NOTIFY, encryption)
  }
}
