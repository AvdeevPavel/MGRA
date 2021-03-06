<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
<xsl:output encoding="UTF-8" method="html" omit-xml-declaration="yes" indent="yes"/>
	
<xsl:template match="trees">
<xsl:text disable-output-escaping='yes'>&lt;!DOCTYPE html></xsl:text>
<html>
<head>			
<script src="/mgra/lib/kinetic-v3.10.5.js"></script>
<script>
	var trees = [ 
		<xsl:apply-templates select="tree" mode = "array"/>
	]

	function getClientWidth() {
		return document.compatMode=='CSS1Compat' &amp;&amp; !window.opera?document.documentElement.clientWidth:document.body.clientWidth;
	}
		
	function getClientHeight() {
		return document.compatMode=='CSS1Compat' &amp;&amp; !window.opera?document.documentElement.clientHeight:document.body.clientHeight;
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
	} 
		
	function changeLines(i, lines, x1, y1) { 
		if (lines[0] != null) { 
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
</script>
</head>
<body>
	<xsl:apply-templates select="tree" mode ="createForm"/>
</body>
</html>
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

</xsl:stylesheet>
