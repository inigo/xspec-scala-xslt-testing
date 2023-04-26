package net.surguy.xspec

import org.specs2.matcher.XmlMatchers
import org.specs2.mutable.Specification
import org.specs2.specification.core.Fragments

import java.io.{File, FileNotFoundException, IOException}
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file.{FileVisitResult, FileVisitor, Files, Path}
import scala.collection.mutable

/**
 * A trait for XSpec tests - extend and implement the xSpec method.
 *
 * @author Inigo Surguy
 */
abstract class XspecSpecification extends Specification with XmlMatchers {

  /**
   * If this is a file, it is assumed to be a single XSpec test and fragments will be created for it. If it is a
   * directory, it is assumed to be the root of a tree of XSpec tests, in which case we will create a "should" block
   * for every ".xspec" file in it
   */
  def xSpec: File

  lazy val allFiles: Seq[XspecSpecification.LabelledFile] = XspecSpecification.findXSpecFiles(xSpec)

  allFiles.foreach { labelledFile =>
    s"Tests in ${labelledFile.label}" should {
      try {
        val results = XspecRunner.runTest(labelledFile.file)
        Fragments(
          results.allTests.map { test =>
            test.label in {
              try {
                if (!test.success && test.expect.nonEmpty) {
                  test.expect must beEqualToIgnoringSpace(test.actual)
                } else {
                  test.success must beTrue
                }
              } catch {
                case e: Exception =>
                  // Throwing the exception inside a test gives a nicer error message, including a stack trace
                  def doThrow(): Unit = {
                    throw e
                  }

                  doThrow() must not(throwAn[Exception])
              }
            }
          }: _*
        )
      } catch {
        case e: Exception =>
          // Throwing the exception inside a test gives a nicer error message, including a stack trace
          def doThrow(): Unit = {
            throw e
          }
          Fragments (
            s"Parsing ${labelledFile.label}" in {
              doThrow() must not(throwAn[Exception])
            }
          )
      }
    }
  }

}

object XspecSpecification {

  private[xspec] case class LabelledFile(label: String, file: File)

  object LabelledFile {
    def apply(file: File): LabelledFile = LabelledFile(file.getName, file)

    implicit val ordering: Ordering[LabelledFile] = Ordering.by(_.label)
  }

  def findXSpecFiles(fileOrDir: File): Seq[LabelledFile] = {
    if (!fileOrDir.exists()) {
      throw new FileNotFoundException(s"File ${fileOrDir.getAbsolutePath} doesn't exist")
    } else {
      if (fileOrDir.isFile) {
        Seq(LabelledFile(fileOrDir))
      } else if (fileOrDir.isDirectory) {
        val collector = new LabelledFileCollector(fileOrDir.getAbsolutePath.length + 1)
        Files.walkFileTree(fileOrDir.toPath, collector)
        collector.files.toSeq
      } else {
        throw new FileNotFoundException(s"File ${fileOrDir.getAbsolutePath} wasn't a file or directory")
      }

    }
  }

  private class LabelledFileCollector(prefixToSkip: Int) extends FileVisitor[Path] {

    val files = new mutable.TreeSet[LabelledFile]

    override def preVisitDirectory(dir: Path, attrs: BasicFileAttributes): FileVisitResult = FileVisitResult.CONTINUE

    override def visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult = {
      val asFile = file.toFile
      if (asFile.getName.endsWith(".xspec")) {
        files.add(LabelledFile(asFile.getAbsolutePath.substring(prefixToSkip), asFile))
      }
      FileVisitResult.CONTINUE
    }

    override def visitFileFailed(file: Path, exc: IOException): FileVisitResult = FileVisitResult.CONTINUE

    override def postVisitDirectory(dir: Path, exc: IOException): FileVisitResult = FileVisitResult.CONTINUE
  }

}