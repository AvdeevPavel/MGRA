package ru.spbau.bioinf.mgra.Parser;

import org.apache.log4j.Logger;
import org.jdom.Element;
import org.jdom.Document;
import ru.spbau.bioinf.mgra.DataFile.BlocksInformation;
import ru.spbau.bioinf.mgra.DataFile.Config;
import ru.spbau.bioinf.mgra.Drawer.CreatorInformation;
import ru.spbau.bioinf.mgra.Server.JettyServer;
import ru.spbau.bioinf.mgra.Server.XmlUtil;
import ru.spbau.bioinf.mgra.Tree.Branch;
import ru.spbau.bioinf.mgra.Tree.Node;
import ru.spbau.bioinf.mgra.Tree.Tree;

import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;

public class Transformer {
    private static final Logger log = Logger.getLogger(Node.class);

    public Transformer(Config config, BlocksInformation blocksInformation, PrintWriter out) throws IOException, InterruptedException {
        Document doc = new Document();
        Element rootXml;

        CreatorInformation genomes = new CreatorInformation(config, blocksInformation, out);

        if (config.isUseTarget() && config.getTarget() != null) {
            JettyServer.responseStage(out, "You entered target genome.");
            rootXml = new Element("target");
        } else {
            JettyServer.responseStage(out, "Read trees.");
            ArrayList<Tree> trees = readTrees(config.getTrees());

            JettyServer.responseStage(out, "Merge trees.");
            trees = merge(trees);

            JettyServer.responseStage(out, "Check is full trees.");
            boolean isFull = isAllFullTree(trees, config.getNumberOfGenome());
            JettyServer.responseInformation(out, "Answer is " + isFull);

            JettyServer.responseStage(out, "Check is all transformation complete.");
            boolean isComplete = isAllTransformationComplete(trees, genomes);
            JettyServer.responseInformation(out, "Answer is " + isComplete);

            ArrayList<Tree> newTrees = new ArrayList<Tree>();
            if (!isFull || !isComplete) {
                JettyServer.responseStage(out, "Start reconstructed tree.");

                JettyServer.responseStage(out, "Create branch for input tree.");
                ArrayList<Branch> dataBranch = createBranchOfInputTree(trees);

                ArrayList<ArrayList<Branch>> currentSet = subStageInAlgorithm(out, config, dataBranch);
                if (currentSet == null || currentSet.isEmpty()) {
                    JettyServer.responseInformation(out, "Not found correct branches. Is input tree correct?");
                    JettyServer.responseStage(out, "Run MGRA tool without input tree.");
                    config.createFile(false);

                    String[] command = new String[]{JettyServer.exeFile.getAbsolutePath(), config.getNameFile()};
                    Process process = Runtime.getRuntime().exec(command, new String[]{}, new File(config.getPathParentFile()));

                    Thread outputThread = JettyServer.listenOutput(process.getInputStream(), out, "output");
                    Thread errorThread = JettyServer.listenOutput(process.getErrorStream(), out, "output");

                    do {
                        try {
                            int value = process.waitFor();
                            log.debug("MGRA process return value : " + value);
                            break;
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } while (true);

                    outputThread.join();
                    errorThread.join();

                    dataBranch.clear();
                    currentSet = subStageInAlgorithm(out, config, dataBranch);
                }

                JettyServer.response(out, "STAGE: Create trees with new correct branches");
                newTrees = createTreesOfBranch(dataBranch, currentSet, trees.size());
            } else {
                JettyServer.responseInformation(out, "Not reconstructed tree.");
            }

            rootXml = new Element("full");
            Element xmlTrees = new Element("trees");

            JettyServer.responseStage(out, "Convert input tree to xml");
            convertTreeToXML(xmlTrees, "input", trees, genomes);

            JettyServer.responseStage(out, "Convert reconstructed tree to xml");
            convertTreeToXML(xmlTrees, "reconstruct", newTrees, genomes);

            rootXml.addContent(xmlTrees);
            rootXml.addContent(genomes.getTransformationXml());
        }

        rootXml.addContent(genomes.getGenomesXml());
        doc.setRootElement(rootXml);

        JettyServer.responseStage(out, "Save results to xml file");
        try {
            XmlUtil.saveXml(doc, new File(config.getPathParentFile(), "tree.xml"));
            JettyServer.responseInformation(out, "File saved.");
        } catch(IOException e) {
            JettyServer.responseErrorServer(out, "Do not save xml file.");
            throw e;
        }
    }

    private ArrayList<Tree> readTrees(ArrayList<String> stringTrees) {
        ArrayList<Tree> trees = new ArrayList<Tree>();

        int countTree = 0;

        for(String s: stringTrees) {
            if (!s.isEmpty()) {
                trees.add(new Tree(s, countTree++));
            }
        }

        return trees;
    }

    private ArrayList<Tree> merge(ArrayList<Tree> oldTrees) {
        ArrayList<Tree> newTree = new ArrayList<Tree>();
        boolean[] mark = new boolean[oldTrees.size()];

        for(int i = 0; i < oldTrees.size(); ++i) {
            for(int j = 0; j < oldTrees.size(); ++j) {
                if (j == i) continue;
                Tree mergeTree = oldTrees.get(j);

                if (oldTrees.get(i).merge(mergeTree)) {
                    mark[j] = true;
                }
            }
        }

        for(int i = 0; i < oldTrees.size(); ++i) {
            if (!mark[i]) {
                newTree.add(oldTrees.get(i));
            }
        }

        return newTree;
    }

    private boolean isAllFullTree(ArrayList<Tree> trees, int number) {
        if (trees.isEmpty()) {
            return false;
        } else {
            boolean isFull = true;
            for(Tree tree: trees) {
                isFull = isFull && tree.isFullTree(number);
            }
            return isFull;
        }
    }

    private boolean isAllTransformationComplete(ArrayList<Tree> trees, CreatorInformation genomes) {
        if (trees.isEmpty()) {
            return false;
        } else {
            boolean isComplete = true;
            for(Tree tree: trees) {
                isComplete = isComplete && tree.isCompleteTransformation(genomes);
            }
            return isComplete;
        }
    }

    private ArrayList<Branch> createBranchOfInputTree(ArrayList<Tree> trees) {
        ArrayList<Branch> ans = new ArrayList<Branch>();
        for(Tree tree: trees) {
            tree.createBranches(ans);
        }
        return ans;
    }

    private ArrayList<ArrayList<Branch>> subStageInAlgorithm(PrintWriter out, Config config, ArrayList<Branch> dataBranch) {
        JettyServer.responseStage(out, "Read branch for stats.txt");
        ArrayList<Branch> inputBranch = null;
        try {
            inputBranch = readBranchInStats(config, true);
            JettyServer.responseInformation(out, "Finish read branch.");
        } catch (Exception e) {
            JettyServer.responseErrorServer(out, "Problem read file stats.txt with information for reconstructed trees and branches");
            log.error("Problem read file stats.txt with information for reconstructed trees and branches", e);
        }

        JettyServer.responseStage(out, "Screening of branches and choose correct");
        return Branch.screeningOfBranches(dataBranch, inputBranch);
    }

    private ArrayList<Branch> readBranchInStats(Config config, boolean isDropBf) throws IOException {
        BufferedReader input = new BufferedReader(new InputStreamReader(new FileInputStream(new File(config.getPathParentFile(), "stats.txt"))));
        int stage = 0;
        String s;

        while((s = input.readLine()) != null) {
            if (s.contains("Rearrangement characters")) {
                ++stage;
                if (stage == config.getStage()) {
                    break;
                }
            }
        }

        //drop string of table in Latex
        for(int i = 0; i < 6; ++i) {
            input.readLine();
        }

        ArrayList<Branch> ans = new ArrayList<Branch>();
        while(!((s = input.readLine()).contains("\\hline"))) {
            if (s.contains("\\emptyset")) {
                continue;
            }
            if (s.contains("\\bf")) {
                if (isDropBf) {
                    continue;
                } else {
                    String st = s.substring(s.indexOf('{') + 4, s.lastIndexOf('}'));
                    String first = st.substring(0, st.indexOf('+')).trim();
                    String second = st.substring(st.indexOf('+') + 1).trim();
                    String weight = s.substring(s.indexOf('=') + 1, s.indexOf('&', s.indexOf('='))).trim();
                    ans.add(new Branch(first, second, Integer.valueOf(weight)));
                }
            }
            String st = s.substring(s.indexOf('{') + 1, s.lastIndexOf('}'));
            String first = st.substring(0, st.indexOf('+')).trim();
            String second = st.substring(st.indexOf('+') + 1).trim();
            String weight = s.substring(s.indexOf('=') + 1, s.indexOf('&', s.indexOf('='))).trim();
            ans.add(new Branch(first, second, Integer.valueOf(weight)));
        }

        return ans;
    }

    private ArrayList<Tree> createTreesOfBranch(ArrayList<Branch> dataBranch, ArrayList<ArrayList<Branch>> currentSet, int countTree) {
        ArrayList<Tree> newTrees = new ArrayList<Tree>();
        for(ArrayList<Branch> branches: currentSet) {
            branches.addAll(dataBranch);
            if (branches != null || !branches.isEmpty()) {
                newTrees.add(new Tree(branches, countTree++));
            }
            branches.removeAll(dataBranch);
        }
        return newTrees;
    }

    private void convertTreeToXML(Element parent, String name, ArrayList<Tree> trees, CreatorInformation genomes) {
        if (trees != null) {
            if (!trees.isEmpty()) {
                Element xmlTree = new Element(name);
                for(Tree tree: trees) {
                    if (tree != null) {
                        xmlTree.addContent(tree.toXml(genomes));
                    }
                }
                parent.addContent(xmlTree);
            }
        }
    }

    public static HashSet<Character> convertToSet(String name) {
        HashSet<Character> ans = new HashSet<Character>();
        for(int i = 0; i < name.length(); ++i) {
            ans.add(name.charAt(i));
        }
        return ans;
    }

    public static String convertToString(HashSet<Character> data) {
        String ans = "";
        for(Character ch: data) {
            ans += ch;
        }
        return ans;
    }

    public static void main(String[] args) {
        ArrayList<Branch> inputSet = new ArrayList<Branch>();
        inputSet.add(new Branch("A", "BCDEFGH", 1));
        inputSet.add(new Branch("FG", "ABCDEH", 1));
        inputSet.add(new Branch("BC", "ADEFGH", 1));
        inputSet.add(new Branch("BCD", "AEFGH", 1));
        inputSet.add(new Branch("FGH", "ABCDE", 1));
        //createTreesOfBranch(inputSet, 0);
    }
}
