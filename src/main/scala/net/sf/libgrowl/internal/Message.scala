package net.sf.libgrowl.internal

import java.io._
import java.net.InetAddress
import java.net.Socket
import java.nio.charset.{Charset, StandardCharsets}

import net.sf.libgrowl.MessageType.MessageType
import net.sf.libgrowl.internal.Encryption.EncryptionType

object Message {

  val SOFTWARE_NAME = "libgrowl"
  val SOFTWARE_VERSION = "0.1"
  val GNTP_VERSION = "GNTP/1.0"
  val LINE_BREAK = "\r\n"
  private val MACHINE_NAME: String = InetAddress.getLocalHost.getHostName
  private val PLATFORM_VERSION = System.getProperty("os.version")
  private val PLATFORM_NAME = System.getProperty("os.name")

  val ENCODING: Charset = StandardCharsets.UTF_8

  def send(socket: Socket, messageBytes: Array[Byte]): GntpMessageResponse = {
    val in = new BufferedReader(new InputStreamReader(socket.getInputStream))
    val out = socket.getOutputStream
    out.write(messageBytes)
    out.flush()
    val buffer = new StringBuilder
    var line = in.readLine
    while (line != null && !line.isEmpty) {
      buffer.append(line).append(Message.LINE_BREAK)
      line = in.readLine
    }
    out.close()
    in.close()
    socket.close()
    GntpMessageResponseParser.parse(buffer.toString)
  }
}

abstract class Message protected(val messageType: MessageType, encryption: EncryptionType) {
  implicit val messageBuilder: MessageBuilder = new MessageBuilder
  import MessageHeader._
  ORIGIN_MACHINE_NAME(Message.MACHINE_NAME)
  ORIGIN_SOFTWARE_NAME(Message.SOFTWARE_NAME)
  ORIGIN_SOFTWARE_VERSION(Message.SOFTWARE_VERSION)
  ORIGIN_PLATFORM_NAME(Message.PLATFORM_NAME)
  ORIGIN_PLATFORM_VERSION(Message.PLATFORM_VERSION)

  protected def lineBreak() {
    messageBuilder.buffer.append(Message.LINE_BREAK)
  }

  def buildMessage: Array[Byte] = {
    val out = new ByteArrayOutputStream()
    val headers = messageBuilder.buffer.toString().trim
    messageBuilder.buffer.clear()
    out.write(s"${Message.GNTP_VERSION} $messageType $encryption${Message.LINE_BREAK}".getBytes(Message.ENCODING))
    out.write(encryption(headers.getBytes(Message.ENCODING)))
    out.write((Message.LINE_BREAK * 2).getBytes(Message.ENCODING))
    for ((id, data) <- messageBuilder.resources) {
      val encData = encryption(data)
      IDENTIFIER(id)
      LENGTH(encData.length)
      val dataHeaders = messageBuilder.buffer.toString().trim
      messageBuilder.buffer.clear()
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
