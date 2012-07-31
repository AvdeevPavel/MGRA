package ru.spbau.bioinf.mgra.Drawer;


import ru.spbau.bioinf.mgra.Parser.Chromosome;
import ru.spbau.bioinf.mgra.Parser.Gene;
import ru.spbau.bioinf.mgra.Parser.Genome;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.font.TextAttribute;
import java.awt.image.BufferedImage;
import java.io.*;
import java.text.AttributedString;
import java.util.*;
import java.util.List;

public class Drawer {
    private Graphics2D graphics;
    private BufferedImage image;

    private final static int heigthBlock = 60;

    private static int widthPolygone = 90;

    private final static int indent = 10;

    private final static int sizeFontInBlock = 18;
    private final static int sizeFontString = 32;

    private final static int nPointsPol = 5;

    private static double step = 0;

    private static double threshold = 0.0085;      //configure for server

    /*color RGB*/
    private int red = 0;
    private int green = 0;
    private int blue = 0;

    private void init(int widthImage, int heigthImage) {
        image = new BufferedImage(widthImage, heigthImage, BufferedImage.TYPE_INT_RGB);
        graphics = image.createGraphics();
        graphics.setColor( Color.WHITE );
        graphics.fillRect(0, 0, widthImage, heigthImage);
    }

    private int calculateWidth(List<Gene> genes) {
        step = (double) widthPolygone / threshold;
        int length = 100;
        for(Gene gene: genes) {
            if (gene.getPercent() > threshold) {
                length += new Double(step * gene.getPercent()).intValue() + 1;
            } else {
                length += widthPolygone;
            }
            ++length;
        }
        return length;
    }

    public Drawer (int widthImage, int heigthImage) {
        init(widthImage, heigthImage);
    }

    public Drawer(String inputFormat, String nameGenome, Genome genome){
        if (inputFormat.equals("grimm")) {
            int widthImage = widthPolygone * genome.getMaxCountGeneInChromosome() + 50;
            int heigthImage = (heigthBlock + indent) * genome.countOfChromosomes();
            init(widthImage, heigthImage);
            drawGenomeInGrimmFormat(genome.getChromosomes());
        } else {
            genome.setLengthInBlock(nameGenome);
            genome.setPercentInBlocks(genome.getMaxLengthChromosome());
            int widthImage = calculateWidth(genome.getMaxChromosome().getGenes());
            int heigthImage = (heigthBlock + indent) * genome.countOfChromosomes();
            init(widthImage, heigthImage);
            drawGenomeInInferCarsFormat(genome.getChromosomes());
        }
    }

    private void drawGenomeInGrimmFormat(List<Chromosome> chromosomes) {
        Integer numberOfChromosome = 1;
        HashMap<String, Color> map = new HashMap<String, Color>();
        int topStartY = 0;

        Font font = new Font("Times new roman", Font.BOLD, sizeFontString);
        graphics.setFont(font);
        FontMetrics fontMetrics = graphics.getFontMetrics();

        int bottomStartY = heigthBlock /2 + fontMetrics.getHeight() / 4;

        for(Chromosome chromosome: chromosomes) {
            drawString(numberOfChromosome.toString() + ".", font, 0, bottomStartY);
            List<Gene> genes = chromosome.getGenes();

            int startX = fontMetrics.stringWidth(numberOfChromosome.toString() + ".") + 5;

            for(Gene gene: genes) {
                if (map.get(gene.getId()) == null) {
                    Color color = nextColor();
                    drawGene(gene.getId(), null, color, startX, topStartY, widthPolygone, gene.getCharDirection());
                    map.put(gene.getId(), color);
                } else {
                    drawGene(gene.getId(), null, map.get(gene.getId()), startX, topStartY, widthPolygone, gene.getCharDirection());
                }
                startX += (widthPolygone + 1);
            }

            topStartY += (heigthBlock + indent);
            bottomStartY += (heigthBlock + indent);
            ++numberOfChromosome;
        }
    }

    private void drawGenomeInInferCarsFormat(List<Chromosome> chromosomes) {
        Integer numberOfChromosome = 1;
        HashMap<String, Color> map = new HashMap<String, Color>();
        int topStartY = 0;

        Font font = new Font("Times new roman", Font.BOLD, sizeFontString);
        graphics.setFont(font);
        FontMetrics fontMetrics = graphics.getFontMetrics();

        int bottomStartY = heigthBlock /2 + fontMetrics.getHeight() / 2;
        for(Chromosome chromosome: chromosomes) {
            drawString(numberOfChromosome.toString() + ".", font, 5, bottomStartY);
            List<Gene> genes = chromosome.getGenes();

            int startX = fontMetrics.stringWidth(numberOfChromosome.toString() + ".") + 5;
            for(Gene gene: genes) {
                int poly = 0;
                if (gene.getPercent() > threshold) {
                    poly = new Double(step * gene.getPercent()).intValue() + 1;
                } else {
                    poly = widthPolygone;
                }

                Color color;
                if (map.get(gene.getId()) == null) {
                    color = nextColor();
                    map.put(gene.getId(), color);
                } else {
                    color = map.get(gene.getId());
                }

                drawGene(gene.getId(), parseLength(gene.getLength()), color, startX, topStartY, poly, gene.getCharDirection());


                startX += (poly + 1);
            }
            topStartY += (heigthBlock + indent);
            bottomStartY += (heigthBlock + indent);
            ++numberOfChromosome;
        }
    }

    public void writeInPng(String nameFile) {
        try {
            ImageIO.write(image, "png", new File(nameFile + ".png"));
        } catch (IOException e) {
            e.printStackTrace();   //add loger
        }

    }

    private String parseLength(long length) {
        String output = "";
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
        blue += 10;
        if (blue > 255) {
            blue = 0;
            green += 10;
            if (green > 255) {
                green = 0;
                red += 10;
                if (red > 255) {
                    red = 0;
                }
            }
        }

        return new Color(red, green, blue);
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

        int startXText = startX + del * widthPoly / 8 - fontMetrics.stringWidth(id) / 2;;
        int startYText = 0;

        if (length != null) {
            startYText = startY + heigthBlock / 2 - fontMetrics.getHeight() / 3;
            drawStringInBlog(length, font, startX + del * widthPoly / 8 - fontMetrics.stringWidth(length) / 2, startYText + fontMetrics.getHeight());
        } else {
            startYText = startY + heigthBlock / 2 + fontMetrics.getHeight() / 3;
        }

        drawStringInBlog(id, font, startXText, startYText);
    }

    private void drawString(String input, Font font, int startX, int startY) {
        graphics.setColor(Color.BLACK);
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

    public static void main(String[] args) {
        Drawer apr = new Drawer(2000, 500);
    }
}