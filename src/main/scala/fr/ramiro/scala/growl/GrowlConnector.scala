package fr.ramiro.scala.growl

import java.net.Socket

import fr.ramiro.scala.growl.Encryption.EncryptionType
import org.slf4j.LoggerFactory

import scala.collection.mutable
import scala.concurrent.{ ExecutionContext, Future }

class GrowlConnector(
  host: String = "localhost",
  port: Int = GrowlConnector.DEFAULT_GROWL_PORT,
  timeout: Int = GrowlConnector.DEFAULT_TIMEOUT,
  encryption: EncryptionType = Encryption.NONE)(implicit ec: ExecutionContext) {
  private val logger = LoggerFactory.getLogger(this.getClass)
  private val mRegisteredNotifications = mutable.HashSet[NotificationType]()

  final def register(application: Application, notificationTypes: NotificationType*): Boolean = {
    val message = Message.registerMessage(application, notificationTypes, encryption)
    val socket: Socket = new Socket(host, port)
    socket.setSoTimeout(timeout)

    //logger.debug(s"request:\n${new String(message)}")
    val (resp, _) = Message.send(socket, message)
    logger.debug(s"resonse: ${resp}")
    val result = resp.messageType == MessageType.OK
    if (result) {
      mRegisteredNotifications ++= notificationTypes
    }
    result
  }

  final def notify(notification: Notification): (MessageResponse, Option[Future[MessageResponse]]) = {
    if (!isRegistered(notification.notificationType)) {
      logger.warn(s"You need to register the notification type ${notification.notificationType.displayName} before using it in notifications.")
    }
    val message = Message.notifyMessage(notification, encryption)
    val socket: Socket = new Socket(host, port)
    socket.setSoTimeout(timeout)
    Message.send(socket, message)
  }

  private def isRegistered(notificationType: NotificationType) = mRegisteredNotifications.contains(notificationType)
}

object GrowlConnector {
  val DEFAULT_GROWL_PORT = 23053
  val DEFAULT_TIMEOUT = 10000
}
