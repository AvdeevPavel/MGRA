package ru.spbau.bioinf.mgra.Drawer;

import org.apache.log4j.Logger;
import ru.spbau.bioinf.mgra.Parser.*;
import ru.spbau.bioinf.mgra.Server.JettyServer;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.font.TextAttribute;
import java.awt.image.BufferedImage;
import java.io.*;
import java.text.AttributedString;
import java.util.*;
import java.util.List;

public class Drawer {
    private static final Logger log = Logger.getLogger(JettyServer.class);

    private Graphics2D graphics;
    private BufferedImage image;

    private final static int heigthBlock = 38;
    private static int widthPolygone = 76;

    private final static int indent = 5;

    private final static int sizeFontInBlock = 12;
    private final static int sizeFontString = 22;

    private final static int nPointsPol = 5;

    private final static int bound = 2;

    private static double step = 0;

    private static double threshold = 0.0085; //configure for server: reduse - bigger memory(RAM), increase - lower memory(RAM)

    private final static Color[] colorForTransformation = {Color.GREEN, Color.RED, new Color(0, 255, 0), new Color(255, 52, 179)};

    /*color RGB*/
    private int red = 45;
    private int green = 0;
    private int blue = 0;

    private void init(int widthImage, int heigthImage) throws OutOfMemoryError, NegativeArraySizeException {
        log.debug("Posted buffered image width = " + widthImage + " height = " + heigthImage);
        image = new BufferedImage(widthImage, heigthImage, BufferedImage.TYPE_INT_RGB);
        graphics = image.createGraphics();
        graphics.setColor( Color.WHITE );
        graphics.fillRect(0, 0, widthImage, heigthImage);
    }

    public Drawer(String inputFormat, Genome genome) throws OutOfMemoryError, NegativeArraySizeException {
        int widthImage;
        int heigthImage = (heigthBlock + indent) * genome.getNumberOfChromosomes() - 3 * bound;

        if (inputFormat.equals("grimm")) {
            log.debug("Create image in grimm format");
            widthImage = (widthPolygone + 1) * (int) genome.getLengthMaxLengthOfChromosomes() + 50;
        } else {
            log.debug("Create image in infercars format");
            widthImage = calculateWidth(genome.getMaxLengthOfChromosome());
        }

        init(widthImage, heigthImage);
        drawChromosomes(bound, genome.getChromosomes(), inputFormat);
    }

    public Drawer(String inputFormat, ArrayList<Transformation> transformations) throws OutOfMemoryError, NegativeArraySizeException {
        int widthImage;
        int heigthImage = 0;

        for(Transformation transformation: transformations) {
            heigthImage += ((heigthBlock + indent) * transformation.getSizeBeforeChromosome() - indent) +
                    ((heigthBlock + indent) * transformation.getSizeAfterChromosome() - indent) + 2 * heigthBlock;
        }
        heigthImage -= (heigthBlock - bound);

        if (inputFormat.equals("grimm")) {
            log.debug("Create image in grimm format");
            widthImage = (int) ((widthPolygone + 1) * Transformation.getLengthMaxLengthOfChromosomes(transformations) + 300);
        } else {
            log.debug("Create image in infercars format");
            widthImage = calculateWidth(Transformation.getMaxLengthOfChromosome(transformations)) + 300;
        }

        init(widthImage, heigthImage);
        drawTransformations(transformations, inputFormat);
    }

    public void writeInPng(String nameFile) throws IOException {
            ImageIO.write(image, "png", new File(nameFile + ".png"));
            log.debug("Create image with genome " + nameFile);
    }

    public boolean isBigImage(int widthMonitor) {
        return (image.getWidth() > widthMonitor);
    }

    public static void setThreshold(double coef) {
        threshold = coef;
    }

    private int calculateWidth(Chromosome chromosome) {
        if (chromosome == null) {
            return 0;
        }
        List<Gene> genes = chromosome.getGenes();
        step = (double) widthPolygone / threshold;
        int length = 100;
        for(Gene gene: genes) {
            if (gene.getPercent() > threshold) {
                length += new Double(step * gene.getPercent()).intValue() + 1 + 1;
            } else {
                length += (widthPolygone + 1);
            }
            ++length;
        }
        return length;
    }

    private void drawTransformations(ArrayList<Transformation> transformations, String inputFormat) {
        int startY = bound;

        for(Transformation transformation: transformations) {
            startY += drawChromosomes(startY, transformation.getBeforeChromosomes(), inputFormat);
            drawEnds(transformation.getEnds(), startY);
            startY += heigthBlock;
            startY += drawChromosomes(startY, transformation.getAfterChromosomes(), inputFormat);
            startY += heigthBlock;
        }
    }

    private int drawChromosomes(int startY, List<Chromosome> chromosomes, String inputFormat) {
        HashMap<String, Color> map = new HashMap<String, Color>();
        int topStartY = startY;

        Font font = new Font("Times new roman", Font.BOLD, sizeFontString);
        graphics.setFont(font);
        FontMetrics fontMetrics = graphics.getFontMetrics();
        int bottomStartY = startY + heigthBlock / 2 + fontMetrics.getHeight() / 4;

        for(Chromosome chromosome: chromosomes) {
            drawChromosome(chromosome, bottomStartY, topStartY, font, map, inputFormat);
            topStartY += (heigthBlock + indent);
            bottomStartY += (heigthBlock + indent);
        }

        return (topStartY - startY - indent);
    }
    
    private void drawChromosome(Chromosome chromosome, int bottomStartY, int topStartY, Font font, HashMap<String, Color> map, String inputFormat) {
        drawString(chromosome.getId() + ".", font, Color.BLACK, 0, bottomStartY);

        FontMetrics fontMetrics = graphics.getFontMetrics();
        int startX = fontMetrics.stringWidth(chromosome.getId() + ".") + 5;

        List<Gene> genes = chromosome.getGenes();
        for(Gene gene: genes) {
            int poly;
            String strLength;

            if (inputFormat.equals("grimm")) {
                poly = widthPolygone;
                strLength = null;
            } else {
                if (gene.getPercent() > threshold) {
                    poly = new Double(step * gene.getPercent()).intValue() + 1;
                } else {
                    poly = widthPolygone;
                }
                strLength = parseLength(gene.getLength());
            }

            Color color;
            if (map.get(gene.getId()) == null) {
                color = nextColor();
                map.put(gene.getId(), color);
            } else {
                color = map.get(gene.getId());
            }

            if (gene.getEnds().isEmpty()) {
                drawGene(gene.getId(), strLength, color, startX, topStartY, poly, gene.getCharDirection());
                startX += (poly + 1);
            } else {
                List<End> ends = gene.getEnds();

                for(End end: ends) {
                    if (((gene.getDirection() == Direction.PLUS) && (end.getType() == EndType.TAIL)) ||((gene.getDirection() == Direction.MINUS) && (end.getType() == EndType.HEAD))) {
                        drawString("   " + end.getType().toString(), font, colorForTransformation[end.getColor()], startX, topStartY + heigthBlock / 2 + fontMetrics.getHeight() / 4);
                        startX += fontMetrics.stringWidth("   " + end.getType().toString());
                    }
                }

                drawGene(gene.getId(), strLength, color, startX, topStartY, poly, gene.getCharDirection());
                startX += (poly + 1);

                for(End end: ends) {
                    if (((gene.getDirection() == Direction.PLUS) && (end.getType() == EndType.HEAD)) ||((gene.getDirection() == Direction.MINUS) && (end.getType() == EndType.TAIL))) {
                        drawString(end.getType().toString() + "    ", font, colorForTransformation[end.getColor()], startX, topStartY + heigthBlock / 2 + fontMetrics.getHeight() / 4);
                        startX += fontMetrics.stringWidth("   " + end.getType().toString());
                    }
                }
            }
        }
    }

    private void drawGene(String id, String length, Color color, int startX, int startY, int widthPoly, char direction) {
        int del = 0;

        if (direction == '+') {
            drawPlusDirPolygon(color, startX, startY, widthPoly);
            del = 3;
        } else if (direction == '-') {
            drawMinusDirPolygon(color, startX, startY, widthPoly);
            del = 5;
        }

        Font font = new Font("Calibri", Font.BOLD, sizeFontInBlock);
        graphics.setFont(font);
        FontMetrics fontMetrics = graphics.getFontMetrics();

        int startXText = startX + del * widthPoly / 8 - fontMetrics.stringWidth(id) / 2;
        int startYText;

        if (length != null) {
            startYText = startY + heigthBlock / 2 - fontMetrics.getHeight() / 3;
            drawStringInBlog(length, font, startX + del * widthPoly / 8 - fontMetrics.stringWidth(length) / 2, startYText + fontMetrics.getHeight());
        } else {
            startYText = startY + heigthBlock / 2 + fontMetrics.getHeight() / 3;
        }

        drawStringInBlog(id, font, startXText, startYText);
    }

    private void drawEnds(End[] ends, int startY) {
        String st = "";

        for(End end: ends) {
            st += (end.getId() + end.getType() + "  ");
        }

        Font font = new Font("Times new roman", Font.BOLD, sizeFontString);
        graphics.setFont(font);
        FontMetrics fontMetrics = graphics.getFontMetrics();

        int startX = 20;//image.getWidth() / 2  - fontMetrics.stringWidth(st) / 2;
        int topStartY = startY + heigthBlock / 2 + fontMetrics.getHeight() / 4;

        for(End end: ends) {
            drawString(end.getId() + end.getType() + "  ", font, colorForTransformation[end.getColor()], startX, topStartY);
            startX += fontMetrics.stringWidth(end.getId() + end.getType() + "  ");
        }
    }
    
    private String parseLength(long length) {
        String output;
        if (Long.toString(length).length() >= 6) {
            int res = (int) ((length / 10000) % 10);
            int residue = (int) ((length / 100000) % 10);
            length /= 1000000;
            if (res > 4) {
                ++residue;
            }
            output = Long.toString(length) + "." + Integer.toString(residue) + "M";
        } else if (Long.toString(length).length() >= 3) {
            int res = (int) ((length / 10) % 10);
            int residue = (int) ((length / 100) % 10);
            length /= 1000;
            if (res > 4) {
                ++residue;
            }
            output = Long.toString(length) + "." + Integer.toString(residue) + "K";
        } else {
            output = Long.toString(length);
        }
        return output;
    }

    private Color nextColor() {
        blue += 12;

        if (blue > 255) {
            blue = 0;
            green += 12;
            if (green > 255) {
                green = 0;
                red += 12;
                if (red > 255)
                    red = 20;
            }
        }

        return new Color(red, green, blue);
    }

    private void drawString(String input, Font font, Color color, int startX, int startY) {
        graphics.setColor(color);
        graphics.setFont(font);
        graphics.drawString(input, startX, startY);
    }

    private void drawStringInBlog(String input, Font font, int startX, int startY) {
        graphics.setColor(Color.WHITE);
        AttributedString as = new AttributedString(input);
        as.addAttribute(TextAttribute.FONT, font);
        as.addAttribute(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
        graphics.drawString(as.getIterator(), startX, startY);
    }

    private void drawPlusDirPolygon(Color color, int startX, int startY, int boundPoly) {
        graphics.setColor(color);
        int[] xLine = {startX, startX + 3 * boundPoly / 4, startX + boundPoly, startX + 3 * boundPoly / 4, startX};
        int[] yLine = {startY, startY, startY + heigthBlock / 2, startY + heigthBlock, startY + heigthBlock};
        graphics.fillPolygon(xLine, yLine, nPointsPol);
    }

    private void drawMinusDirPolygon(Color color, int startX, int startY, int boundPoly) {
        graphics.setColor(color);
        int[] xLine = {startX, startX + boundPoly / 4, startX + boundPoly, startX + boundPoly, startX + boundPoly / 4};
        int[] yLine = {startY + heigthBlock / 2, startY, startY, startY + heigthBlock, startY + heigthBlock};
        graphics.fillPolygon(xLine, yLine, nPointsPol);
    }
}
