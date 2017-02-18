package net.sf.libgrowl

import javax.imageio.ImageIO

import net.sf.libgrowl.internal.{Encryption, ResourceIcon}
import org.scalatest.FunSuite

class GrowlConnectorSuite extends FunSuite {
  val APPLICATION_ICON: String = "app-icon.png"
  val RING_ICON: String = "ring.png"
  val SMS_ICON: String = "sms.png"
  val MMS_ICON: String = "mms.png"
  val BATTERY_ICON: String = "battery100.png"
  val VOICEMAIL_ICON: String = "voicemail.png"
  val PING_ICON: String = APPLICATION_ICON

  test("notification"){
    val growl = new GrowlConnector("localhost", encryption = Encryption("password"))

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
    ) === MessageType.OK)
    val notification1 = Notification(application, notificationType1, "Notification title 1", Some("Notification text 1"))
    assert(growl.notify(notification1) === MessageType.OK)

    val notification2 = Notification(application, notificationType2, "Notification title 2", Some("Notification text 2"), getImage(APPLICATION_ICON))
    assert(growl.notify(notification2) === MessageType.OK)
  }

  private def getImage(img: String) = Some(ResourceIcon(ImageIO.read(this.getClass.getClassLoader.getResourceAsStream(img))))
}

