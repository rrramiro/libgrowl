package net.sf.libgrowl.internal

import net.sf.libgrowl.Priority

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
}

class MessageHeader(headerName: String) {
  def apply(value: Boolean)(implicit messageBuilder: MessageBuilder): Unit = apply(value.toString.toLowerCase.capitalize)

  def apply(value: Priority.Value)(implicit messageBuilder: MessageBuilder): Unit = apply(value.id)

  def apply(value: Int)(implicit messageBuilder: MessageBuilder): Unit = apply(String.valueOf(value))

  def apply(value: String)(implicit messageBuilder: MessageBuilder): Unit = {
    messageBuilder.buffer.append(s"$headerName: ${sanitize(value)}${IProtocol.LINE_BREAK}")
  }

  def apply(value: Icon)(implicit messageBuilder: MessageBuilder): Unit = value match {
    case UrlIcon(url) => apply(url)
    case r @ ResourceIcon(_) =>
      apply(IProtocol.X_GROWL_RESOURCE + r.resourceId)
      messageBuilder.resources.put(r.resourceId, r.imageData)
  }

  def sanitize(value: String): String = value.replaceAll(IProtocol.LINE_BREAK, "\n")

  override def toString: String = headerName
}