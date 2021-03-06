package net.surguy.xspec

import javax.xml.transform.{Source, URIResolver}
import java.net.URI
import javax.xml.transform.stream.StreamSource


/**
 * A URIResolver that resolves resources that are on the classpath - typically, xsl:imports.
 *
 * @author Inigo Surguy
 */
private[xspec] class ClasspathUriResolver extends URIResolver {
  def resolve(href: String, base: String): Source = {
    val uri = new URI(base).resolve(href)
    val correctedUri = if (uri.toString.startsWith("file:")) uri.toString.substring(5) else uri.toString
    val input = this.getClass.getResourceAsStream(correctedUri)
    if (input != null) new StreamSource(input, correctedUri) else null
  }
}
