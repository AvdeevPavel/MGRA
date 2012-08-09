package ru.spbau.bioinf.mgra.Tree;

import org.jdom.Element;
import ru.spbau.bioinf.mgra.Drawer.CreatorInformation;
import ru.spbau.bioinf.mgra.Server.XmlUtil;

import java.util.ArrayList;
import java.util.Collections;

public class Tree {
    private Node root;
    private ArrayList<Element> elementOfLevel;
    private int idTree = 0;
    private int height = 0;

    public Tree(String s, int id) {
         idTree = id;
         root = new Node(s);
         normalize();
    }

    public Tree(ArrayList<Branch> inputSet, int id) {
        idTree = id;

        Collections.sort(inputSet, Collections.reverseOrder());

        root = new Node(inputSet.get(0).getAllSet());

        for(Branch branch: inputSet) {
            root.addChild(new Node(branch.getFirstSet()));
        }

        normalize();
    }

    public boolean merge(Tree mergeTree) {
        boolean flag = root.merge(mergeTree.root);
        if (flag == true)
            normalize();
        return flag;
    }

    public boolean isFullTree(int numberGenomes) {
        return ((root.calculateBranch() - 1) == 2 * numberGenomes - 3);
    }

    public boolean isCompleteTransformation(CreatorInformation creator) {
        return root.isCompleteTransformation(creator);
    }

    public void createBranches(ArrayList<Branch> branches) {
        root.createBranches(root.getDataSet(), branches);
        branches.add(root.createRootBranch());
    }

    public Element toXml(CreatorInformation genomes) {
        elementOfLevel = new ArrayList<Element>(this.height + 1);
        for(int i = 0; i <= this.height; ++i) {
            elementOfLevel.add(new Element("row"));
        }

        Element tree = new Element("tree");
        XmlUtil.addElement(tree, "number", idTree);

        root.addCells(elementOfLevel, genomes);

        for(int i = 1; i <= root.getCurrentMaxHeight(); ++i) {
            tree.addContent(elementOfLevel.get(i));
        }
        return tree;
    }

    private void normalize() {
        root.finishBuilding();
        root.setParent(null);
        root.evaluateHeight();

        height = root.getCurrentMaxHeight();

        int[] countNodesOnLevel = new int[height + 1];
        countNodesOnLevel[0] = -1;
        root.evaluateNumberNodeOnLevel(countNodesOnLevel);
    }

    @Override
    public String toString() {
        return root.toString();
    }
}

