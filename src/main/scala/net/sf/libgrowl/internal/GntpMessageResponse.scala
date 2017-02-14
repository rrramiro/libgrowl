package net.sf.libgrowl.internal

import java.util.Date

import net.sf.libgrowl.GntpCallbackResult.GntpCallbackResult
import net.sf.libgrowl.GntpErrorStatus.GntpErrorStatus
import net.sf.libgrowl.MessageType
import net.sf.libgrowl.MessageType.MessageType

trait GntpMessageResponse {
  def messageType: MessageType
  def respondingType: MessageType
  def internalNotificationId: Option[Long]
}

case class GntpCallbackMessage(
  internalNotificationId: Option[Long],
  notificationId: Option[String],
  callbackResult: GntpCallbackResult,
  context: String,
  contextType: String,
  timestamp: Date
) extends GntpMessageResponse {
  val messageType: MessageType = MessageType.CALLBACK
  val respondingType: MessageType = MessageType.NOTIFY
}

case class GntpOkMessage(
  internalNotificationId: Option[Long],
  respondingType: MessageType,
  notificationId: Option[String]
) extends GntpMessageResponse {
  val messageType: MessageType = MessageType.OK
}

case class GntpErrorMessage(
  internalNotificationId: Option[Long],
  respondingType: MessageType,
  status: GntpErrorStatus,
  description: String
) extends GntpMessageResponse {
  val messageType: MessageType = MessageType.ERROR
}
