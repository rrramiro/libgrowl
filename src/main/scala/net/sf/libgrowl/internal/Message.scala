package net.sf.libgrowl.internal

import java.io._
import java.net.InetAddress
import java.net.Socket
import java.nio.charset.{Charset, StandardCharsets}

import net.sf.libgrowl.IResponse
import net.sf.libgrowl.MessageType.MessageType
import net.sf.libgrowl.internal.Encryption.EncryptionType

object Message {

  val SOFTWARE_NAME = "libgrowl"
  val SOFTWARE_VERSION = "0.1"
  /**
    * name of the sending machine
    */
  private val MACHINE_NAME: String = InetAddress.getLocalHost.getHostName

  /**
    * platform version of the sending machine
    */
  private val PLATFORM_VERSION = System.getProperty("os.version")
  /**
    * platform of the sending machine
    */
  private val PLATFORM_NAME = System.getProperty("os.name")

  val ENCODING: Charset = StandardCharsets.UTF_8

  def send(host: String, port: Int, messageBytes: Array[Byte]): Int = {
    var responseText: String = null
    try {
//      while (!messageText.endsWith(IProtocol.LINE_BREAK + IProtocol.LINE_BREAK)) {
//        println("@@@@@@@")
//        messageText = messageText + IProtocol.LINE_BREAK
//      }
      // now start the communication
      val socket = new Socket(host, port)
      socket.setSoTimeout(10000)
      val in = new BufferedReader(new InputStreamReader(socket.getInputStream))
      val out = socket.getOutputStream
      out.write(messageBytes)
      out.flush()
      System.out.println("------------------------")
      System.out.println(new String(messageBytes, ENCODING))
      val buffer = new StringBuilder
      var line = in.readLine
      while (line != null && !line.isEmpty) {
        buffer.append(line).append(IProtocol.LINE_BREAK)
        line = in.readLine
      }
      responseText = buffer.toString
      System.out.println("------------------------")
      System.out.println(responseText)
      System.out.println("------------------------")
      val response = GntpMessageResponseParser.parse(responseText)
      out.close()
      in.close()
      socket.close()
      System.out.println(response)


    } catch {
      case e: Throwable =>
        e.printStackTrace()
        return IResponse.ERROR
    }
    if (responseText == null) return IResponse.ERROR
    if (responseText.contains("-OK")) return IResponse.OK
    IResponse.ERROR
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
    messageBuilder.buffer.append(IProtocol.LINE_BREAK)
  }


  def buildMessage: Array[Byte] = {
    val out = new ByteArrayOutputStream()
    val headers = messageBuilder.buffer.toString()
    messageBuilder.buffer.clear()
    out.write(s"${IProtocol.GNTP_VERSION} $messageType $encryption${IProtocol.LINE_BREAK}".getBytes(Message.ENCODING))
    out.write(encryption(headers.getBytes(Message.ENCODING)))
    //out.write((IProtocol.LINE_BREAK * 2).getBytes(Message.ENCODING))
//    lineBreak()
//    for ((id, data) <- messageBuilder.resources) {
//      lineBreak()
//      IDENTIFIER(id)
//      LENGTH(data.length)
//      lineBreak()
//      messageBuilder.buffer.append(new String(encryption(data), Message.ENCODING))
//      //for (byte b : data) { mBuffer.append((char) b); }
//      lineBreak()
//    }
    // always have a line break and an empty line at the message end
//    messageBuilder.buffer.toString
    out.write(s"${IProtocol.LINE_BREAK}${IProtocol.LINE_BREAK}".getBytes(Message.ENCODING))
    out.flush()
    out.close()
    out.toByteArray
  }

}
