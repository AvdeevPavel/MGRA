package ru.spbau.bioinf.mgra.Parser;

import org.jdom.Element;
import ru.spbau.bioinf.mgra.DataFile.BlocksInformation;
import ru.spbau.bioinf.mgra.DataFile.Config;
import ru.spbau.bioinf.mgra.Server.XmlUtil;

import java.io.File;
import java.util.ArrayList;

public class Tree {
    private Node root;
    private ArrayList<Element> elementOfLevel;
    private int idTree = 0;

    public Tree(String s, int id) {
         idTree = id;
         root = new Node(null, s);
         calculateInformation();
    }

    public Tree(String s) {

    }

    public void setIdTree(int id) {
        idTree = id;
    }

    public void calculateInformation() {
        root.evaluateHeight();
        evaluateNumberNodeOnLevel(root.getCurrentMaxHeight());
    }

    public boolean merge(Tree mergeTree) {
        return root.merge(mergeTree.root);
    }

    public boolean isFullTree(int numberGenomes) {
        return ((root.calculateBranch() - 1) == 2 * numberGenomes - 3);
    }

    public boolean isCompleteTransformation(String path) {
        return root.isCompleteTransformation(path);
    }

    public void createBranches(ArrayList<Branch> branches) {
        root.createBranches(root.getData(), branches);
        if (root.getChild() != null) {
            branches.add(new Branch(root.getChild().getData(), root.getData()));
        }
    }

    public Element toXml(String path, Config config, BlocksInformation blocksInformation) {
        int height = root.getCurrentMaxHeight();
        elementOfLevel = new ArrayList<Element>(height + 1);
        for(int i = 0; i <= height; ++i) {
            elementOfLevel.add(new Element("row"));
        }

        Element tree = new Element("tree");
        XmlUtil.addElement(tree, "number", idTree);

        root.addCells(elementOfLevel, path, config, blocksInformation);

        for(int i = 1; i <= root.getCurrentMaxHeight(); ++i) {
            tree.addContent(elementOfLevel.get(i));
        }
        return tree;
    }

    private void evaluateNumberNodeOnLevel(int height) {
        int[] countNodesOnLevel = new int[height + 1];
        countNodesOnLevel[0] = -1;
        root.evaluateNumberNodeOnLevel(countNodesOnLevel);
    }

    @Override
    public String toString() {
        return root.toString();
    }
}

