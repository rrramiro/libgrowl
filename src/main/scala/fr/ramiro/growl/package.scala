package fr.ramiro

import java.util.Date

package object growl {

  object Priority extends Enumeration {
    type Priority = Value
    val LOWEST = Value(-2)
    val LOW = Value(-1)
    val NORMAL = Value(0)
    val HIGH = Value(1)
    val HIGHEST = Value(2)
  }

  object MessageType extends Enumeration {
    val REGISTER, NOTIFY, OK, CALLBACK, ERROR = Value
  }

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

  /**
   * This class encapsulates an application, that wants to send notifications to
   * Growl
   *
   * @param name
   * application name that will be displayed to the user in the Growl
   * settings dialog
   * @param icon
   * URL or file path of the icon of this application<br>
   * The icon will be used in the settings dialog of Growl and in the
   * notification, if no other icon is specified for the notification
   * type.
   * @author Bananeweizen
   *
   */
  case class Application(name: String, icon: Option[Icon] = None)

  /**
   * notification to be displayed in Growl
   *
   * Create a new notification of the given notification type for the given
   * application. The notification has the given title and message. The icon
   * depends on the notification type.
   *
   * @param application
   * @param notificationType
   * @param title
   * @param text
   * @param priority <p>
   * A higher number indicates a higher priority. This is a display hint for the
   * receiver which may be ignored. It must be one of the {@link IPriority}
   * values.
   * </p>
   * @param sticky Indicates if the notification should remain displayed until dismissed by
   * the user (default false).
   * @param id <p>
   * A unique ID for the notification. If present, serves as a hint to the
   * notification system that this notification should replace any existing
   * on-screen notification with the same ID. This can be used to update an
   * existing notification. The notification system may ignore this hint.
   * </p>
   *
   * A unique ID for the notification. If present, serves as a hint to
   * the notification system that this notification should replace any
   * existing on-screen notification with the same ID. This can be used
   * to update an existing notification. The notification system may
   * ignore this hint.
   *
   * @author Bananeweizen
   */
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
    id: Option[String] = None
  )

  /**
   * A notification type groups all the similar notifications of your application.
   * E.g. you may need a "download started" and "download finished" notification
   * type, which you can then use multiple times for each download that started or
   * finished.
   *
   * @author Bananeweizen
   *
   */
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
      timestamp: Date
  ) extends MessageResponse {
    val messageType: MessageType.Value = MessageType.CALLBACK
    val respondingType: MessageType.Value = MessageType.NOTIFY
  }

  case class OkMessage(
      internalNotificationId: Option[Long],
      respondingType: MessageType.Value,
      notificationId: Option[String]
  ) extends MessageResponse {
    val messageType: MessageType.Value = MessageType.OK
  }

  case class ErrorMessage(
      internalNotificationId: Option[Long],
      respondingType: MessageType.Value,
      status: Option[ErrorStatus.Value],
      description: String
  ) extends MessageResponse {
    val messageType: MessageType.Value = MessageType.ERROR
  }

}
