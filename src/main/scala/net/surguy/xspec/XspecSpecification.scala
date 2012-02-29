package net.surguy.xspec

import org.specs2.mutable.Specification
import org.specs2.specification.Fragments
import java.io.File

/**
 * A trait for XSpec tests - extend and implement the xSpec method.
 *
 * @author Inigo Surguy
 */
abstract class XspecSpecification extends Specification {
  def xSpec: File

  override def is: Fragments = {
    try {
      val results = XspecRunner.runTest(xSpec)
      results.allTests.foreach{ t =>
        exampleFactory.newExample( t.label , {
          if (!t.success && t.expect.length!=0) t.expect must beEqualToIgnoringSpace(t.actual)
          t.success must beTrue
        })
      }
      specFragments
    } catch {
      case e:Exception =>
        // Throwing the exception inside a test gives a nicer error message, including a stack trace
        def doThrow() { throw e }
        exampleFactory.newExample( "Loading the XSpec definition failed", { doThrow() must not(throwAn[Exception]) })
        specFragments
    }
  }
}