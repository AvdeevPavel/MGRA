<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
<xsl:output encoding="UTF-8" method="html" omit-xml-declaration="yes" indent="yes"/>

<xsl:template match="transformation">
	<xsl:text disable-output-escaping='yes'>&lt;!DOCTYPE html></xsl:text>
	<html>
	<body>
	<xsl:apply-templates select="rearrangement_png"/>
	<xsl:apply-templates select="rearrangement_xml"/>
	</body>
	</html>
</xsl:template>

<xsl:template match="rearrangement_png">
	<blockquote>Rearrangement <xsl:value-of select="./id"/></blockquote>  
	<xsl:if test= "resize = 'true'"> <img src="{../name}_trs_{./id}.png" width="100%"></img> </xsl:if>
	<xsl:if test= "resize = 'false'"> <img src="{../name}_trs_{./id}.png"></img> </xsl:if>
</xsl:template>

<xsl:template match="rearrangement_xml">
	<blockquote><font size="4"><strong>Rearrangement <xsl:value-of select="./id"/></strong></font></blockquote> 
	<p><strong>Before:</strong></p>
	<xsl:apply-templates select="before/chromosome">
		<xsl:sort select="id" data-type="number"/>
	</xsl:apply-templates>

	<xsl:apply-templates select="end"/>
	<br/>
	
	<p><strong>After:</strong></p>
	<xsl:apply-templates select="after/chromosome">
		<xsl:sort select="id" data-type="number"/>
	</xsl:apply-templates>
	<br/>
</xsl:template>

 <xsl:template match="chromosome">
	<xsl:if test="10>id">&#160;</xsl:if>
	<xsl:value-of select="id"/>.<xsl:apply-templates select="gene"/><br/>
</xsl:template>

<xsl:template match="gene">
	<xsl:apply-templates select="end" mode="prefix"/>
	<a href="#{id}" title="{id}">
	<xsl:choose>
		<xsl:when test="direction='minus'">&lt;</xsl:when>
		<xsl:otherwise>&gt;</xsl:otherwise>
	</xsl:choose>
	</a>
	<xsl:apply-templates select="end" mode="suffix"/>
</xsl:template>

<xsl:template match="end">
	<span class="end{color}">
		<xsl:value-of select="id"/><xsl:value-of select="type"/>&#160;
	</span>
</xsl:template>

<xsl:template match="end" mode="prefix">
	<xsl:if test="((../direction ='plus') and(type='t')) or ((../direction ='minus') and(type='h'))">
		&#160;<xsl:apply-templates select="." mode="show"/>
	</xsl:if>
</xsl:template>

<xsl:template match="end" mode="suffix">
	<xsl:if test="((../direction ='plus') and(type='h')) or ((../direction ='minus') and(type='t'))">
		<xsl:apply-templates select="." mode="show"/>&#160;
	</xsl:if>
</xsl:template>

<xsl:template match="end" mode="show">
	<span class="end{color}"><xsl:value-of select="type"/></span>
</xsl:template>

<xsl:template match="id">
	'<xsl:value-of select="."/>',
</xsl:template>

</xsl:stylesheet>
			
