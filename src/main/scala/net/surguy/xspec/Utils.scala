package net.surguy.xspec

import collection.JavaConversions._
import java.io.{IOException, File}

/**
 * Utility methods - normally accessible via other libraries such as Guava, but this
 * project is attempting to limit external dependencies.
 *
 * @author Inigo Surguy
 */
private[xspec] object Utils {

  def createTempDir(): File = {
    val baseDir = new File(System.getProperty("java.io.tmpdir"))
    val dirName = "tempxspec_" + System.nanoTime() + "-" + math.random
    val tempDir = new File(baseDir, dirName)
    if (tempDir.mkdir()) tempDir else throw new IllegalStateException("Could not create temp directory at "+tempDir.getAbsolutePath)
  }

  def deleteRecursively(dir: File) {
    if (!dir.exists) return
    if (dir.isDirectory) dir.listFiles().foreach(deleteRecursively)
    if (!dir.delete()) throw new IOException("Unable to delete directory "+dir)
  }

}
