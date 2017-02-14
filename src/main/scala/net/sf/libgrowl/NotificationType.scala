package net.sf.libgrowl

import net.sf.libgrowl.internal.UrlIcon

/**
 * A notification type groups all the similar notifications of your application.
 * E.g. you may need a "download started" and "download finished" notification
 * type, which you can then use multiple times for each download that started or
 * finished.
 *
 * @author Bananeweizen
 *
 */
case class NotificationType(notificationTypeId: String, displayName: String, enabled: Boolean = true, icon: Option[UrlIcon] = None)

object NotificationType{
  def apply(displayName: String): NotificationType = {
    apply(displayName, displayName)
  }
}
