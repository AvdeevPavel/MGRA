<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
<xsl:output encoding="UTF-8" method="html" omit-xml-declaration="yes" indent="yes"/>
	
<xsl:template match="target">
<html>
<head>
<title>MGRA tree</title>
<script src="/mgra/lib/jquery-1.8.0.min.js"></script>
<script>
load_information = null; 
download_information = null;
$(document).ready(function(){
	function my_load_func(nameInf, nameFile) {    			
		$.ajax({ 
			type: "POST",
			url: "/mgra/" + nameFile + ".html",
			async: false,
			cashe: false,
			context: document.body,
			data: "width="+$(window).width() + "&amp;parentHref=" + location.href,
			dataType: 'html',
			beforeSend: function() { 
				$('#' + nameInf + "_bar").html("&lt;u&gt;Please wait. We processed this request: read information, generate images, send. This may take some time.&lt;/u&gt;");	
			}, 
			success: function(data) { 
				$('#' + nameInf + "_info").html(data);
			}, 
			error: function() {
				$('#' + nameInf + "_error").html("&lt;strong&gt;We can not create image. You can download information in view of text file&lt;/strong&gt;");	 				
			}, 
			complete: function() { 
				$('#' + nameInf + "_bar").html("");	
			}  	
		});
	} 
	function my_download_func(nameFile) {    			
		$.ajax({ 
			type: "POST",
			url: "/mgra/" + nameFile,
			async: false,
			cashe: false,
			data: "width="+$(window).width() + "&amp;parentHref=" + location.href,
			success: function(data) { 
				window.location.href = data;
			}, 
		});
	} 
	load_information = my_load_func; 
	download_information = my_download_func;
	load_information("gen<xsl:value-of select="genomes/genome/name"/>", "<xsl:value-of select="genomes/genome/name"/>_gen");
});
</script>
</head>
<body>
    <header> 
		<h1><p align="center"><a href="http://mgra.bioinf.spbau.ru">MGRA (Multiple Genome Rearrangements and Ancestors) web server, beta version</a></p></h1>
	</header>
    <xsl:apply-templates select="genomes/genome"/>	
	<footer>
	<hr/>
	MGRA 1.0 &#169; 2008,09 by Max Alekseyev
	</footer>
</body>
</html>
</xsl:template>

<xsl:template match="genome">
    <h3>Chromosomes for genome <xsl:value-of select="./name"/></h3>
	<p id="gen{./name}_bar" align="center"></p>
	<div id="gen{./name}_info"></div>
	<div id="button_text_gen_{./name}" align="center">
		<input name="download_text" type="button" value="Save as text" onclick="download_information('download/{./name}.gen')"/>
	</div>
</xsl:template>
</xsl:stylesheet>
