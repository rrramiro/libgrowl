package net.sf.libgrowl

import net.sf.libgrowl.internal.{Encryption, MessageResponse, ResourceIcon}
import org.scalatest.FunSuite

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Success

class GrowlConnectorSuite extends FunSuite {
  val APPLICATION_ICON: String = "app-icon.png"
  val RING_ICON: String = "ring.png"
  val SMS_ICON: String = "sms.png"
  val MMS_ICON: String = "mms.png"
  val BATTERY_ICON: String = "battery100.png"
  val VOICEMAIL_ICON: String = "voicemail.png"
  val PING_ICON: String = APPLICATION_ICON

  test("notification"){
    val growl = new GrowlConnector("localhost", encryption = Encryption("password", EncryptionAlgorithm.AES))(global)

    val application = Application("Application name", getImage(APPLICATION_ICON))
    val notificationType1 = NotificationType("NT1", "Notification type 1", getImage(RING_ICON))
    val notificationType2 = NotificationType("NT2", "Notification type 2", getImage(SMS_ICON))
    val notificationType3 = NotificationType("NT3", "Notification type 3", getImage(MMS_ICON))
    val notificationType4 = NotificationType("NT4", "Notification type 4", getImage(BATTERY_ICON))
    val notificationType5 = NotificationType("NT5", "Notification type 5", getImage(VOICEMAIL_ICON))
    val notificationType6 = NotificationType("NT6", "Notification type 6", getImage(PING_ICON))
    assert(growl.register(
      application,
      notificationType1,
      notificationType2,
      notificationType3,
      notificationType4,
      notificationType5,
      notificationType6
    ))
    val notification1 = Notification(application, notificationType1, "Notification title 1", Some("Notification text 1"))
    val (r1, c1) = growl.notify(notification1)
    printCallback(c1)

    assert(r1.messageType === MessageType.OK)

    val notification2 = Notification(application, notificationType2, "Notification title 2", Some("Notification text 2"), getImage(APPLICATION_ICON))
    val (r2, c2) = growl.notify(notification2)
    printCallback(c2)
    assert(r2.messageType === MessageType.OK)
  }

  def printCallback(fcb: Option[Future[MessageResponse]]) = {
    fcb.foreach{ f =>
      f.onComplete{
        case Success(cb) =>
          println("@@@@@@@@@@@@@@@@@@@@@@@@@@")
          println(cb)
          println("@@@@@@@@@@@@@@@@@@@@@@@@@@")
        case _ =>
      }
    }
  }

  private def getImage(img: String) = Some(ResourceIcon(this.getClass.getClassLoader.getResourceAsStream(img)))
}

