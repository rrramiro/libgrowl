package net.sf.libgrowl

import net.sf.libgrowl.internal.Icon

/**
  * This class encapsulates an application, that wants to send notifications to
  * Growl
  * @param name
  * application name that will be displayed to the user in the Growl
  * settings dialog
  * @param icon
  * URL or file path of the icon of this application<br>
  * The icon will be used in the settings dialog of Growl and in the
  * notification, if no other icon is specified for the notification
  * type.
  *
  * @author Bananeweizen
  *
  */
case class Application(name: String, icon: Option[Icon] = None)