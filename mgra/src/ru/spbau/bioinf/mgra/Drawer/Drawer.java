package ru.spbau.bioinf.mgra.Drawer;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.font.TextAttribute;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.AttributedString;
import java.util.Hashtable;
import java.util.Map;


public class Drawer {
    private Graphics2D graphics;
    private BufferedImage image;

    private int heightBlockPol = 0;

    private final static int heightBlog = 60;
    private final static int minWidthBlog = 60;
    private final static int heightTriangle = 20;
    private final static int boundPol = minWidthBlog + heightTriangle;
    private final static int indent = 10;
    private final static int sizeFontInBlock = 18;

    private final static int nPointsPol = 5;

    public Drawer(String inputFormat, int countChromosome, long maxLengthChromosome) {
        if (inputFormat.equals("grimm")) {
            int heightImage = (heightBlog  + indent) * countChromosome;
            int widthImage = (minWidthBlog + heightTriangle) * (int) maxLengthChromosome;

            image = new BufferedImage(widthImage, heightImage, BufferedImage.TYPE_INT_RGB);
            graphics = image.createGraphics();
            graphics.setColor( Color.WHITE );
            graphics.fillRect(0, 0, widthImage, heightImage);
        } else {
            System.out.println("to do");
        }

    }

    /*public void initPictureInGrimmFormat(int countChromosome, int maxLengthChromosome, File reconstructGenomeInformation) {
        int heightImage = (heightBlog  + indent) * countChromosome;
        int widthImage = (minWidthBlog + heightTriangle) * maxGeneInChromosome;

        image = new BufferedImage(widthImage, heightImage, BufferedImage.TYPE_INT_RGB);
        graphics = image.createGraphics();
        graphics.setColor( Color.WHITE );
        graphics.fillRect(0, 0, widthImage, heightImage);
        drawChromosomeGrimmFormat(reconstructGenomeInformation);
    }

    public void initPictureForInferCarsFormat(int CountChromosome, int maxLengthInChromosome, File reconstructGenomeInformation, File cfg_file, File genomeInf) {
        ;
    }

    public void drawChromosomeGrimmFormat(File reconstructGenomeInformation) {
        int numberOfChromosome = 0;

    }

    public void drawChromosomeInferCarsFormat(File reconstructGenomeInformation, File cfg_file, File genomeInf) {
    } */

    public void writeInPng(String nameFile) {
        try {
            ImageIO.write(image, "png", new File(nameFile + ".png"));
        } catch (IOException e) {
            e.printStackTrace(); //loggin addd
        }

    }

    private void drawGene(String input, Color color, int  startX, int startY, char direction) {
        if (direction == '+') {
            drawPlusDirPolygon(color, startX, startY);
            Font font = new Font("Calibri", Font.BOLD, sizeFontInBlock);
            graphics.setFont(font);
            FontMetrics fontMetrics = graphics.getFontMetrics();
            drawStringInBlog(input, font, startX + (minWidthBlog /*+ heightTriangle*/) / 2 - fontMetrics.stringWidth(input) / 2, startY + heightBlog / 2 + fontMetrics.getHeight() / 3);
        } else if (direction == '-') {
            drawMinusDirPolygon(color, startX, startY);
            Font font = new Font("Calibri", Font.BOLD, sizeFontInBlock);
            graphics.setFont(font);
            FontMetrics fontMetrics = graphics.getFontMetrics();
            drawStringInBlog(input, font, startX + /*(*/heightTriangle + minWidthBlog/*)*/ / 2 - fontMetrics.stringWidth(input) / 2, startY + heightBlog / 2 + fontMetrics.getHeight() / 3);
        }
    }

    private void drawString(String input, Font font, int startX, int startY) {
        graphics.setColor(Color.BLACK);
        graphics.setFont(font);
        graphics.drawString("input", startX, startY);
    }

    private void drawStringInBlog(String input, Font font, int startX, int startY) {
        graphics.setColor(Color.BLACK);
        AttributedString as = new AttributedString(input);
        as.addAttribute(TextAttribute.FONT, font);
        as.addAttribute(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
        graphics.drawString(as.getIterator(), startX, startY);
    }

    private void drawPlusDirPolygon(Color color, int startX, int startY) {
        graphics.setColor(color);
        int[] xLine = {startX, startX + minWidthBlog, startX + boundPol, startX + minWidthBlog, startX};
        int[] yLine = {startY, startY, startY + heightBlog / 2, startY + heightBlog, startY + heightBlog};
        graphics.fillPolygon(xLine, yLine, nPointsPol);
    }

    private void drawMinusDirPolygon(Color color, int startX, int startY) {
        graphics.setColor(color);
        int[] xLine = {startX, startX + heightTriangle, startX + boundPol, startX + boundPol, startX + heightTriangle};
        int[] yLine = {startY + heightBlog / 2, startY, startY, startY + heightBlog, startY + heightBlog};
        graphics.fillPolygon(xLine, yLine, nPointsPol);
    }

    public static void main(String[] args) throws IOException {
        //Drawer picture = new Drawer();
        //picture.write("test.png");
    }
}