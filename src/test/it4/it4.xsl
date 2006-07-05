<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:template match="doc1">
    <doc2><xsl:value-of select="."/></doc2>
  </xsl:template>
</xsl:stylesheet>
