<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:output encoding="UTF-8" method="html" omit-xml-declaration="yes" indent="yes"/>

	<xsl:template match="trees">
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

				<!--<script src="/home/Desktop/MGRA/kinetic-v3.10.4.js"></script>-->
				<script src="http://www.kineticjs.com/download/kinetic-v3.10.4.js"></script>
            	<script>
                	var values =  [
               	    	<xsl:apply-templates select="tree/row/cell/text"/>
                	]
					
					var trees = [ 
						<xsl:apply-templates select="tree" mode = "array"/>
					]

					function getClientWidth() {
						return document.compatMode=='CSS1Compat' &amp;&amp; !window.opera?document.documentElement.clientWidth:document.body.clientWidth;
					}
		
					function getClientHeight() {
						return document.compatMode=='CSS1Compat' &amp;&amp; !window.opera?document.documentElement.clientHeight:document.body.clientHeight;
					}
					
					function changeStyle(id, show){
	                    var element = document.getElementById(id);
	                    if (element != null) {
	                        element.style.display= id == show ? "" : "none";;
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
							this.setFill("blue");
							layer.draw();
						});	
						text.on("mouseout", function() { 
							this.setDraggable(false);
							var element = document.getElementById('gen'+str);
	                    	if (element != null) {
								this.setFill("yellow");
	                 		} else {
								this.setFill("#00dddd");
							}
							layer.draw();
						});	

						text.on("mousedown", function() {
			          		document.body.style.cursor = "pointer";
			        	});

						text.on("dblclick", function(evt) { 
							for (var i = 0; i &lt; values.length; ++i) {
	                        	changeStyle('trs'+values[i], 'trs'+str);
	                        	changeStyle('gen'+values[i], 'gen'+str);
	                    	}	
						});
						return text;  
					}

					function createLine(x1, y1, x2, y2) { 
						var line = new Kinetic.Line({
							points: [{x: x1, y: y1} , {x: x2, y: y2}],
							stroke: "black", 
							lineCap: "round",
							lineJoin: "round", 
							strokeWidth: 3,
						});
						return line;
					} 
		
					function createEventDrag(rect, lines, layer) { 
						rect.on("dragmove dragend", function() {
							if (lines[0] != null)
								lines[0].attrs.points[1] ={x:(this.getX()+this.getBoxWidth() / 2), y:this.getY()};				
							if (lines[1] != null)		
								lines[1].attrs.points[0] ={x:(this.getX()+this.getBoxWidth() / 2), y:(this.getY()+this.getBoxHeight())};
							if (lines[2] != null)	
								lines[2].attrs.points[0] ={x:(this.getX()+this.getBoxWidth() / 2), y:(this.getY()+this.getBoxHeight())};						
							layer.draw();
    	    			});
					} 

 					function createNodes(k, stage, layer) {
 						var stepHeight = stage.getHeight() / (trees[k].length + 1); 
						var countInLevel = 2; 

						for(var i = 0; i &lt; trees[k].length; ++i) { 
							countInLevel = countInLevel * 2;
							var stepWidth = 1; 
							for(var j = 0; j &lt; trees[k][i].length; ++j) { 
								var element = document.getElementById('gen'+trees[k][i][j].text);
	                    		if (element != null) {
										trees[k][i][j].viewRect = createText(trees[k][i][j].text, "yellow", stepWidth * stage.getWidth() / countInLevel, stepHeight + i * stepHeight, layer);
								} else { 
										trees[k][i][j].viewRect = createText(trees[k][i][j].text, "#00dddd", stepWidth * stage.getWidth() / countInLevel, stepHeight + i * stepHeight, layer);
								} 
								stepWidth += 2;								
							}
						} 
					}

					function createEdge(k, stage) { 

						for(var i = 0; i &lt; trees[k].length; ++i) { 
							for(var j = 0; j &lt; trees[k][i].length; ++j) { 
								trees[k][i][j].lines = new Array(3);
							} 
						}
			
						trees[k][0][0].lines[0] = createLine(stage.getWidth() / 2, 5, (trees[k][0][0].viewRect.getX() + trees[k][0][0].viewRect.getBoxWidth() / 2), trees[k][0][0].viewRect.getY());
						trees[k][0][1].lines[0] = createLine(stage.getWidth() / 2, 5, (trees[k][0][1].viewRect.getX() + trees[k][0][1].viewRect.getBoxWidth() / 2), trees[k][0][1].viewRect.getY());
		
						for(var i = 0; i &lt; trees[k].length; ++i) { 
							for(var j = 0; j &lt; trees[k][i].length; ++j) {
								var x1 = trees[k][i][j].viewRect.getX() + trees[k][i][j].viewRect.getBoxWidth() / 2;
								var y1 = trees[k][i][j].viewRect.getY() + trees[k][i][j].viewRect.getBoxHeight(); 

								if (trees[k][i][j].leftChildNumber != -1) { 
									var x2 = trees[k][i + 1][trees[k][i][j].leftChildNumber].viewRect.getX() + trees[k][i + 1][trees[k][i][j].leftChildNumber].viewRect.getBoxWidth() / 2; 
									var y2 = trees[k][i + 1][trees[k][i][j].leftChildNumber].viewRect.getY(); 
									trees[k][i][j].lines[1] = createLine(x1, y1, x2, y2);								
									trees[k][i + 1][trees[k][i][j].leftChildNumber].lines[0] = trees[k][i][j].lines[1]; 
								} 
								
								if (trees[k][i][j].rightChildNumber != -1) { 
									var x2 = trees[k][i + 1][trees[k][i][j].rightChildNumber].viewRect.getX() + trees[k][i + 1][trees[k][i][j].leftChildNumber].viewRect.getBoxWidth() / 2; 
									var y2 = trees[k][i + 1][trees[k][i][j].rightChildNumber].viewRect.getY(); 
									trees[k][i][j].lines[2] = createLine(x1, y1, x2, y2);
									trees[k][i + 1][trees[k][i][j].rightChildNumber].lines[0] = trees[k][i][j].lines[2]; 
								}
							} 
						} 					
					} 

					function main(k, stage, layer) { 
						createNodes(k, stage, layer); 
						createEdge(k, stage);

						for(var i = 0; i &lt; trees[k].length; ++i) { 
							for(var j = 0; j &lt; trees[k][i].length; ++j) { 
								layer.add(trees[k][i][j].viewRect);
							} 
						}
			
						for(var i = 0; i &lt; trees[k].length; ++i) { 
							for(var j = 0; j &lt; trees[k][i].length; ++j) { 
								createEventDrag(trees[k][i][j].viewRect, trees[k][i][j].lines, layer);
							} 
						}					

						for(var i = 0; i &lt; trees[k].length - 1; ++i) { 
							for(var j = 0; j &lt; trees[k][i].length; ++j) { 
								for(var c = 0; c &lt; trees[k][i][j].lines.length; ++c) { 
									if (trees[k][i][j].lines[c] != null) { 
										layer.add(trees[k][i][j].lines[c]);
									} 
								} 
							} 
						}
					} 

					window.onload = function() {
						<xsl:apply-templates select="tree" mode = "code"/>
    				};
				</script>
			</head>
			<body>
			    <h1> <center> MGRA TREE, beta version </center> </h1>
				<xsl:apply-templates select="tree" mode = "createForm"/>
   			    <xsl:apply-templates select="tree/row/cell/genome"/>	
				<xsl:apply-templates select="tree/row/cell/transformations"/>
         		<footer>
				<hr/>
				MGRA 1.0 &#169; 2008,09 by Max Alekseyev
				</footer>
			</body>
		</html>
	</xsl:template>

	<xsl:template match="tree" mode = "array">
		[
			<xsl:apply-templates select="row"/>
		], 
    </xsl:template>

	<xsl:template match="tree" mode = "code">
			var stage = createStage("tree<xsl:value-of select="number"/>", 99 * getClientWidth() / 100,  getClientHeight() / 2);
			var layer = new Kinetic.Layer();
			main(<xsl:value-of select="number"/>, stage, layer);
			stage.add(layer);
    </xsl:template>

	<xsl:template match="tree" mode = "createForm">
			<div id="tree{./number}"></div>  	
    </xsl:template>


  	 <xsl:template match="row">
        	[
				<xsl:apply-templates select="cell"/>
			],
    </xsl:template>

	 <xsl:template match="cell">
			{	
				viewRect: null, 
				lines: null,
		  		leftChildNumber: <xsl:value-of select="leftChildNumber"/>,
				rightChildNumber: <xsl:value-of select="rightChildNumber"/>,
				text: "<xsl:value-of select="text"/>"
			},
    </xsl:template>
	
	<xsl:template match="transformations">
        <div id="trs{../text}" style="display:none;">
            <h3>Transformations for <xsl:value-of select="../text"/></h3>
            <xsl:apply-templates select="transformation"/>
        </div>
    </xsl:template>

	<xsl:template match="transformation">
        <xsl:apply-templates select="before/chromosome">
            <xsl:sort select="id" data-type="number"/>
        </xsl:apply-templates>
        <xsl:apply-templates select="end"/>
        <br/>
        <xsl:apply-templates select="after/chromosome">
            <xsl:sort select="id" data-type="number"/>
        </xsl:apply-templates>
        <br/>
    </xsl:template>

    <xsl:template match="genome">
        <div id="gen{../text}" style="display:none;">
            <h3>Chromosomes for <xsl:value-of select="../text"/></h3>
            <xsl:apply-templates select="chromosome"/>
        </div>
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

	<xsl:template match="text">
		'<xsl:value-of select="."/>',
	</xsl:template>

	<xsl:template match="number">
		'<xsl:value-of select="."/>',
	</xsl:template>
</xsl:stylesheet>
