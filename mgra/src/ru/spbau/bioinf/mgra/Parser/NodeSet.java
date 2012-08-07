package ru.spbau.bioinf.mgra.Parser;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class NodeSet {
    private NodeSet parent;
    private List<NodeSet> children = new ArrayList<NodeSet>();
    private HashSet<Character> data = new HashSet<Character>();

    public NodeSet(HashSet<Character> data_, NodeSet parent_) {
        data = data_;
        parent = parent_;
    }

    public void addChild(NodeSet newChildFirst) {
        if (newChildFirst.data.equals(data))
            return;

        for(NodeSet child: children) {
            if (child.data.containsAll(newChildFirst.data)) {
                child.addChild(newChildFirst);
                return;
            }
        }

        HashSet<Character> tmp = new HashSet<Character>(data);
        tmp.removeAll(newChildFirst.data);
        NodeSet newChildSecond = new NodeSet(tmp, this);
        newChildFirst.parent = this;

        ArrayList<NodeSet> oldChildren = new ArrayList<NodeSet>(children);

        children.clear();
        children.add(newChildFirst);
        children.add(newChildSecond);

        for(NodeSet child: oldChildren) {
            if (newChildFirst.data.containsAll(child.data)) {
               newChildFirst.children.add(child);
            } else if (newChildSecond.data.containsAll(child.data)) {
                newChildSecond.children.add(child);
            } else {
                HashSet<Character> temp = new HashSet<Character>(child.data);
                temp.removeAll(newChildFirst.data);
                NodeSet nodeSecond = new NodeSet(temp, newChildSecond);
                newChildSecond.children.add(nodeSecond);
                temp = new HashSet<Character>(child.data);
                temp.removeAll(newChildSecond.data);
                NodeSet nodeFirst = new NodeSet(temp, newChildFirst);
                newChildFirst.children.add(nodeFirst);
                child.refractor(nodeFirst, nodeSecond);
            }
        }
    }

    public void refractor(NodeSet firstParent, NodeSet secondParent) {
        for(NodeSet child: children) {
            if (firstParent.data.containsAll(child.data)) {
                firstParent.children.add(child);
            } else if (secondParent.data.containsAll(child.data)) {
                secondParent.children.add(child);
            } else {
                HashSet<Character> temp = new HashSet<Character>(child.data);
                temp.removeAll(firstParent.data);
                NodeSet nodeSecond = new NodeSet(temp, secondParent);
                secondParent.children.add(nodeSecond);
                temp = new HashSet<Character>(child.data);
                temp.removeAll(secondParent.data);
                NodeSet nodeFirst = new NodeSet(temp, firstParent);
                child.refractor(nodeFirst, nodeSecond);
                firstParent.children.add(nodeFirst);
            }
        }
    }

    public String toString() {
        String ans = "";

        if (children.isEmpty()) {
            for(Character ch: data) {
                ans += ch;
            }
        } else {
           ans = "(";
           for(NodeSet child: children) {
               ans += child.toString() + " ";
           }
           ans += ")";
        }
        return ans;

    }
}
