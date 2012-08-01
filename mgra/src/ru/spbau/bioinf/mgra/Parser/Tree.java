package ru.spbau.bioinf.mgra.Parser;

import org.jdom.Element;
import ru.spbau.bioinf.mgra.Server.XmlUtil;

import java.io.File;
import java.util.ArrayList;

public class Tree {
    private Node root;
    private ArrayList<Element> elementOfLevel;
    private int idTree = 0;
    private boolean valid = true;

    public Tree(String s, int id) {
         idTree = id;
         root = new Node(null, s);

         root.evaluateHeight();
         int height = root.getCurrentMaxHeight();
         evaluateNumberNodeOnLevel(height);
         setValid(s);

         elementOfLevel = new ArrayList<Element>(height + 1);
         for(int i = 0; i <= height; ++i) {
              elementOfLevel.add(new Element("row"));
         }
    }

    public boolean isValid() {
        return valid;
    }

    public Element toXml(File dateDir) {
        Element tree = new Element("tree");
        XmlUtil.addElement(tree, "number", idTree);

        root.addCells(elementOfLevel, dateDir);
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

    private void setValid(String s) {
        boolean ans = true;
        boolean flag = false;
        String cur = "";
        for (int i = 0;  i < s.length(); i++) {
            char ch = s.charAt(i);
            if (Character.isLetterOrDigit(ch)) {
                 if (flag) {
                     cur += ch;
                 } else {
                     flag = true;
                     cur = "";
                 }
            } else {
                if (cur.length() > 1) {
                    ans = false;
                    break;
                }
                flag = false;
            }
        }
        valid =  ans;
    }
}

