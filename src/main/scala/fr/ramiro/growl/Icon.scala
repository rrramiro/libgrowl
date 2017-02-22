package fr.ramiro.growl

import java.awt.image.BufferedImage
import java.io.{ ByteArrayOutputStream, File, InputStream }
import javax.imageio.ImageIO
import javax.swing.ImageIcon

sealed trait Icon

case class UrlIcon(url: String) extends Icon

case class ResourceIcon(imageData: Array[Byte]) extends Icon {
  val resourceId: String = Encryption.md5(imageData)
}

object ResourceIcon {
  private val BEST_FORMAT = Array("PNG", "GIF", "JPEG")

  def apply(icon: ImageIcon): ResourceIcon = {
    val iconImage = new BufferedImage(icon.getIconWidth, icon.getIconWidth, BufferedImage.TYPE_INT_ARGB)
    val g2 = iconImage.createGraphics
    icon.paintIcon(null, g2, 0, 0)
    g2.dispose()
    apply(iconImage)
  }

  def apply(iconFile: File): ResourceIcon = apply(ImageIO.read(iconFile))

  def apply(iconStream: InputStream): ResourceIcon = apply(ImageIO.read(iconStream))

  def apply(icon: BufferedImage): ResourceIcon = {
    val output: ByteArrayOutputStream = new ByteArrayOutputStream
    if (!ImageIO.write(icon, getBestFormat(ImageIO.getWriterFormatNames), output)) {
      throw new IllegalStateException("Could not read icon data")
    }
    new ResourceIcon(output.toByteArray)
  }

  private def getBestFormat(formatNames: Array[String]): String = {
    ResourceIcon.BEST_FORMAT.find {
      formatNames.contains
    }.getOrElse(formatNames(0))
  }
}

