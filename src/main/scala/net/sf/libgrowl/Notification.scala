package net.sf.libgrowl

import java.io.File
import javax.swing.ImageIcon
import net.sf.libgrowl.internal.Icon
import net.sf.libgrowl.internal.ResourceIcon
import net.sf.libgrowl.internal.UrlIcon

/**
  * notification to be displayed in Growl
  *
  * Create a new notification of the given notification type for the given
  * application. The notification has the given title and message. The icon
  * depends on the notification type.
  *
  * @param application
  * @param notificationType
  * @param title
  * @param text
  * @param priority <p>
  * A higher number indicates a higher priority. This is a display hint for the
  * receiver which may be ignored. It must be one of the {@link IPriority}
  * values.
  * </p>
  * @param sticky Indicates if the notification should remain displayed until dismissed by
  * the user (default false).
  * @param id <p>
  * A unique ID for the notification. If present, serves as a hint to the
  * notification system that this notification should replace any existing
  * on-screen notification with the same ID. This can be used to update an
  * existing notification. The notification system may ignore this hint.
  * </p>
  *
  * A unique ID for the notification. If present, serves as a hint to
  * the notification system that this notification should replace any
  * existing on-screen notification with the same ID. This can be used
  * to update an existing notification. The notification system may
  * ignore this hint.
  *
  * @author Bananeweizen
  */
case class Notification(
  application: Application,
  notificationType: NotificationType,
  title: String,
  text: Option[String],
  priority: Priority.Value = Priority.NORMAL,
  sticky: Boolean = false,
  icon: Option[Icon] = None,
  urlCallback: Option[String] = None,
  coalescingId: Option[String] = None,
  id: Option[String] = None
)