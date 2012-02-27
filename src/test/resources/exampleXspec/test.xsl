<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:myfn="http://surguy.net/xslt/functions"
                exclude-result-prefixes="#all"
                version="2.0">

    <xsl:template match="node() | @* | processing-instruction() | comment()" priority="-1">
        <xsl:copy>
            <xsl:apply-templates select="node() | @* | processing-instruction() | comment()" mode="#current" />
        </xsl:copy>
    </xsl:template>

    <xsl:function name="myfn:hello" as="xs:string">
        <xsl:param name="text" as="xs:string"/>
        <xsl:sequence select="concat('Hello ', $text)"/>
    </xsl:function>

</xsl:stylesheet>
