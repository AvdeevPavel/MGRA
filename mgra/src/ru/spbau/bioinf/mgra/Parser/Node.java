package ru.spbau.bioinf.mgra.Parser;

import org.apache.log4j.Logger;
import org.jdom.Element;
import ru.spbau.bioinf.mgra.Server.XmlUtil;

import java.io.BufferedReader;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Node {

    private static final Logger log = Logger.getLogger(Node.class);

    private Node parent;

    private String root = "";

    private List<Node> children = new ArrayList<Node>();

    private int height = 0;
    private int numberOnLevel = 0;

    @Override
    public String toString() {
        return root;
    }

    public Node(Node parent, String s) {
        this.parent = parent;

        for (int i = 0;  i < s.length(); i++) {
             char ch = s.charAt(i);
             if (Character.isLetterOrDigit(ch)) {
                 root += ch;
             }
        }

        if (s.length() == root.length())
            return;

        if (s.startsWith("(")) {
            s = s.substring(1, s.length() - 1);
        }

        int stat = 0;
        String cur = "";
        for (int i = 0;  i < s.length(); i++) {
            char ch = s.charAt(i);
            if (ch == '(') {
                stat++;
            }
            
            if (ch == ')') {
                stat--;
            }

            if (stat == 0) {
                if (ch == ',') {
                    children.add(new Node(this, cur));
                    cur = "";
                }
            }

            if (cur.length() > 0 || ch != ',') {
                cur += ch;
            }
        }
        
        if (cur.length() > 0) {
            children.add(new Node(this, cur));
        }
    }

    private void addCell(Element parent, File dateDir) {
        Element cell = new Element("cell");
        XmlUtil.addElement(cell, "level", this.height);
        XmlUtil.addElement(cell, "numberNode", this.numberOnLevel);

        if (!children.isEmpty()) {
            if (children.get(0) != null) {
                XmlUtil.addElement(cell, "leftChildNumber", children.get(0).numberOnLevel);
            } else {
                XmlUtil.addElement(cell, "leftChildNumber", -1);
            }
            if (children.get(1) != null) {
                XmlUtil.addElement(cell, "rightChildNumber", children.get(1).numberOnLevel);
            } else {
                XmlUtil.addElement(cell, "rightChildNumber", -1);
            }
        } else {
            XmlUtil.addElement(cell, "leftChildNumber", -1);
            XmlUtil.addElement(cell, "rightChildNumber", -1);
        }

        XmlUtil.addElement(cell, "text", this.root);
        Genome genome = new Genome();
        try {
           BufferedReader input = TreeReader.getBufferedInputReader(new File(dateDir, root + ".gen"));
           String s;
           int count = 0;
           while ((s = input.readLine())!=null) {
                s = s.trim();
                if (!s.startsWith("#") && s.length() > 0) {
                      count++;
                      genome.addChromosome(new Chromosome(count, s));
                }
           }
           cell.addContent(genome.toXml());
        } catch (Exception e) {
           log.error("Problems with " + root + ".gen file.", e);
        }
        try {
           BufferedReader input = TreeReader.getBufferedInputReader(new File(dateDir, root + ".trs"));
           List<Transformation> transformations = new ArrayList<Transformation>();
           String s;
         
           while ((s = input.readLine())!=null) {
               transformations.add(new Transformation(s));
           }

           for (Transformation transformation : transformations) {
              transformation.update(genome);
           }
                
           XmlUtil.addElement(cell, "length", transformations.size());
           Element trs = new Element("transformations");
                
           for (Transformation transformation : transformations) {
               trs.addContent(transformation.toXml());
           }
                
           cell.addContent(trs);
        } catch (Exception e) {
           log.error("Problems with " + root + ".trs file.", e);
        }
        parent.addContent(cell);
    }

    public void addCells(ArrayList<Element> elementsOfLevel, File dateDir) {
            if (!children.isEmpty()) {
                for(Node child: children) {
                    child.addCell(elementsOfLevel.get(child.height), dateDir);
                    child.addCells(elementsOfLevel, dateDir);
                }
            }
    }

    public void evaluateHeight() {
        if (parent == null) {
            this.height = 0;
        } else {
            this.height = parent.height + 1;
        }
        if (!children.isEmpty()) {
            for (Node child : children) {
                child.evaluateHeight();
            }
        }
    }

    public void evaluateNumberNodeOnLevel(int[] countNodesInLevel) {
        this.numberOnLevel = countNodesInLevel[this.height]++;
        if (!children.isEmpty()) {
           for(Node child: children) {
              child.evaluateNumberNodeOnLevel(countNodesInLevel);
           }
        }
    }

    public int getCurrentMaxHeight() {
        if (!children.isEmpty()) {
            int max = this.height;
            for (Node child : children) {
                if (max < child.getCurrentMaxHeight())
                        max = child.getCurrentMaxHeight();
            }
            return max;
        } else {
            return this.height;
        }
    }
}
