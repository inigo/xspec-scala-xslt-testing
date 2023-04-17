package net.surguy.xspec

import java.io.{File, IOException}
import java.nio.file.Files

/**
 * Utility methods - normally accessible via other libraries such as Guava, but this
 * project is attempting to limit external dependencies.
 *
 * @author Inigo Surguy
 */
private[xspec] object Utils {

  def createTempDir(): File = {
    Files.createTempDirectory("specs2-xspec").toFile
  }

  def deleteRecursively(dir: File): Unit = {
    if (!dir.exists) return
    if (dir.isDirectory) Option(dir.listFiles()).foreach(_.foreach(deleteRecursively))
    if (!dir.delete()) throw new IOException("Unable to delete directory " + dir)
  }

}
