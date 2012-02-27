package net.surguy.xspec

import java.io.File
import xml.XML
import org.specs2.mutable.SpecificationWithJUnit

/**
 * @author Inigo Surguy
 */
class XspecSpecificationTest extends SpecificationWithJUnit {

  private def getFile(fileName: String) = new File(new File("src/test/resources"), fileName)
  
  "Applying an XSpec test" should {
    "produce some sensible results" in {
      val results = XspecRunner.runTest(getFile("/exampleXspec/test.xspec"))
      println(results)
      (results must not).beNull
    }
  }

  "Parsing XSpec results" should {
    val xml = XML.load(this.getClass.getResourceAsStream("/exampleXspec/xspecOut.xml"))
    val result = XspecRunner.parseResults(xml)
    "return a tree of result objects" in { result.allScenarios.exists( _.label == "identity transforms" ) must beTrue}
    "return all scenarios" in { result.allScenarios must haveLength(3)}
    "return all tests (not all scenarios have tests) " in { result.allTests must haveLength(2)}
    "identify whether all tests passed" in { result.isSuccessful must beFalse}
  }

}