package net.surguy.xspec

import net.sf.saxon.TransformerFactoryImpl

import java.io.{ByteArrayInputStream, ByteArrayOutputStream, File, FileNotFoundException}
import javax.xml.transform.{Source, Templates}
import javax.xml.transform.stream.{StreamResult, StreamSource}
import scala.xml._

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

  val compileXsltTests = "/io/xspec/xspec/impl/src/compiler/compile-xslt-tests.xsl"

  private lazy val xspecGenerator = getTemplates

  def runTest(specFile: File): XspecResult = {
    if (!specFile.exists()) {
      throw new FileNotFoundException("Cannot find XSpec file at " + specFile.getAbsolutePath)
    }
    // XSpec is a two-stage transform, like Schematron.
    // First it creates a XSpec stylesheet from the test file, then it calls templates in that stylesheet
    // First, generate the XSpec stylesheet:

    val baos = new ByteArrayOutputStream()
    xspecGenerator.newTransformer().transform(new StreamSource(specFile), new StreamResult(baos))

    val xspecStylesheet = baos.toByteArray

    // Then, call the initial template (this is using a Saxon-specific API, that isn't yet present in JAXP)
    // The XSpec file itself specifies the stylesheet that it should be applied to, via the stylesheet attribute

    val tFactory = new TransformerFactoryImpl()
    tFactory.setURIResolver(new ClasspathUriResolver())
    val transformer: net.sf.saxon.jaxp.TransformerImpl =
      tFactory.newTransformer(new StreamSource(new ByteArrayInputStream(xspecStylesheet))).asInstanceOf[net.sf.saxon.jaxp.TransformerImpl]
    transformer.setInitialTemplate("{http://www.jenitennison.com/xslt/xspec}main")

    val xspecResults = new ByteArrayOutputStream()
    transformer.transform(new StreamSource(new ByteArrayInputStream(xspecStylesheet)), new StreamResult(xspecResults))
    val xspecResult = XML.load(new ByteArrayInputStream(xspecResults.toByteArray))
    parseResults(xspecResult)
  }

  private def getTemplates: Templates = {
    val tFactory = new TransformerFactoryImpl()
    val xslt = XspecRunner.getClass.getResourceAsStream(compileXsltTests)
    if (xslt == null) {
      throw new FileNotFoundException("Cannot find XSLT on classpath : " + compileXsltTests)
    }
    val uriResolver = new ClasspathUriResolver()
    tFactory.setURIResolver(uriResolver)
    tFactory.newTemplates(new StreamSource(xslt, compileXsltTests))
  }

  private[xspec] def parseResults(xspecResult: Elem): XspecResult = XspecResult(parseScenarios(xspecResult, ""))

  private def parseScenarios(elem: Node, labelSoFar: String): Seq[Scenario] = {
    for (scenarioNode <- elem \ "scenario";
         label <- scenarioNode \ "label";
         scenarios = parseScenarios(scenarioNode, labelSoFar + " " + label.text);
         result = parseResults(scenarioNode, labelSoFar + " " + label.text)) yield {
      Scenario(label.text, scenarios, result)
    }
  }

  private def parseResults(elem: Node, labelSoFar: String): Option[XspecTest] = {
    (for (result <- elem \ "test";
          success <- result \ "@successful";
          label <- result \ "label";
          expect = result \ "expect";
          actual <- result \ "result") yield {
      val expectChild = (expect \ "_").collect { case e: Elem => e.copy(scope = TopScope) }
      val actualChild = (actual \ "_").collect { case e: Elem => e.copy(scope = TopScope) }
      XspecTest((labelSoFar + " " + label.text).trim, success.text.toBoolean, expectChild, actualChild)
    }).headOption
  }

}

