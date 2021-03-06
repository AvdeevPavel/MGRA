package ru.spbau.bioinf.mgra.Tree;

import org.apache.log4j.Logger;
import org.jdom.Element;
import org.jdom.Document;

import ru.spbau.bioinf.mgra.DataFile.Config;
import ru.spbau.bioinf.mgra.Server.JettyServer;
import ru.spbau.bioinf.mgra.Server.MyHandler;
import ru.spbau.bioinf.mgra.Server.XmlUtil;

import java.awt.image.AreaAveragingScaleFilter;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class TreeReader {
    private static final Logger log = Logger.getLogger(Node.class);

    public static void createTarget(Config config, PrintWriter out) throws IOException, InterruptedException {
        MyHandler.responseStage(out, "You entered target genome.");
        Document doc = new Document();
        Element rootXml = new Element("target");
        Element gen = new Element("genomes");

        builtGenomeToXML(gen, config);
        rootXml.addContent(gen);

        doc.setRootElement(rootXml);

        save(config.getPathParentFile(), out, "tree.xml", doc);
    }

    public static void createFullPage(Config config, PrintWriter out) throws IOException, InterruptedException {
        MyHandler.responseStage(out, "Read trees.");
        ArrayList<Tree> trees = readTrees(config.getTrees());

        MyHandler.responseStage(out, "Merge trees.");
        trees = merge(trees);

        /*JettyServer.responseStage(out, "Check is full trees.");
        boolean isFull = isAllFullTree(trees, config.getNumberOfGenome());
        JettyServer.responseInformation(out, "Answer is " + isFull);*/

        ArrayList<Tree> newTrees = new ArrayList<Tree>();
        if (config.isReconstructedTree()) {
            MyHandler.responseStage(out, "You enter option for reconstructed tree. Start reconstructed tree.");

            MyHandler.responseStage(out, "Create branch for input tree.");
            ArrayList<Branch> dataBranch = createBranchOfInputTree(trees);

            ArrayList<ArrayList<Branch>> currentSet = subStageInAlgorithm(out, config, dataBranch, true);
            if (currentSet == null || currentSet.isEmpty()) {
                MyHandler.responseInformation(out, "Not found correct branches. Is input tree correct?");
                config.createFile(false);
                MyHandler.responseStage(out, "Run MGRA tool without input tree.");
                JettyServer.runMgraTool(config, out);
                dataBranch.clear();
                currentSet = subStageInAlgorithm(out, config, dataBranch, false);
            }

            MyHandler.response(out, "STAGE: Create trees with new correct branches");
            newTrees = createTreesOfBranch(dataBranch, currentSet, trees.size());
        } else {
            MyHandler.responseInformation(out, "Not reconstructed tree.");
        }

        Document doc = new Document();
        Element rootXml = new Element("full");

        Element xmlTrees = new Element("trees");
        HashMap<HashSet<Character>, String> builtGenome = getBuiltGenome(config);
        appendTransformation(trees, newTrees, config.getPathParentFile(), builtGenome, out);
        Element input = new Element("input");
        Element reconstruct = new Element("reconstruct");

        MyHandler.responseStage(out, "Convert input tree to xml");
        convertTreeToXML(input, trees, builtGenome);

        MyHandler.responseStage(out, "Convert reconstructed tree to xml");
        convertTreeToXML(reconstruct, newTrees, builtGenome);

        xmlTrees.addContent(input);
        if (newTrees != null) {
            if (!newTrees.isEmpty()) {
                xmlTrees.addContent(reconstruct);
            }
        }
        rootXml.addContent(xmlTrees);

        Element trs = new Element("transformations");
        transformationToXML(trs, config);
        rootXml.addContent(trs);

        Element gen = new Element("genomes");
        builtGenomeToXML(gen, config);
        rootXml.addContent(gen);

        doc.setRootElement(rootXml);
        save(config.getPathParentFile(), out, "tree.xml", doc);
    }

    public static void createShowTree(String[] stringTrees, String path) throws IOException, InterruptedException {
        log.debug("Start view showtree");
        log.debug("Read trees.");
        ArrayList<String> st = new ArrayList<String>();
        for(String s: stringTrees) {
            st.add(s);
        }

        log.debug("Merge trees.");
        ArrayList<Tree> trees = readTrees(st);
        trees = merge(trees);

        log.debug("Convert trees to xml.");
        Document doc = new Document();
        Element rootXml = new Element("trees");
        convertTreeToXML(rootXml, trees, new HashMap<HashSet<Character>, String>());
        doc.setRootElement(rootXml);

        log.debug("Save to xml.");
        try {
            XmlUtil.saveXml(doc, new File(path, "showtree.xml"));
            log.debug("Done");
        } catch (Exception e) {
            log.error("Can not save, because" + e);
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

    private static ArrayList<Tree> readTrees(ArrayList<String> stringTrees) {
        ArrayList<Tree> trees = new ArrayList<Tree>();

        int countTree = 0;

        for(String s: stringTrees) {
            if (!s.isEmpty()) {
                trees.add(new Tree(s, countTree++));
            }
        }

        return trees;
    }

    private static ArrayList<Tree> merge(ArrayList<Tree> oldTrees) {
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

    private static boolean isAllFullTree(ArrayList<Tree> trees, int number) {
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

    private static ArrayList<Branch> createBranchOfInputTree(ArrayList<Tree> trees) {
        ArrayList<Branch> ans = new ArrayList<Branch>();
        for(Tree tree: trees) {
            tree.createBranches(ans);
        }
        return ans;
    }

    private static ArrayList<ArrayList<Branch>> subStageInAlgorithm(PrintWriter out, Config config, ArrayList<Branch> dataBranch, boolean isDropBf) {
        MyHandler.responseStage(out, "Read branch for stats.txt");
        ArrayList<Branch> inputBranch = null;
        try {
            inputBranch = readBranchInStats(config, isDropBf);
            MyHandler.responseInformation(out, "Finish read branch.");
        } catch (Exception e) {
            MyHandler.responseErrorServer(out, "Problem read file stats.txt with information for reconstructed trees and branches");
            log.error("Problem read file stats.txt with information for reconstructed trees and branches", e);
        }

        MyHandler.responseStage(out, "Screening of branches and choose correct");
        return Branch.screeningOfBranches(dataBranch, inputBranch);
    }

    private static ArrayList<Branch> readBranchInStats(Config config, boolean isDropBf) throws IOException {
        BufferedReader input = new BufferedReader(new InputStreamReader(new FileInputStream(new File(config.getPathParentFile(), "stats.txt"))));
        ArrayList<Branch> ans = new ArrayList<Branch>();
        String s;

        while((s = input.readLine()) != null) {
            if (s.contains("Rearrangement characters")) {
                ans.clear();

                //drop string of table in Latex
                for(int i = 0; i < 6; ++i) {
                    input.readLine();
                }

                while(!((s = input.readLine()).contains("\\hline"))) {
                    if (s.contains("\\emptyset")) {
                        continue;
                    } else if (s.contains("\\bf")) {
                        if (!isDropBf) {
                            String st = s.substring(s.indexOf('{') + 4, s.lastIndexOf('}'));
                            String first = st.substring(0, st.indexOf('+')).trim();
                            String second = st.substring(st.indexOf('+') + 1).trim();
                            String weight = s.substring(s.indexOf('=') + 1, s.indexOf('&', s.indexOf('='))).trim();
                            ans.add(new Branch(first, second, Integer.valueOf(weight)));
                        }
                    } else {
                        String st = s.substring(s.indexOf('{') + 1, s.lastIndexOf('}'));
                        String first = st.substring(0, st.indexOf('+')).trim();
                        String second = st.substring(st.indexOf('+') + 1).trim();
                        String weight = s.substring(s.indexOf('=') + 1, s.indexOf('&', s.indexOf('='))).trim();
                        ans.add(new Branch(first, second, Integer.valueOf(weight)));
                    }
                }
            }
        }

        for(Branch branch: ans)
            System.out.println(branch);
        return ans;
    }

    private static ArrayList<Tree> createTreesOfBranch(ArrayList<Branch> dataBranch, ArrayList<ArrayList<Branch>> currentSet, int countTree) {
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

    private static void convertTreeToXML(Element parent, ArrayList<Tree> trees, HashMap<HashSet<Character>, String> builtGenome) {
        if (trees != null) {
            if (!trees.isEmpty()) {
                for(Tree tree: trees) {
                    if (tree != null) {
                        parent.addContent(tree.toXml(builtGenome));
                    }
                }
            }
        }
    }

    private static HashMap<HashSet<Character>, String> getBuiltGenome(Config config) {
        HashMap<HashSet<Character>, String> builtGenome = new HashMap<HashSet<Character>, String>();
        File[] files = new File(config.getPathParentFile()).listFiles();
        for(File file: files) {
            if (file.getName().endsWith(".gen")) {
                builtGenome.put(convertToSet(file.getName().substring(0, file.getName().indexOf('.'))), file.getName().substring(0, file.getName().indexOf('.')));
            }
        }
        return builtGenome;
    }

    private static void builtGenomeToXML(Element parent, Config config) {
        File[] files = new File(config.getPathParentFile()).listFiles();
        for(File file: files) {
            if (file.getName().endsWith(".gen")) {
                Element gen = new Element("genome");
                XmlUtil.addElement(gen, "name", file.getName().substring(0, file.getName().indexOf('.')));
                parent.addContent(gen);
            }
        }
    }

    private static void transformationToXML(Element parent, Config config) {
        File[] files = new File(config.getPathParentFile()).listFiles();
        for(File file: files) {
            if (file.getName().endsWith(".trs")) {
                Element gen = new Element("transformation");
                XmlUtil.addElement(gen, "name", file.getName().substring(0, file.getName().indexOf('.')));
                parent.addContent(gen);
            }
        }
    }

    private static void appendTransformation(ArrayList<Tree> trees, ArrayList<Tree> reconstructTrees, String pathDirectory, HashMap<HashSet<Character>, String> builtGenome, PrintWriter out) {
        try {
            for(Tree tree: trees) {
                tree.appendTransfromation(pathDirectory, builtGenome);
            }

            if (reconstructTrees != null) {
                for(Tree tree: reconstructTrees) {
                    tree.appendTransfromation(pathDirectory, builtGenome);
                }
            }
        } catch (IOException e) {
            MyHandler.responseErrorServer(out, "Can not update root transformation. Output data can contains error.");
            log.error("Can not work with append transformation " + e);
        }
    }

    private static void save(String path, PrintWriter out, String nameFile, Document doc) throws IOException {
        MyHandler.responseStage(out, "Save results to xml file");
        try {
            XmlUtil.saveXml(doc, new File(path, nameFile));
            MyHandler.responseInformation(out, "File saved.");
        } catch(IOException e) {
            MyHandler.responseErrorServer(out, "Do not save xml file.");
            throw e;
        }
    }
}
