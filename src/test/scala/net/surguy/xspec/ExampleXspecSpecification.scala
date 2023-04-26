package net.surguy.xspec

import java.io.File

/**
 * An example of how to use the XspecSpecification trait.
 *
 * Note that not all of the tests are expected to pass.
 *
 * @author Inigo Surguy
 */
class ExampleXspecSpecification extends XspecSpecification {

  override def xSpec: File = new File("src/test/resources")

}
