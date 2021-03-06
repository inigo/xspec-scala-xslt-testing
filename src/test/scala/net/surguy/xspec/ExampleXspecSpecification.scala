package net.surguy.xspec

import java.io.File
import org.specs2.SpecificationWithJUnit

/**
 * An example of how to use the XspecSpecification trait.
 *
 * Note that not all of the tests are expected to pass.
 *
 * @author Inigo Surguy
 */
class ExampleXspecSpecification extends XspecSpecification {

  override def xSpec: File = new File(new File("src/test/resources"), "exampleXspec/test.xspec")

}
