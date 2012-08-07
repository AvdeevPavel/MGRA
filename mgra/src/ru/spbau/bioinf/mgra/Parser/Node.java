package ru.spbau.bioinf.mgra.Parser;

import org.jdom.Element;
import ru.spbau.bioinf.mgra.DataFile.BlocksInformation;
import ru.spbau.bioinf.mgra.DataFile.Config;
import ru.spbau.bioinf.mgra.Server.XmlUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Node {
    private Node parent;
    private List<Node> children = new ArrayList<Node>();

    private String root = "";
    private int height = 0;
    private int numberOnLevel = 0;

    @Override
    public String toString() {
        if (children.isEmpty()) {
            return root;
        } else {
            return "(" + children.get(0).toString() + "," + children.get(1).toString() + ")";
        }
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

    public void addCells(ArrayList<Element> elementsOfLevel, String path, Config config, BlocksInformation blocksInformation) {
        for(Node child: children) {
            child.addCell(elementsOfLevel.get(child.height), path, config, blocksInformation);
            child.addCells(elementsOfLevel, path, config, blocksInformation);
        }
    }

    public boolean isCompleteTransformation(String path) {
        if (parent != null) {
            File gen = new File(path, root + ".gen");
            if (!gen.exists()) {
                return false;
            }
        }

        for(Node child: children) {
            if (!child.isCompleteTransformation(path)) {
                return false;
            }
        }

        return true;
    }

    public boolean merge(Node mergeTreeRoot) {
        for(int i = 0; i < children.size(); ++i) {
            if (children.get(i).root.equals(mergeTreeRoot.root)) {
                if (children.get(i).children.isEmpty()) {
                    mergeTreeRoot.parent = this;
                    children.remove(i);
                    children.add(i, mergeTreeRoot);
                    return true;
                }
            } else {
                if (children.get(i).merge(mergeTreeRoot)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void createBranches(String fullName, ArrayList<Branch> branches) {
        if (parent != null && parent.parent != null) {
            branches.add(new Branch(root, fullName));
        }

        for(Node child: children) {
            child.createBranches(fullName, branches);
        }
    }

    public int calculateBranch() {
        int count = 0;
        for(Node child: children) {
            count += child.calculateBranch();
        }

        if (parent == null)
            return count;
        else
            return count + 1;
    }


    public void evaluateHeight() {
        if (parent == null) {
            this.height = 0;
        } else {
            this.height = parent.height + 1;
        }

        for (Node child : children) {
            child.evaluateHeight();
        }
    }

    public void evaluateNumberNodeOnLevel(int[] countNodesInLevel) {
        this.numberOnLevel = countNodesInLevel[this.height]++;
        for(Node child: children) {
           child.evaluateNumberNodeOnLevel(countNodesInLevel);
        }
    }

    public int getCurrentMaxHeight() {
        int max = this.height;
        for (Node child : children) {
            if (max < child.getCurrentMaxHeight())
                max = child.getCurrentMaxHeight();
        }
        return max;
    }

    public String getData() {
        return root;
    }

    public Node getChild() {
        if (!children.isEmpty()) {
            if (children.get(0) != null)
                return children.get(0);
        }
        return null;
    }

    private void addCell(Element parent, String path, Config config, BlocksInformation blocksInformation) {
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

        Genome genome = Transformer.createChromosomesToPNG(cell, root, path, config, blocksInformation);
        Transformer.createTransformationToPNG(cell, genome, root, path);
        parent.addContent(cell);
    }
}
