package ru.spbau.bioinf.mgra.Tree;

import java.util.*;

public class Branch implements Comparable<Branch> {
    private HashSet<Character> firstSet = new HashSet<Character>();
    private HashSet<Character> secondSet = new HashSet<Character>();
    private int weight = 0;

    private void init(String first, String second)  {
        if (first.length() > second.length()) {
            String temp = first;
            first = second;
            second = temp;
        }

        firstSet = TreeReader.convertToSet(first);
        secondSet = TreeReader.convertToSet(second);
    }

    public Branch(String first, String second, int weight_) {
        init(first, second);
        weight = weight_;
    }

    public Branch(HashSet<Character> first, HashSet<Character> second) {
       firstSet = new HashSet<Character>(first);
       secondSet = new HashSet<Character>(second);
       secondSet.removeAll(firstSet);
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

    public boolean compatibilityAll(Collection<Branch> inputSet) {
        for(Branch branch: inputSet) {
            if (!this.compatibilityTo(branch)) {
               return false;
            }
        }
        return true;
    }

    public boolean isSizeBranchOne() {
        if (firstSet.size() == 1 || secondSet.size() == 1) {
            return true;
        }
        return false;
    }

    public HashSet<Character> getAllSet() {
        HashSet<Character> ans = new HashSet<Character>(firstSet);
        ans.addAll(secondSet);
        return ans;
    }

    public HashSet<Character> getFirstSet() {
        return firstSet;
    }

    public static ArrayList<ArrayList<Branch>> screeningOfBranches(ArrayList<Branch> data, ArrayList<Branch> input) {
        if (input == null) {
            return null;
        }

        if (input.isEmpty()) {
            return null;
        }

        LinkedList<InformationForBranch> informationTrees = new LinkedList<InformationForBranch>();
        screeningOfBranches_visit(0, data, new HashSet<Branch>(), input, informationTrees);

        boolean[] mark = new boolean[informationTrees.size()];
        for(int j = 0; j < informationTrees.size(); ++j) {
                for(int i = 0; i < informationTrees.size(); ++i) {
                    if (i != j && informationTrees.get(j).compare(informationTrees.get(i))) {
                        mark[i] = true;
                    }
                }
        }

        for(int i = 0; i < informationTrees.size(); ++i) {
            if (mark[i]) {
                informationTrees.remove(i);
            }
        }

        ArrayList<ArrayList<Branch>> ans = new ArrayList<ArrayList<Branch>>();
        for(InformationForBranch tree: informationTrees) {
            ans.add(tree.branches);
        }
        return ans;
    }

    private static void screeningOfBranches_visit(int start, ArrayList<Branch> inputSet, HashSet<Branch> currentSet, ArrayList<Branch> stats, LinkedList<InformationForBranch> ans) {
        int i = start;
        boolean flag = false;

        for(; i < stats.size(); ++i) {
            if (stats.get(i).compatibilityAll(inputSet) && stats.get(i).compatibilityAll(currentSet)) {
                if (stats.get(i).isSizeBranchOne()) {
                    currentSet.add(stats.get(i));
                } else {
                    currentSet.add(stats.get(i));
                    screeningOfBranches_visit(i + 1, inputSet, currentSet, stats, ans);
                    currentSet.remove(stats.get(i));
                    flag = true;
               }
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

    static class InformationForBranch {
        int weight = 0;
        ArrayList<Branch> branches;

        InformationForBranch(HashSet<Branch> branches) {
           for(Branch branch: branches) {
               weight += branch.weight;
           }
           this.branches = new ArrayList<Branch>(branches);
        }

        boolean compare(InformationForBranch secondBranch) {
            for(Branch branch: branches) {
                boolean flag = false;
                for(Branch secBranch: secondBranch.branches) {
                    if (branch.equals(secBranch)) {
                        flag = true;
                    }
                }
                if (!flag)
                    return false;
            }
            return true;
        }
    }

    public boolean equals(Branch second) {
        if (this == second)
            return true;

        if (second.firstSet.equals(firstSet) && second.secondSet.equals(secondSet)) {
            return true;
        }

        if (second.firstSet.equals(secondSet) && second.secondSet.equals(firstSet)) {
            return true;
        }

        return false;
    }

    @Override
    public int compareTo(Branch branch) {
        return new Integer(firstSet.size()).compareTo(branch.firstSet.size());
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


