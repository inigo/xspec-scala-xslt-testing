package net.surguy.xspec

import org.specs2.matcher.XmlMatchers
import org.specs2.mutable.Specification
import org.specs2.specification.core.{Fragments, SpecStructure}

import java.io.File

/**
 * A trait for XSpec tests - extend and implement the xSpec method.
 *
 * @author Inigo Surguy
 */
abstract class XspecSpecification extends Specification with XmlMatchers {
  def xSpec: File

  override def is: SpecStructure = {
    try {
      val results = XspecRunner.runTest(xSpec)
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
        def doThrow(): Unit = {
          throw e
        }
        "Loading the XSpec definition must succeed" in {
          doThrow() must not(throwAn[Exception])
        }
    }
  }
}