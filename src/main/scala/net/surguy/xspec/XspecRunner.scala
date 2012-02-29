package net.surguy.xspec

import javax.xml.transform.stream.{StreamResult, StreamSource}
import java.io.{FileNotFoundException, File, FileInputStream}
import xml._
import javax.xml.transform.Templates
import net.sf.saxon.{TransformerFactoryImpl, Controller}

/**
 * Apply an XSpec test.
 *
 * @author Inigo Surguy
 */
object XspecRunner {
  // Note that the XSpec namespace isn't being used in any of the XML matches. This shouldn't matter - Scala XML
  // matches work in every namespace by default, and it doesn't seem possible to put an incorrectly namespaced value
  // anywhere in the XSpec result XML where it will be picked up incorrectly.
  val xspecNs = "{http://www.jenitennison.com/xslt/xspec}"

  val xspecGenerator = getTemplates("/xspec/compiler/generate-xspec-tests.xsl")

  def runTest(specFile: File): XspecResult = {
    if (!specFile.exists()) {
      throw new FileNotFoundException("Cannot find XSpec file at " + specFile.getAbsolutePath)
    }

    val dir = Utils.createTempDir()
    try {
      // XSpec is a two-stage transform, like Schematron. 
      // First it creates a XSpec stylesheet from the test file, then it calls templates in that stylesheet
      
      // First, generate the XSpec stylesheet:
      val xspecFile = new File(dir, "xspec.xsl")
      xspecGenerator.newTransformer().transform(new StreamSource(specFile), new StreamResult(xspecFile))

      // Then, call the initial template (this is using a Saxon-specific API, that isn't yet present in JAXP)
      // The XSpec file itself specifies the stylesheet that it should be applied to, via the stylesheet attribute
      val tFactory = new TransformerFactoryImpl()
      tFactory.setURIResolver(new ClasspathUriResolver())
      val transformer = tFactory.newTransformer(new StreamSource(xspecFile))
      transformer.asInstanceOf[Controller].setInitialTemplate("{http://www.jenitennison.com/xslt/xspec}main")

      val xspecResults = new File(dir, "xspecResult.xml")
      transformer.transform(new StreamSource(xspecFile), new StreamResult(xspecResults))
      val xspecResult = XML.loadFile(xspecResults)
      parseResults(xspecResult)
    } finally {
      Utils.deleteRecursively(dir)
    }
  }
  
  private def getTemplates(fileName: String): Templates = {
    val tFactory = new TransformerFactoryImpl()
    val xslt = XspecRunner.getClass.getResourceAsStream(fileName)
    if (xslt == null) {
      throw new FileNotFoundException("Cannot find XSLT on classpath : " + fileName)
    }
    val uriResolver = new ClasspathUriResolver()
    tFactory.setURIResolver(uriResolver)
    tFactory.newTemplates(new StreamSource(xslt, fileName))
  }

  private[xspec] def parseResults(xspecResult: Elem): XspecResult = XspecResult(parseScenarios(xspecResult, ""))

  private def parseScenarios(elem: Node, labelSoFar: String): Seq[Scenario] = {
    for (scenarioNode <- elem \ "scenario";
         label <- scenarioNode \ "label";
         scenarios = parseScenarios(scenarioNode, labelSoFar+" "+label.text);
         result = parseResults(scenarioNode, labelSoFar+" "+label.text)) yield {
      Scenario(label.text, scenarios, result)
    }
  }

  private def parseResults(elem: Node, labelSoFar: String): Option[XspecTest] = {
    (for (result <- elem \ "test";
          success <- result \ "@successful";
          label <- result \ "label";
          expect = result \ "expect";
          actual <- result \ "result") yield {
      val expectChild = (expect \ "_").collect{ case e: Elem => e.copy( scope = TopScope ) }
      val actualChild = (actual \ "_").collect{ case e: Elem => e.copy( scope = TopScope ) }
      XspecTest((labelSoFar + " " + label.text).trim, success.text.toBoolean, expectChild , actualChild )
    }).headOption
  }

}

