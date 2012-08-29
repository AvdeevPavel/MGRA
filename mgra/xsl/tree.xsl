<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
<xsl:output encoding="UTF-8" method="html" omit-xml-declaration="yes" indent="yes"/>

<xsl:template match="full">
<xsl:text disable-output-escaping='yes'>&lt;!DOCTYPE html></xsl:text>
<html>
<head>
<title>MGRA tree</title>
				
<style type="text/css">
	.end0{color:green}
	.end1{color:red}
	.end2{color:lime}
	.end3{color:maroon}
</style>
<script src="/mgra/lib/jquery-1.8.0.min.js"></script>
<script src="/mgra/lib/kinetic-v3.10.5.js"></script>
<script>
	load_information = null;
	getClientWidth = null; 
	getClientHeight = null; 

	var values =  [
		<xsl:apply-templates select="genomes/genome/name"/>
	]
				
	var trees = [ 
		<xsl:apply-templates select="trees/input/tree" mode = "array"/>
		<xsl:apply-templates select="trees/reconstruct/tree" mode = "array"/>
	]

	function getRearrangementImage(nameTransfor, id) {
		if (document.getElementById("trs" + nameTransfor + "_info" + id).firstChild == null) { 
			load_information("trs"+nameTransfor, nameTransfor+"_trs/"+id, id)
			document.getElementById("trs" + nameTransfor + "_xml" + id).innerHTML = "";
			document.getElementById("rear_image_" + nameTransfor + id).style.display = "none";
		} 
	}
				
	function changeStyle(id, show){
		var element = document.getElementById(id);
		if (element != null) {
			element.style.display= id == show ? "" : "none";
		}
	}

	function createStage(nameContainer, width_, height_) {	
		var stage = new Kinetic.Stage({ 
			container: nameContainer,
			width: width_,  
			height: height_
		});

		stage.on("mouseup", function() {
       		document.body.style.cursor = "default";
       	});
		return stage; 		 
	} 	
			
	function createText(str, color, coorX, coorY, layer) { 
		var text = new Kinetic.Text({
			text: str,	
			x: coorX,
			y: coorY,
			cornerRadius: 5,
			stroke: "black",
			strokeWidth: 2,
			fill: color,
			fontSize: 13,
			padding: 13, 
			fontFamily: "Calibri",
			textFill: "black",
			align: 'center',
			fontStyle: 'italic',
			shadow: {
				color: 'black',
				blur: 1,
				offset: [10, 10],
				alpha: 0.2
			}
		});

		text.on("mouseover", function() { 
			this.setDraggable(true);
			this.setFill("#8DB6CD");
			layer.draw();
		});	

		text.on("mouseout", function() { 
			this.setDraggable(false);
			if (document.getElementById('gen'+str) != null) {
				this.setFill("#0000CD");
       		} else {
				this.setFill("#87CEFA");
			}
			layer.draw();
		});	

		text.on("mousedown", function() {
       		document.body.style.cursor = "pointer";
       	});

		text.on("dblclick", function(evt) { 
			for (var i = 0; i &lt; values.length; ++i) {
				changeStyle('gen'+values[i], 'gen'+str);
				changeStyle('trs'+values[i], 'gen'+str);
			}

			if (document.getElementById('gen'+str+'_info').firstChild == null) { 
				load_information('gen'+ str, str + '_gen', "");
			} 
		});
		return text;  
	}

	function createLine(x1, y1, x2, y2, color, strId, strName) { 
		var line = new Kinetic.Line({
			points: [x1, y1, x2, y2],
			stroke: color, 
			lineCap: "round",
			lineJoin: "round", 
			strokeWidth: 4,
			id: strId + "_" + strName,
			name: strName
		});
		return line;
	}

	function createRootArrow(fromX, fromY, toX, toY, text, stage, color) { 
		var group = new Kinetic.Group();

		var line1 = new Kinetic.Shape({
         	drawFunc: function(context) {
	      	  context.beginPath();
	       	  context.moveTo(fromX, fromY);
	       	  context.quadraticCurveTo(stage.getWidth() / 2, 5, toX, toY);
	       	  this.stroke(context);
			},
	        stroke: color,
			lineJoin: "round",
			strokeWidth: 4,
			id: "mainLine_" + text,
			name: text
		});
		line1.saveImageData();

		var headlen = 15;   
		var angle = Math.atan2(5 - toY, stage.getWidth() / 2 - toX);
		group.add(line1);				
		group.add(createLine(toX, toY, toX + headlen*Math.cos(angle-Math.PI/6), toY + headlen*Math.sin(angle-Math.PI/6), color, "leftLine", text));
		group.add(createLine(toX, toY, toX + headlen*Math.cos(angle+Math.PI/6), toY + headlen*Math.sin(angle+Math.PI/6), color, "rightLine", text)); 
		return group;
	} 

	function createArrow(fromX, fromY, toX, toY, text, color) { 
		var group = new Kinetic.Group();
		var headlen = 15;   
    	var angle = Math.atan2(fromY - toY, fromX - toX);

		group.add(createLine(fromX, fromY, toX, toY, color, "mainLine", text)); 
		group.add(createLine(fromX, fromY, fromX - headlen*Math.cos(angle-Math.PI/6), fromY - headlen*Math.sin(angle-Math.PI/6), color, "leftLine", text));   
		group.add(createLine(fromX, fromY, fromX - headlen*Math.cos(angle+Math.PI/6), fromY - headlen*Math.sin(angle+Math.PI/6), color, "rightLine", text));   
		return group;
	} 	

	function createNodes(k, stage, layer) {
 		var stepHeight = stage.getHeight() / (trees[k].length + 1); 
		var countInLevel = 2; 

		for(var i = 0; i &lt;trees[k].length; ++i) { 
			countInLevel = countInLevel * 2;
			var stepWidth = 1; 
			for(var j = 0; j &lt; trees[k][i].length; ++j) { 
	     		if (document.getElementById('gen'+trees[k][i][j].text) != null) {
					trees[k][i][j].viewRect = createText(trees[k][i][j].text, "#0000CD", stepWidth * stage.getWidth() / countInLevel, stepHeight + i * stepHeight, layer);
				} else { 
					trees[k][i][j].viewRect = createText(trees[k][i][j].text, "#87CEFA", stepWidth * stage.getWidth() / countInLevel, stepHeight + i * stepHeight, layer);
				} 
				stepWidth += 2;								
			}
		} 

		for(var i = 0; i &lt; trees[k].length; ++i) { 
			for(var j = 0; j &lt; trees[k][i].length; ++j) { 
				layer.add(trees[k][i][j].viewRect);
			} 
		}				
		stage.add(layer);
	}

	function createEdges(k, stage, layer) { 
		for(var i = 0; i &lt; trees[k].length; ++i) { 
			for(var j = 0; j &lt; trees[k][i].length; ++j) { 
				trees[k][i][j].arrows = new Array(3);
			} 
		}
			
		var rootArray = null;
		if (document.getElementById('trs' + trees[k][0][0].text) != null) {
			rootArrow = createRootArrow((trees[k][0][0].viewRect.getX() + trees[k][0][0].viewRect.getBoxWidth()), trees[k][0][0].viewRect.getY(), trees[k][0][1].viewRect.getX(), trees[k][0][1].viewRect.getY(), trees[k][0][0].text, stage, "black");
		} else { 
			rootArrow = createRootArrow((trees[k][0][0].viewRect.getX() + trees[k][0][0].viewRect.getBoxWidth()), trees[k][0][0].viewRect.getY(), trees[k][0][1].viewRect.getX(), trees[k][0][1].viewRect.getY(), trees[k][0][0].text, stage, "#878787");
		} 
		trees[k][0][0].arrows[0] = rootArrow; 
		trees[k][0][1].arrows[0] = rootArrow;
		
		for(var i = 0; i &lt; trees[k].length; ++i) { 
			for(var j = 0; j &lt; trees[k][i].length; ++j) {
				if (trees[k][i][j].leftChildNumber != -1) {
					var x1 = trees[k][i][j].viewRect.getX();
					var y1 = trees[k][i][j].viewRect.getY() + trees[k][i][j].viewRect.getBoxHeight(); 
					var x2 = trees[k][i + 1][trees[k][i][j].leftChildNumber].viewRect.getX() + trees[k][i + 1][trees[k][i][j].leftChildNumber].viewRect.getBoxWidth() / 2; 
					var y2 = trees[k][i + 1][trees[k][i][j].leftChildNumber].viewRect.getY(); 
					if (document.getElementById('trs' + trees[k][i + 1][trees[k][i][j].leftChildNumber].text) != null) { 
						trees[k][i][j].arrows[1] = createArrow(x1, y1, x2, y2, trees[k][i + 1][trees[k][i][j].leftChildNumber].text, "black");								
					} else { 
						trees[k][i][j].arrows[1] = createArrow(x1, y1, x2, y2, trees[k][i + 1][trees[k][i][j].leftChildNumber].text, "#878787");							
					} 
					trees[k][i + 1][trees[k][i][j].leftChildNumber].arrows[0] = trees[k][i][j].arrows[1]; 
				} 
								
				if (trees[k][i][j].rightChildNumber != -1) { 
					var x1 = trees[k][i][j].viewRect.getX() + trees[k][i][j].viewRect.getBoxWidth();
					var y1 = trees[k][i][j].viewRect.getY() + trees[k][i][j].viewRect.getBoxHeight(); 
					var x2 = trees[k][i + 1][trees[k][i][j].rightChildNumber].viewRect.getX() + trees[k][i + 1][trees[k][i][j].rightChildNumber].viewRect.getBoxWidth() / 2; 
					var y2 = trees[k][i + 1][trees[k][i][j].rightChildNumber].viewRect.getY(); 												
					if (document.getElementById('trs' + trees[k][i + 1][trees[k][i][j].rightChildNumber].text) != null) { 
						trees[k][i][j].arrows[2] = createArrow(x1, y1, x2, y2, trees[k][i + 1][trees[k][i][j].rightChildNumber].text, "black");								
					} else {
						trees[k][i][j].arrows[2] = createArrow(x1, y1, x2, y2, trees[k][i + 1][trees[k][i][j].rightChildNumber].text, "#878787");							
					}	
					trees[k][i + 1][trees[k][i][j].rightChildNumber].arrows[0] = trees[k][i][j].arrows[2]; 
				}
			} 
		} 					
							
		for(var i = 0; i &lt; trees[k].length - 1; ++i) { 
			for(var j = 0; j &lt; trees[k][i].length; ++j) { 
				for(var c = 0; c &lt; trees[k][i][j].arrows.length; ++c) { 
					if (trees[k][i][j].arrows[c] != null) { 
						layer.add(trees[k][i][j].arrows[c]);
					} 
				} 
			} 
		}
		stage.add(layer);

		for(var i = 0; i &lt; values.length; ++i) { 
			var line = layer.get('#mainLine_' + values[i])[0];
			if (line != null) { 
				line.saveImageData();
				line.on("dblclick", function(evt) { 
					str = 'trs' + this.getName();
					for (var i = 0; i &lt; values.length; ++i) {
	                	changeStyle('gen'+values[i], str);
						changeStyle('trs'+values[i], str);
                 	}
	
					if (document.getElementById(str + '_info').firstChild == null) { 
						load_information(str, this.getName() + '_trs', "");
					} 
				});
			} 		
		}
	} 
		
	function changeLines(i, lines, x1, y1) { 
		if (lines[0] != null) { 
			lines[0].saveImageData();
			lines[0].attrs.points[i] ={x: x1, y: y1};				
			lines[0].saveImageData();
			var headlen = 15;   
			var angle = Math.atan2(lines[0].attrs.points[0].y - lines[0].attrs.points[1].y, lines[0].attrs.points[0].x - lines[0].attrs.points[1].x);
			lines[1].setPoints([lines[0].attrs.points[0].x, lines[0].attrs.points[0].y, lines[0].attrs.points[0].x - headlen*Math.cos(angle-Math.PI/6), lines[0].attrs.points[0].y - headlen*Math.sin(angle-Math.PI/6)]);

			lines[2].setPoints([lines[0].attrs.points[0].x, lines[0].attrs.points[0].y, lines[0].attrs.points[0].x - headlen*Math.cos(angle+Math.PI/6), lines[0].attrs.points[0].y - headlen*Math.sin(angle+Math.PI/6)]);						
		} 
	} 

	function createEventDrag(rect, arrows, layer) { 
		rect.on("dragmove dragend", function() {
			if (arrows[0] != null) {
				var lines = arrows[0].getChildren();
				changeLines(1, lines, this.getX() + this.getBoxWidth() / 2, this.getY());
			} 

			if (arrows[1] != null) { 
				var lines = arrows[1].getChildren();
				changeLines(0, lines, this.getX(), this.getY() + this.getBoxHeight());		 		
			} 

			if (arrows[2] != null) { 
				var lines = arrows[2].getChildren();
				changeLines(0, lines, this.getX()+this.getBoxWidth(), this.getY() + this.getBoxHeight());				 
			}  											
			layer.draw();
		});
 	} 

	function createEventForRoot(rect, arrows, rect1, helpArrows, flag) { 
		rect.on("dragmove dragend", function() {
			if (arrows[0] != null) {		
				var line = arrows[0].getChildren()[0];			
				this.getLayer().remove(arrows[0]);
				if (flag == true) { 
					arrows[0] = createRootArrow(this.getX() + this.getBoxWidth(), this.getY(), rect1.getX(), rect1.getY(), line.getName(), this.getStage(), line.getStroke());
				} else { 
					arrows[0] = createRootArrow(rect1.getX() + rect1.getBoxWidth(), rect1.getY(), this.getX(), this.getY(), line.getName(), this.getStage(), line.getStroke());
				} 
				helpArrows[0] = arrows[0]; 
				this.getLayer().add(arrows[0]);
			} 

			if (arrows[1] != null) { 
				var lines = arrows[1].getChildren();
				changeLines(0, lines, this.getX(), this.getY() + this.getBoxHeight());		 		
			} 

			if (arrows[2] != null) { 
				var lines = arrows[2].getChildren();
				changeLines(0, lines, this.getX()+this.getBoxWidth(), this.getY() + this.getBoxHeight());				 
			}  											
			this.getLayer().draw();
		});
	} 

	function main(k, stage, layer) { 
		createNodes(k, stage, layer); 			
		createEdges(k, stage, layer);
		
		createEventForRoot(trees[k][0][0].viewRect, trees[k][0][0].arrows, trees[k][0][1].viewRect, trees[k][0][1].arrows, true); 
		createEventForRoot(trees[k][0][1].viewRect, trees[k][0][1].arrows, trees[k][0][0].viewRect, trees[k][0][0].arrows, false);
		for(var i = 1; i &lt; trees[k].length; ++i) { 
			for(var j = 0; j &lt; trees[k][i].length; ++j) {
				createEventDrag(trees[k][i][j].viewRect, trees[k][i][j].arrows, layer);	 
			} 
		}		
	} 

	window.onload = function() {
		for(var i = 0; i &lt; trees.length; ++i) {  
			var stage = createStage("tree" + i, getClientWidth(),  getClientHeight() / 2);
			var layer = new Kinetic.Layer();
			main(i, stage, layer);
			document.getElementById("save" + i).addEventListener("click", function() {
    	    	stage.toDataURL({
    	        	callback: function(dataUrl) {
    	       		   window.open(dataUrl);
    	        	}
    	      	});
    	    }, false);
		} 
	};

	$(document).ready(function(){
		function my_load_func(nameInf, nameFile, id) {    			
			$.ajax({ 
				type: "POST",
				url: "/mgra/" + nameFile + ".html",
				async: false,
				cashe: false,
				context: document.body,
				data: "width="+$(window).width() + "&amp;parentHref=" + location.href,
				dataType: 'html',
				beforeSend: function() { 
					$("#" + nameInf + "_bar" + id).html("&lt;u&gt;Please wait. We processed this request: read information, generate images or html text, send. This may take some time.&lt;/u&gt;");	
				}, 
				success	: function(data) { 
					$("#" + nameInf + "_info" + id).html(data);
				}, 
				error: function() {
					$("#" + nameInf + "_info" + id).html("&lt;p align=\"center\"&gt;&lt;strong&gt;We can not create image. You can download information in view of text file&lt;/strong&gt;&lt;/p&gt;");	 				
				}, 
				complete: function() { 
					$("#" + nameInf + "_bar" + id).html("");	
				}  	
			});
		} 
		function my_getWidth() { 
			return $(window).width();
		} 
		function my_getHeight() { 
			return $(window).height();
		} 
		load_information = my_load_func; 
		getClientWidth = my_getWidth;
		getClientHeight = my_getHeight;
	});	
</script>
</head>
<body>
	<header> 
		<h1><p align="center"><a href="http://mgra.bioinf.spbau.ru">MGRA (Multiple Genome Rearrangements and Ancestors) web server, beta version</a></p></h1>  
	</header>
	<p><font size="4"><strong>Information for working subtree(s): </strong></font></p> 
	<ol type="1">
		<li>
			You can drag and drop nodes. Click interesting node and drag what you want.   
		</li>
		<br/>
		<li>
			If node is dark blue, you push double click and see genome.
		</li>
		<br/>
		<li>
			If arrow is black, you push double click and see transformation.        		
		</li>
		<br/>
	</ol>
	<xsl:apply-templates select="trees"/>
	<xsl:apply-templates select="genomes"/>	
	<xsl:apply-templates select="transformations"/>
	<footer>
	<hr/>
	MGRA 1.0 &#169; 2008,09 by Max Alekseyev
	</footer>
</body>
</html>
</xsl:template>

<!--Trees transformation-->
<xsl:template match="trees">
	<xsl:apply-templates select="input"/>
	<xsl:apply-templates select="reconstruct"/>
</xsl:template>

<xsl:template match="input">
	<p><font size = "10"><string> Input subtree(s): </string></font></p>
	<xsl:apply-templates select="tree" mode = "createForm"/>
</xsl:template>

<xsl:template match="reconstruct">
	<p><font size = "10"><string> Reconstructed subtree(s): </string></font></p>
	<xsl:apply-templates select="tree" mode = "createForm"/>
</xsl:template>

<xsl:template match="tree" mode = "array">
	[
		<xsl:apply-templates select="row"/>
	], 
</xsl:template>

<xsl:template match="tree" mode = "createForm">
	<div id = "block{./id}">
		<div id = "tree{./id}"></div>  
		<center> <button id = "save{./id}">Save as image</button> </center>	
	</div>
</xsl:template>

<xsl:template match="row">
   	[
		<xsl:apply-templates select="cell"/>
	],
</xsl:template>

<xsl:template match="cell">
	{	
		viewRect: null, 
		arrows: null,
  		leftChildNumber: <xsl:value-of select="leftChildNumber"/>,
		rightChildNumber: <xsl:value-of select="rightChildNumber"/>,
		text: "<xsl:value-of select="name"/>"
	},
</xsl:template>

<!--Genomes transformation-->
<xsl:template match="genomes">
	<xsl:apply-templates select = "genome"/>
</xsl:template>

<xsl:template match="genome">
	<div id="gen{./name}" style="display:none;">
    	<h3>Chromosomes for genome <xsl:value-of select="./name"/></h3>
		<p id="gen{./name}_bar" align="center"></p>
		<div id="gen{./name}_info"></div>
		<div id="button_text_gen_{./name}" align="center">
			<input name="download_text" type="button" value="Save as text" onclick="window.location.href='{./name}.gen'"/>
		</div>
	</div>
</xsl:template>

<!--Transformations XSL transformation-->
<xsl:template match="transformations">
	<xsl:apply-templates select = "transformation"/>
</xsl:template>
	
<xsl:template match="transformation">
	<div id="trs{./name}" style="display:none;">
		<h3>Transformation for <xsl:value-of select="./name"/></h3>
		<p id="trs{./name}_bar" align="center"></p>
		<div id="trs{./name}_info"></div>
		<div id="buttons_trs_{./name}" align="center">
			<input name="download_text" type="button" value="Save as text" onclick="window.location.href='{./name}.trs'"/>
		</div>	
	</div>
</xsl:template>

<xsl:template match="name">
	'<xsl:value-of select="."/>',
</xsl:template>
</xsl:stylesheet>
