package ru.spbau.bioinf.mgra.Parser;

import java.util.*;

public class Branch implements Comparable<Branch>{
    private HashSet<Character> firstSet = new HashSet<Character>();
    private HashSet<Character> secondSet = new HashSet<Character>();
    private int weight = 0;

    private void init(String first, String second)  {
        if (first.length() > second.length()) {
            String temp = first;
            first = second;
            second = temp;
        }

        for(int i = 0; i < first.length(); ++i) {
            firstSet.add(first.charAt(i));
        }

        for(int i = 0; i < second.length(); ++i) {
            if (!firstSet.contains(second.charAt(i))) {
                secondSet.add(second.charAt(i));
            }
        }
    }

    public Branch(String first, String second) {
         init(first, second);
    }

    public Branch(String first, String second, int weight_) {
        init(first, second);
        weight = weight_;
    }

    public boolean compatibilityTo(Branch second) {
        if (firstSet.containsAll(second.firstSet)) {
            return true;
        }

        if (second.firstSet.containsAll(firstSet)) {
            return true;
        }

        if (firstSet.containsAll(second.secondSet)) {
            return true;
        }

        if (second.secondSet.containsAll(firstSet)) {
            return true;
        }

        return false;
    }

    public static boolean compatibilityAll(List<Branch> inputSet, Branch second) {

        for(Branch branch: inputSet) {
            if (!second.compatibilityTo(branch)) {
                return false;
            }
        }

        return true;
    }

    public static Tree createTree(ArrayList<Branch> inputSet) {
        Tree tree = null;

        if (inputSet != null || !inputSet.isEmpty()) {
            Collections.sort(inputSet, Collections.reverseOrder());
            HashSet<Character> tmp = new HashSet<Character>(inputSet.get(0).firstSet);
            tmp.addAll(inputSet.get(0).secondSet);
            NodeSet root = new NodeSet(tmp, null);
            for(Branch branch: inputSet) {
                root.addChild(new NodeSet(branch.firstSet, null));
            }
        }
        return tree;
    }

    public static ArrayList<ArrayList<Branch>> screeningOfBranches(ArrayList<Branch> data, ArrayList<Branch> input) {
        if (input == null) {
            return null;
        }

        LinkedList<InformationForBranch> informationTrees = new LinkedList<InformationForBranch>();

        screeningOfBranches_visit(0, data, new LinkedList<Branch>(), input, informationTrees);

        ArrayList<ArrayList<Branch>> ans = new ArrayList<ArrayList<Branch>>();
        for(InformationForBranch tree: informationTrees) {
            ans.add(tree.branches);
        }
        return ans;
    }

    private static void screeningOfBranches_visit(int start, ArrayList<Branch> inputSet, LinkedList<Branch> currentSet, ArrayList<Branch> stats, LinkedList<InformationForBranch> ans) {
        int i = start;
        boolean flag = false;

        for(; i < stats.size(); ++i) {
            if (Branch.compatibilityAll(inputSet, stats.get(i)) && Branch.compatibilityAll(currentSet, stats.get(i))) {
                int index = currentSet.size();
                currentSet.addLast(stats.get(i));
                screeningOfBranches_visit(i + 1, inputSet, currentSet, stats, ans);
                currentSet.remove(index);
                flag = true;
            }
        }

        if (i == stats.size() && !currentSet.isEmpty() && !flag) {
            InformationForBranch tmp = new InformationForBranch(currentSet);
            int index = 0;
            for(; index < ans.size(); ++index) {
                if (ans.get(index).weight <= tmp.weight) {
                    break;
                }
            }

            ans.add(index, tmp);

            if (ans.size() > 3) {
                ans.removeLast();
            }
        }
    }

    private String toMyString() {
        String ans = "";
        for(Character element: firstSet) {
            ans += element;
        }
        for(Character element: secondSet) {
            ans += element;
        }
        return ans;
    }

    static class InformationForBranch {
        int weight = 0;
        ArrayList<Branch> branches;

        InformationForBranch(List<Branch> branches_) {
           for(Branch branch: branches_) {
               weight += branch.weight;
           }
           branches = new ArrayList<Branch>(branches_);
        }
    }

    @Override
    public int compareTo(Branch branch) {
        return  new Integer(firstSet.size()).compareTo(branch.firstSet.size());
    }

    @Override
    public String toString() {
        String ans = weight + " ";

        for(Character element: firstSet) {
            ans += element;
        }
        ans += " + ";
        for(Character element: secondSet) {
            ans += element;
        }

        return ans;
    }
}


