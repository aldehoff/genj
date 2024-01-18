<?xml version='1.0' encoding='ISO-8859-1'?>

<!-- =============================================================== -->
<!--                                                                 -->
<!-- Convert XSL FO to CSV (comma separated values)                  -->
<!--                                                                 -->
<!-- Author: Nils Meier, nmeier at users dot sourceforge dot net     -->
<!--                                                                 -->
<!-- =============================================================== -->

<xsl:stylesheet version="1.0"
 xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
 xmlns:fo="http://www.w3.org/1999/XSL/Format"
 exclude-result-prefixes="fo">
 
<xsl:output method="text" encoding="utf-8" indent="no"/>

<xsl:template match="fo:table">
  <xsl:if test="@role='csv'">
   <xsl:apply-templates/>
  </xsl:if>
</xsl:template>

<xsl:template match="fo:table-row">
 <xsl:apply-templates select="fo:table-cell"/>
 <xsl:value-of select="'&#xA;'"/>
</xsl:template>
 
<xsl:template match="fo:table-cell">
 <xsl:value-of select="."/>
 <xsl:if test="position()!=last()">;</xsl:if>
</xsl:template>
 
<xsl:template match="text()">
</xsl:template>
 
</xsl:stylesheet>