package ru.spbau.bioinf.mgra.Tree;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class NodeSet {
    private List<NodeSet> children = new ArrayList<NodeSet>();
    private HashSet<Character> data = null;

    public NodeSet(char c) {
        data = new HashSet<Character>();
        data.add(c);
    }

    public NodeSet(HashSet<Character> data_) {
        data = new HashSet<Character>(data_);
    }

    public NodeSet(HashSet<Character> first, HashSet<Character> second) {
        data = new HashSet<Character>(first);
        data.removeAll(second);
    }

    public void addChild(NodeSet nodeFirst) {
        if (nodeFirst.data.equals(data)) {
            return;
        }

        for(NodeSet child: children) {
            if (child.data.containsAll(nodeFirst.data)) {
                child.addChild(nodeFirst);
                return;
            }
        }

        NodeSet nodeSecond = new NodeSet(data, nodeFirst.data);

        this.refractor(nodeFirst, nodeSecond);

        children.clear();
        children.add(nodeFirst);
        children.add(nodeSecond);
    }

        public void addSet(HashSet<Character> addSet) {
        data.addAll(addSet);
    }

    public void finishBuilding() {
        if (data.size() == 2 && children.isEmpty())  {
            for (Character ch: data) {
                children.add(new NodeSet(ch));
            }
        } else {
            for(NodeSet child: children) {
                child.finishBuilding();
            }
        }
    }

    public String dataToString() {
        String ans = "";
        for(Character ch: data) {
            ans += ch;
        }
        return  ans;
    }

    public List<NodeSet> getChildren() {
        return children;
    }

    private void refractor(NodeSet firstParent, NodeSet secondParent) {
        for(NodeSet child: children) {
            if (firstParent.data.containsAll(child.data)) {
                firstParent.children.add(child);
            } else if (secondParent.data.containsAll(child.data)) {
                secondParent.children.add(child);
            } else {
                NodeSet nodeFirst = new NodeSet(child.data, secondParent.data);
                NodeSet nodeSecond = new NodeSet(child.data, firstParent.data);
                firstParent.children.add(nodeFirst);
                secondParent.children.add(nodeSecond);
                child.refractor(nodeFirst, nodeSecond);
            }
        }
        children.clear();
    }

    public String toString() {
        String ans = "";

        if (children.isEmpty()) {
            for(Character ch: data) {
                ans += ch;
            }
        } /*else if (children.size() == 1) {
            for(Character ch: children.get(0).data) {
                ans += ch;
            }
        } */ else {
           ans = "(";
           for(int i = 0; i < children.size(); ++i) {
                ans += children.get(i).toString();
                if (i != children.size() - 1) {
                    ans += ",";
                }
           }
           ans += ")";
        }
        return ans;

    }
}
