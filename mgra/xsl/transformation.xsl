<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
<xsl:output encoding="UTF-8" method="html" omit-xml-declaration="yes" indent="yes"/>

<xsl:template match="transformation">
	<xsl:apply-templates select="rearrangement">
		<xsl:sort select="id" data-type="number"/>
	</xsl:apply-templates>
	<div id="buttons_trs_{./name}" align="center">
		<input name="download_text" type="button" value="download data in text" onclick="alert('to appear, we download genome.txt');"/>
		<input name="download_png" type="button" value="download data image in archive" onclick="alert('to appear, we download image in archive');"/>		
	</div>	
</xsl:template>

<xsl:template match="rearrangement">
	<xsl:if test= "resize = 'true'"> <img src="{./name}_trs.png" width="100%"></img> </xsl:if>
	<xsl:if test= "resize = 'false'"> <img src="{./name}_trs.png"></img> </xsl:if>
</xsl:template>

	<!--<xsl:if test= "resize = 'true'"> <img src="{./name}_trs.png" width="100%"></img> </xsl:if>
		<xsl:if test= "resize = 'false'"> <img src="{./name}_trs.png"></img> </xsl:if>
		<div id="buttons_trs_{./name}" align="center">
			<input name="download_text" type="button" value="download data in text" onclick="alert('to appear, we download genome.txt');"/>
			<input name="download_png" type="button" value="download data image in archive" onclick="alert('to appear, we download image in archive');"/>		
		</div>-->
	

</xsl:stylesheet>
