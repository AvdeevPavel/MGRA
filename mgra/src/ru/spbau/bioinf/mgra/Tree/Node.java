package ru.spbau.bioinf.mgra.Tree;

import org.jdom.Element;
import ru.spbau.bioinf.mgra.Drawer.DrawerGenomes;
import ru.spbau.bioinf.mgra.Parser.Transformer;
import ru.spbau.bioinf.mgra.Server.XmlUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class Node {
    private Node parent;
    private List<Node> children = new ArrayList<Node>();

    private String dataString = "";
    private HashSet<Character> dataSet = new HashSet<Character>();

    private int height = 0;
    private int numberOnLevel = 0;

    public Node(char c) {
        dataSet = new HashSet<Character>();
        dataSet.add(c);
    }

    Node(String s) {
        for (int i = 0;  i < s.length(); i++) {
             char ch = s.charAt(i);
             if (Character.isLetterOrDigit(ch)) {
                 dataString += ch;
             }
        }

        if (s.length() == dataString.length())
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
                    children.add(new Node(cur));
                    cur = "";
                }
            }

           if (cur.length() > 0 || ch != ',') {
                cur += ch;
            }
        }
        if (cur.length() > 0) {
            children.add(new Node(cur));
        }
    }

    public Node(HashSet<Character> first, HashSet<Character> second) {
        dataSet = new HashSet<Character>(first);
        dataSet.removeAll(second);
    }

    public Node(HashSet<Character> data) {
        dataSet = new HashSet<Character>(data);
    }

    public void addChild(Node nodeFirst) {
        if (nodeFirst.dataSet.equals(dataSet)) {
            return;
        }

        for(Node child: children) {
            if (child.dataSet.containsAll(nodeFirst.dataSet)) {
                child.addChild(nodeFirst);
                return;
            }
        }

        Node nodeSecond = new Node(dataSet, nodeFirst.dataSet);

        this.refractor(nodeFirst, nodeSecond);

        children.clear();
        children.add(nodeFirst);
        children.add(nodeSecond);
    }

    void addCells(ArrayList<Element> elementsOfLevel, String path, DrawerGenomes genomes) {
        for(Node child: children) {
            child.addCell(elementsOfLevel.get(child.height), path, genomes);
            child.addCells(elementsOfLevel, path, genomes);
        }
    }

    boolean merge(Node mergeTreeRoot) {
        for(int i = 0; i < children.size(); ++i) {
            if (children.get(i).dataSet.equals(mergeTreeRoot.dataSet)) {
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

    boolean isCompleteTransformation(String path) {
        if (parent != null) {
            File gen = new File(path, dataString + ".gen");
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

    Branch createRootBranch() {
        if (!children.isEmpty()) {
            return new Branch(dataSet, parent.dataSet);
        }
        return null;
    }

    void createBranches(HashSet<Character> full, ArrayList<Branch> branches) {
        if (parent != null && parent.parent != null) {
            branches.add(new Branch(dataSet, full));
        }

        for(Node child: children) {
            child.createBranches(full, branches);
        }
    }

    int calculateBranch() {
        int count = 0;
        for(Node child: children) {
            count += child.calculateBranch();
        }

        if (parent == null)
            return count;
        else
            return count + 1;
    }

    /********************/
    /*normalize function*/
    /********************/
    int getCurrentMaxHeight() {
        int max = this.height;
        for (Node child : children) {
            if (max < child.getCurrentMaxHeight())
                max = child.getCurrentMaxHeight();
        }
        return max;
    }

    void evaluateHeight() {
        if (parent == null) {
            this.height = 0;
        } else {
            this.height = parent.height + 1;
        }

        for (Node child : children) {
            child.evaluateHeight();
        }
    }

    void evaluateNumberNodeOnLevel(int[] countNodesInLevel) {
        this.numberOnLevel = countNodesInLevel[this.height]++;
        for(Node child: children) {
           child.evaluateNumberNodeOnLevel(countNodesInLevel);
        }
    }

    void setParent(Node parent) {
        this.parent = parent;

        for(Node child: children) {
            child.setParent(this);
        }
    }

    void finishBuilding() {
        if (dataSet.size() == 2 && children.isEmpty())  {
            for (Character ch: dataSet) {
                children.add(new Node(ch));
            }
        } else {
            for(Node child: children) {
                child.finishBuilding();
            }
        }
    }

    void setData() {
        if (dataSet.isEmpty()) {
            for(int i = 0; i < dataString.length(); ++i) {
                dataSet.add(dataString.charAt(i));
            }
        }

        if (dataString.isEmpty()) {
            for(Character ch: dataSet) {
                dataString += ch;
            }
        }
    }

    /********/
    /*Geters*/
    /********/
    HashSet<Character> getDataSet() {
       return dataSet;
   }

    /*Other function*/
    private void addCell(Element parent, String path, DrawerGenomes genomes) {
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

        Transformer.createInformationForGenome(cell, dataString, dataSet, genomes);
        Transformer.createTransformationToPNG(cell, genomes.getGenome(dataSet), dataString, path);
        parent.addContent(cell);
    }

    private void refractor(Node firstParent, Node secondParent) {
        for(Node child: children) {
            if (firstParent.dataSet.containsAll(child.dataSet)) {
                firstParent.children.add(child);
            } else if (secondParent.dataSet.containsAll(child.dataSet)) {
                secondParent.children.add(child);
            } else {
                Node nodeFirst = new Node(child.dataSet, secondParent.dataSet);
                Node nodeSecond = new Node(child.dataSet, firstParent.dataSet);
                firstParent.children.add(nodeFirst);
                secondParent.children.add(nodeSecond);
                child.refractor(nodeFirst, nodeSecond);
            }
        }
        children.clear();
    }

    @Override
    public String toString() {
        if (children.isEmpty()) {
            return dataString;
        } else {
            String ans = "(";
            for(int i = 0; i < children.size(); ++i) {
                ans += children.get(i).toString();
                if (i != children.size() - 1) {
                    ans += ",";
                }
            }
            ans += ")";
            return ans;
        }
    }
}
