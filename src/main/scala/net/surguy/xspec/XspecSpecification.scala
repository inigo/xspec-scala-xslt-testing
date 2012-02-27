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
    val results = XspecRunner.runTest(xSpec)
    results.allTests.foreach{ t =>
      exampleFactory.newExample( t.label , {
        if (!t.success && t.expect.length!=0) t.expect must beEqualToIgnoringSpace(t.actual)
        t.success must beTrue
      })
    }
    specFragments
  }
}