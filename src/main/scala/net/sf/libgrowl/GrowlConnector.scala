package net.sf.libgrowl

import net.sf.libgrowl.internal._

import scala.collection.mutable

/**
  * GrowlConnector is the entry point for sending notifications to Growl. Typical
  * use looks like this:
  * <p>
  * <code>
  * // connect to Growl on the given host<br>
  * GrowlConnector growl = new GrowlConnector("hostname");<br>
  * <br>
  * // give your application a name and icon (optionally)<br>
  * Application downloadApp = new Application("Downloader", "http://example.com/icon.png");<br>
  * <br>
  * // create reusable notification types, their names are used in the Growl settings<br>
  * NotificationType downloadStarted = new NotificationType("Download started", "c:\started.png");<br>
  * NotificationType downloadFinished = new NotificationType("Download finished", "c:\finished.jpg");<br>
  * NotificationType[] notificationTypes = new NotificationType[] { downloadStarted, downloadFinished };<br>
  * <br>
  * // now register the application in growl<br>
  * growl.register(downloadApp, notificationTypes);<br>
  * <br>
  * // create a notification with specific title and message<br>
  * Notification ubuntuDownload = new Notification(downloadApp, downloadStarted, "Ubuntu 9.4", "654 MB");<br>
  * <br>
  * // finally send the notification<br>
  * growl.notify(ubuntuDownload);<br>
  * </code>
  * </p>
  *
  * @author Bananeweizen
  *
  */


/**
  * create a growl connection to the given host on the given port number
  *
  * @param host
  * host name
  * @param port
  * port number
  */
class GrowlConnector(val host: String = "localhost", val port: Int = IProtocol.DEFAULT_GROWL_PORT, encryption: Encryption = Encryption.NONE) {
  private val mRegisteredNotifications = mutable.HashSet[NotificationType]()

  /**
    * registers your application with Growl
    * <p>
    * Only after registering an application, it can send notifications. You can
    * re-register your application as often as you want (e.g. during every
    * program start), Growl will be able to handle this.
    * </p>
    *
    * @param application
    * your application
    * @param notificationTypes
    * all notification types supported by your application
    * @return response, see { @link IResponse}
    */
  final def register(application: Application, notificationTypes: NotificationType*): Int = {
    val message = new RegisterMessage(application, notificationTypes, encryption)
    val result = Message.send(host, port, message.buildMessage)
    if (result == IResponse.OK) {
      mRegisteredNotifications ++= notificationTypes
    }
    result
  }

  /**
    * sends a notification to Growl
    * <p>
    * Your application must have been registered first, see
    * {@link #register(Application, NotificationType[])}
    * </p>
    *
    * @param notification
    * notification to send to Growl
    * @return response, see { @link IResponse}
    */
  final def notify(notification: Notification): Int = {
    if (!isRegistered(notification.notificationType)) {
      System.err.println("You need to register the notification type " + notification.notificationType.displayName + " before using it in notifications.")
    }
    val message = new NotifyMessage(notification, encryption)
    Message.send(host, port, message.buildMessage)
  }

  private def isRegistered(notificationType: NotificationType) = mRegisteredNotifications.contains(notificationType)
}