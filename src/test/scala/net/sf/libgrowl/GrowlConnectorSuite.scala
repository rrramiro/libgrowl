package net.sf.libgrowl

import net.sf.libgrowl.internal.Encryption
import org.scalatest.FunSuite

class GrowlConnectorSuite extends FunSuite {
  test("notification"){
    val growl = new GrowlConnector("localhost", encryption = Encryption("password"))
    val application = Application("Application name")
    val notificationType = NotificationType("Notification type")
    growl.register(application, notificationType)
    val notification = Notification(application, notificationType, "Notification title", Some("Notification text"))
    growl.notify(notification)
  }
}

