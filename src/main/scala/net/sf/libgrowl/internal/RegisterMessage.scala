package net.sf.libgrowl.internal

import net.sf.libgrowl.{Application, MessageType, NotificationType}

class RegisterMessage(val mApplication: Application, val notificationTypes: Seq[NotificationType], encryption: Encryption) extends Message(MessageType.REGISTER, encryption) {
  APPLICATION_NAME(mApplication.name)
  mApplication.icon.foreach{ APPLICATION_ICON(_) }
  NOTIFICATION_COUNT(notificationTypes.length)
  for (notificationType <- notificationTypes) {
    lineBreak()
    NOTIFICATION_NAME(notificationType.notificationTypeId)
    NOTIFICATION_DISPLAY_NAME(notificationType.displayName)
    NOTIFICATION_ENABLED(notificationType.enabled)
    notificationType.icon.foreach{ NOTIFICATION_ICON(_)}
  }
}
