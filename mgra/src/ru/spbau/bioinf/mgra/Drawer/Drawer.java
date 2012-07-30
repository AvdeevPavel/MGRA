package ru.spbau.bioinf.mgra.Drawer;

import ru.spbau.bioinf.mgra.DataFile.Config;
import ru.spbau.bioinf.mgra.Parser.Chromosome;
import ru.spbau.bioinf.mgra.Parser.Gene;
import ru.spbau.bioinf.mgra.Parser.Genome;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.font.TextAttribute;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.AttributedString;
import java.util.*;
import java.util.List;


public class Drawer {
    private Graphics2D graphics;
    private BufferedImage image;

    private final static int heigthBlock = 60;

    private static int widthPolygone = 80;

    private final static int indent = 10;

    private final static int sizeFontInBlock = 18;
    private final static int sizeFontString = 32;

    private final static int nPointsPol = 5;

    private static long maxLengthChromosome = 0;

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


    public Drawer(String inputFormat, Genome genome) {
        if (inputFormat.equals("grimm")) {
            int widthImage = Config.getWidthMonitor();
            int countLine = 0;
            maxLengthChromosome = genome.getMaxCountGeneInChromosome();
            if ((widthImage - 30) / (int) maxLengthChromosome > widthPolygone) {
                widthPolygone = (widthImage - 30) / (int) maxLengthChromosome;
                countLine = genome.countOfChromosomes();
            } else {
                countLine = 0;
                List<Chromosome>genome.getChromosomes()
            }
            int heigthImage = (heigthBlock + indent) * countLine;
            init(widthImage, heigthImage);
            drawGenomeInGrimmFormat(genome.getChromosomes());
        } else {
            int widthImage = Config.getWidthMonitor();
            int countLine = 0;
            maxLengthChromosome = genome.getMaxLengthChromosome(); //to do change

            int heigthImage = (heigthBlock + indent) * countLine;

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
                    drawGene(gene.getId(), color, startX, topStartY, widthPolygone, gene.getCharDirection());
                    map.put(gene.getId(), color);
                } else {
                    drawGene(gene.getId(), map.get(gene.getId()), startX, topStartY, widthPolygone, gene.getCharDirection());
                }
                startX += widthPolygone;
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

        int bottomStartY = heigthBlock /2 + fontMetrics.getHeight() / 4;

        for(Chromosome chromosome: chromosomes) {
            drawString(numberOfChromosome.toString() + ".", font, 0, bottomStartY);
            List<Gene> genes = chromosome.getGenes();

            int startX = fontMetrics.stringWidth(numberOfChromosome.toString() + ".") + 5;

            for(Gene gene: genes) {
                int poly = (int) ((double) (Config.getWidthMonitor() - 30) / (double) (maxLengthChromosome) * gene.getLength()) + 1;

                if (map.get(gene.getId()) == null) {
                    Color color = nextColor();
                    drawGene(gene.getId(), color, startX, topStartY, poly, gene.getCharDirection());
                    map.put(gene.getId(), color);
                } else {
                    drawGene(gene.getId(), map.get(gene.getId()), startX, topStartY, poly, gene.getCharDirection());
                }
                startX += poly;
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
            e.printStackTrace(); //loggin addd
        }

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

    private void drawGene(String input, Color color, int  startX, int startY, int widthPoly, char direction) {
        if (direction == '+') {
            drawPlusDirPolygon(color, startX, startY, widthPoly);
            Font font = new Font("Calibri", Font.BOLD, sizeFontInBlock);
            graphics.setFont(font);
            FontMetrics fontMetrics = graphics.getFontMetrics();
            drawStringInBlog(input, font, startX + 3 * widthPoly / 8 - fontMetrics.stringWidth(input) / 2, startY + heigthBlock / 2 + fontMetrics.getHeight() / 3);
        } else if (direction == '-') {
            drawMinusDirPolygon(color, startX, startY, widthPoly);
            Font font = new Font("Calibri", Font.BOLD, sizeFontInBlock);
            graphics.setFont(font);
            FontMetrics fontMetrics = graphics.getFontMetrics();
            drawStringInBlog(input, font, startX + 5 * widthPoly / 8 - fontMetrics.stringWidth(input) / 2, startY + heigthBlock / 2 + fontMetrics.getHeight() / 3);
        }
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
}