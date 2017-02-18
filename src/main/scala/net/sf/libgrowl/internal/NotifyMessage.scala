package net.sf.libgrowl.internal

import net.sf.libgrowl.internal.Encryption.EncryptionType
import net.sf.libgrowl.{MessageType, Notification}

/*
    * // optional callback final String callbackId =
    * notification.getCallbackId(); if (callbackId != null) {
    * header(IProtocol.HEADER_NOTIFICATION_CALLBACK_ID, callbackId); }
    */
class NotifyMessage(val notification: Notification, encryption: EncryptionType) extends Message(MessageType.NOTIFY, encryption) {
  import MessageHeader._
  APPLICATION_NAME(notification.application.name)
  NOTIFICATION_NAME(notification.notificationType.notificationTypeId)
  notification.id.foreach { NOTIFICATION_ID(_) }
  NOTIFICATION_TITLE(notification.title)
  notification.text.foreach{ NOTIFICATION_TEXT(_) }
  NOTIFICATION_STICKY(notification.sticky)
  NOTIFICATION_PRIORITY(notification.priority)
  notification.icon.foreach{ NOTIFICATION_ICON(_) }
  notification.coalescingId.foreach { NOTIFICATION_COALESCING_ID(_) }

//  notification.urlCallback.fold{
//    notification.id.foreach{
//      id =>
//        NOTIFICATION_CALLBACK_CONTEXT(id)
//        NOTIFICATION_CALLBACK_CONTEXT_TYPE("int")
//    }
//  }{
//    NOTIFICATION_CALLBACK_TARGET(_)
//  }
  notification.id.foreach{ NOTIFICATION_INTERNAL_ID(_) }
}
