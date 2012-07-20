package ru.spbau.bioinf.mgra.Parser;

import org.jdom.Element;
import ru.spbau.bioinf.mgra.Server.XmlUtil;

import java.io.File;
import java.util.ArrayList;

public class Tree {
    private Node root;
    private ArrayList<Element> elementOfLevel;
    private static int countTree = 0;

    public Tree(String s) {
         root = new Node(null, s);
         root.evaluateHeight();
         int height = root.getCurrentMaxHeight();
         int[] countNodesOnLevel = new int[height + 1];
         countNodesOnLevel[0] = -1;
         root.evaluateNumberNodeOnLevel(countNodesOnLevel);
         elementOfLevel = new ArrayList<Element>(height + 1);
         for(int i = 0; i <= height; ++i) {
              elementOfLevel.add(new Element("row"));
         }
         countTree = 0;
    }

    public Element toXml(File dateDir) {
        Element tree = new Element("tree");
        XmlUtil.addElement(tree, "number", countTree++);

        root.addCells(elementOfLevel, dateDir);
        for(int i = 1; i <= root.getCurrentMaxHeight(); ++i) {
            tree.addContent(elementOfLevel.get(i));
        }
        return tree;
    }
}

