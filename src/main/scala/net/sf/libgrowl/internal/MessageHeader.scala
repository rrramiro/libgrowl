package net.sf.libgrowl.internal

import net.sf.libgrowl.Priority

class MessageHeader(messageBuilder: MessageBuilder, headerName: String){
  def apply(value: Boolean) {
    apply(value.toString.toLowerCase.capitalize)
  }

  def apply(value: Priority.Value) {
    apply(value.id)
  }

  def apply(value: Int) {
    apply(String.valueOf(value))
  }

  def apply(value: String) {
    // filter out any \r\n in the header values
    messageBuilder.buffer.append(headerName).append(": ").append(value.replaceAll(IProtocol.LINE_BREAK, "\n")).append(IProtocol.LINE_BREAK)
  }

  def apply(value: Icon) {
    value match {
      case UrlIcon(url) => apply(url)
      case r @ ResourceIcon(_) =>
        apply(IProtocol.X_GROWL_RESOURCE + r.resourceId)
        messageBuilder.resources.put(r.resourceId, r.imageData)
    }
  }

  override def toString: String = headerName
}
