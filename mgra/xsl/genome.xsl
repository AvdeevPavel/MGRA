<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
<xsl:output encoding="UTF-8" method="html" omit-xml-declaration="yes" indent="yes"/>

<xsl:template match="genome_png">
	<xsl:text disable-output-escaping='yes'>&lt;!DOCTYPE html></xsl:text>
	<html>
	<body>
	<xsl:if test= "resize = 'true'"> <img src="{./name}_gen.png" width="100%"></img> </xsl:if>	
	<xsl:if test= "resize = 'false'"> <img src="{./name}_gen.png"></img> </xsl:if>
	</body>
	</html>
</xsl:template>

<xsl:template match="genome_xml">
	<xsl:text disable-output-escaping='yes'>&lt;!DOCTYPE html></xsl:text>
	<html>
	<body>
	<xsl:apply-templates select="chromosome">
		<xsl:sort select="id" data-type="number"/>
	</xsl:apply-templates>
	</body>
	</html>
</xsl:template>

<xsl:template match="chromosome">
	<xsl:if test="10>id">&#160;</xsl:if>
	<xsl:value-of select="id"/>.<xsl:apply-templates select="gene"/><br/>
</xsl:template>

<xsl:template match="gene">
	<a href="#{id}" title="{id}">
		<xsl:choose>
			<xsl:when test="direction='minus'">&lt;</xsl:when>
			<xsl:otherwise>&gt;</xsl:otherwise>
		</xsl:choose>
	</a>
</xsl:template>

</xsl:stylesheet>


