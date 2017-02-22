package net.sf.libgrowl.internal

import java.util.Date

import net.sf.libgrowl.CallbackResult
import net.sf.libgrowl.ErrorStatus
import net.sf.libgrowl.MessageType
import net.sf.libgrowl.MessageType.MessageType

trait MessageResponse {
  def messageType: MessageType
  def respondingType: MessageType
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
  val messageType: MessageType = MessageType.CALLBACK
  val respondingType: MessageType = MessageType.NOTIFY
}

case class OkMessage(
  internalNotificationId: Option[Long],
  respondingType: MessageType,
  notificationId: Option[String]
) extends MessageResponse {
  val messageType: MessageType = MessageType.OK
}

case class ErrorMessage(
  internalNotificationId: Option[Long],
  respondingType: MessageType,
  status: Option[ErrorStatus.Value],
  description: String
) extends MessageResponse {
  val messageType: MessageType = MessageType.ERROR
}
