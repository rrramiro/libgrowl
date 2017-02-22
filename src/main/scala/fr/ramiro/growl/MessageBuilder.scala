package fr.ramiro.growl

import java.io.ByteArrayOutputStream

import fr.ramiro.growl.MessageHeader.{ IDENTIFIER, LENGTH }

import scala.collection.mutable

class MessageBuilder {
  val buffer: StringBuilder = new StringBuilder
  val resources: mutable.HashMap[String, Array[Byte]] = mutable.HashMap[String, Array[Byte]]()

  def buildMessage(messageType: MessageType.Value, encryption: Encryption.EncryptionType): Array[Byte] = {
    val out = new ByteArrayOutputStream()
    val headers = buffer.toString().trim
    buffer.clear()
    out.write(s"${Message.GNTP_VERSION} $messageType $encryption${Message.LINE_BREAK}".getBytes(Message.ENCODING))
    out.write(encryption(headers.getBytes(Message.ENCODING)))
    out.write((Message.LINE_BREAK * 2).getBytes(Message.ENCODING))
    for ((id, data) <- resources) {
      val encData = encryption(data)
      IDENTIFIER(id)(this)
      LENGTH(encData.length)(this)
      val dataHeaders = buffer.toString().trim
      buffer.clear()
      out.write(dataHeaders.getBytes(Message.ENCODING))
      out.write((Message.LINE_BREAK * 2).getBytes(Message.ENCODING))
      out.write(encData)
      out.write((Message.LINE_BREAK * 2).getBytes(Message.ENCODING))
    }
    out.flush()
    out.close()
    out.toByteArray
  }
}
