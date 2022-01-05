package fr.ramiro.scala

import java.util.Date

package object growl {
  //scalastyle:off magic.number
  object Priority extends Enumeration {
    type Priority = Value
    val LOWEST = Value(-2)
    val LOW = Value(-1)
    val NORMAL = Value(0)
    val HIGH = Value(1)
    val HIGHEST = Value(2)
  }
  // scalastyle:on magic.number
  object MessageType extends Enumeration {
    val REGISTER, NOTIFY, OK, CALLBACK, ERROR = Value
  }
  //scalastyle:off magic.number
  object ErrorStatus extends Enumeration {
    val RESERVED = Value(100)
    val TIMED_OUT = Value(200)
    val NETWORK_FAILURE = Value(201)
    val INVALID_REQUEST = Value(300)
    val UNKNOWN_PROTOCOL = Value(301)
    val UNKNOWN_PROTOCOL_VERSION = Value(302)
    val REQUIRED_HEADER_MISSING = Value(303)
    val NOT_AUTHORIZED = Value(400)
    val UNKNOWN_APPLICATION = Value(401)
    val UNKNOWN_NOTIFICATION = Value(402)
    val INTERNAL_SERVER_ERROR = Value(500)
  }
  // scalastyle:on magic.number

  object CallbackResult extends Enumeration {
    val CLICK, CLICKED, CLOSE, CLOSED, TIMEOUT, TIMEDOUT = Value
  }

  object HashAlgorithm extends Enumeration {
    val MD5 = Value
    val SHA1 = Value("SHA-256")
    val SHA384 = Value("SHA-384")
    val SHA512 = Value("SHA-512")
  }

  object EncryptionAlgorithm {

    sealed trait Value {
      val name: String
      val code: String

      override def toString: String = name
    }

    case object DES extends Value {
      val name = "DES"
      val code = "DES"
    }

    case object AES extends Value {
      val name = "AES"
      val code = "AES"
    }

    case object DESede extends Value {
      val name = "DESede"
      val code = "3DES"
    }

  }

  case class Application(name: String, icon: Option[Icon] = None)

  case class Notification(
    application: Application,
    notificationType: NotificationType,
    title: String,
    text: Option[String],
    icon: Option[Icon] = None,
    priority: Priority.Value = Priority.NORMAL,
    sticky: Boolean = false,
    urlCallback: Option[String] = None,
    coalescingId: Option[String] = None,
    id: Option[String] = None)

  case class NotificationType(notificationTypeId: String, displayName: String, icon: Option[Icon] = None, enabled: Boolean = true)

  trait MessageResponse {
    def messageType: MessageType.Value
    def respondingType: MessageType.Value
    def internalNotificationId: Option[Long]
  }

  case class CallbackMessage(
    internalNotificationId: Option[Long],
    notificationId: Option[String],
    callbackResult: CallbackResult.Value,
    context: String,
    contextType: String,
    timestamp: Date) extends MessageResponse {
    val messageType: MessageType.Value = MessageType.CALLBACK
    val respondingType: MessageType.Value = MessageType.NOTIFY
  }

  case class OkMessage(
    internalNotificationId: Option[Long],
    respondingType: MessageType.Value,
    notificationId: Option[String]) extends MessageResponse {
    val messageType: MessageType.Value = MessageType.OK
  }

  case class ErrorMessage(
    internalNotificationId: Option[Long],
    respondingType: MessageType.Value,
    status: Option[ErrorStatus.Value],
    description: String) extends MessageResponse {
    val messageType: MessageType.Value = MessageType.ERROR
  }

}
