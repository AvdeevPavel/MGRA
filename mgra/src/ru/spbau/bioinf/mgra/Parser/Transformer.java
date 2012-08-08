package ru.spbau.bioinf.mgra.Parser;

import org.apache.log4j.Logger;
import org.jdom.Element;
import org.jdom.Document;
import ru.spbau.bioinf.mgra.DataFile.BlocksInformation;
import ru.spbau.bioinf.mgra.DataFile.Config;
import ru.spbau.bioinf.mgra.Drawer.DrawerGenomes;
import ru.spbau.bioinf.mgra.Server.JettyServer;
import ru.spbau.bioinf.mgra.Server.XmlUtil;
import ru.spbau.bioinf.mgra.Tree.Branch;
import ru.spbau.bioinf.mgra.Tree.Node;
import ru.spbau.bioinf.mgra.Tree.Tree;

import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class Transformer {
    private static final Logger log = Logger.getLogger(Node.class);

    public Transformer(Config config, BlocksInformation blocksInformation, PrintWriter out) throws IOException, InterruptedException {
        Document doc = new Document();
        ArrayList<String> nameValues = new ArrayList<String>();

        if (config.isUseTarget() && config.getTarget() != null) {
            JettyServer.response(out, "STAGE: You entered target and now create target genome");
            Element rootXml = new Element("targetgenome");
            doc.setRootElement(rootXml);

            nameValues.add(config.getTarget());

            DrawerGenomes genomes = new DrawerGenomes(nameValues, config, blocksInformation);

            createInformationForGenome(rootXml, nameValues.get(0), Branch.convertToSet(nameValues.get(0)), genomes);
        } else {
            ArrayList<String> stringTrees = config.getTrees();
            ArrayList<Tree> trees = new ArrayList<Tree>();
            int countTree = 0;

            JettyServer.response(out, "STAGE: Read trees");
            for(String s: stringTrees) {
                trees.add(new Tree(s, countTree++));
            }

            JettyServer.response(out, "STAGE: Merge trees");
            trees = merge(trees);

            JettyServer.response(out, "STAGE: Check is full trees");
            boolean isFull = true;
            for(Tree tree: trees) {
                isFull = isFull && tree.isFullTree(config.getNumberOfGenome());
            }

            JettyServer.response(out, "STAGE: Is all transformation complete");
            boolean isComplete = true;
            for(Tree tree: trees) {
                isComplete = isComplete && tree.isCompleteTransformation(config.getPathParentFile());
            }

            ArrayList<Tree> newTrees = null;
            if (!isFull || !isComplete) {
                JettyServer.response(out, "STAGE: Start reconstructed tree");
                JettyServer.response(out, "STAGE: Create branch for input tree");
                ArrayList<Branch> dataBranch = createBranchOfInputTree(trees);

                //delete
                for (Branch branch: dataBranch) {
                    System.out.println(branch.toString());
                }

                JettyServer.response(out, "STAGE: Read branch for stats.txt");
                ArrayList<Branch> inputBranch = null;
                try {
                    inputBranch = readBranchInStats(config, true);
                    //delete
                    for(Branch st: inputBranch) {
                        System.out.println(st.toString());
                    }
                } catch (Exception e) {
                    JettyServer.responseErrorServer(out, "Problem read file stats.txt with information for reconstructed trees and branches");
                    log.error("Problem read file stats.txt with information for reconstructed trees and branches", e);
                }

                JettyServer.response(out, "STAGE: Screening of branches and choose correct");
                ArrayList<ArrayList<Branch>> currentSet = Branch.screeningOfBranches(dataBranch, inputBranch);

                if (currentSet == null || currentSet.isEmpty()) {
                    JettyServer.response(out, "STAGE: Not found correct branches. Is input tree correct? Run MGRA tool without input tree");
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

                    JettyServer.response(out, "STAGE: Read branch in stats.txt and screening this");
                    dataBranch = new ArrayList<Branch>();
                    inputBranch = readBranchInStats(config, false);
                    currentSet = Branch.screeningOfBranches(dataBranch, inputBranch);
                    //delete
                    for(ArrayList<Branch> branches: currentSet) {
                        System.out.println("is a new set branch");
                        for (Branch branch: branches) {
                            System.out.println(branch.toString());
                        }
                    }
                }

                JettyServer.response(out, "STAGE: Create trees with new correct branches");
                newTrees = new ArrayList<Tree>();
                for(ArrayList<Branch> branches: currentSet) {
                    branches.addAll(dataBranch);
                    if (branches != null || !branches.isEmpty()) {
                        newTrees.add(new Tree(branches, countTree++));
                    }
                    branches.removeAll(dataBranch);
                }
                //generate Namevaluse
            }

            //generate nameValues

            DrawerGenomes genomes = new DrawerGenomes(nameValues, config, blocksInformation);

            Element rootXml = new Element("trees");
            doc.setRootElement(rootXml);

            JettyServer.response(out, "STAGE: Convert input tree to xml");
            Element inputTree = new Element("inputtrees");
            for(Tree tree: trees) {
                inputTree.addContent(tree.toXml(config, genomes));
            }
            rootXml.addContent(inputTree);

            if (newTrees != null) {
                if (!newTrees.isEmpty()) {
                    JettyServer.response(out, "STAGE: Convert reconstructed tree to xml");
                    Element reconstructTree = new Element("reconstructtrees");
                    for(Tree tree: newTrees) {
                        if (tree != null) {
                            reconstructTree.addContent(tree.toXml(config, genomes));
                        }
                    }
                    rootXml.addContent(reconstructTree);
                }
            }
        }

        XmlUtil.saveXml(doc, new File(config.getPathParentFile(), "tree.xml"));
    }



    private ArrayList<Tree> merge(ArrayList<Tree> oldTrees) {
        ArrayList<Tree> newTree = new ArrayList<Tree>();
        boolean[] mark = new boolean[oldTrees.size()];

        for(int i = 0; i < oldTrees.size(); ++i) {
            Tree tree = oldTrees.get(i);
            for(int j = 0; j < oldTrees.size(); ++j) {
                if (j == i) continue;
                Tree mergeTree = oldTrees.get(j);

                if (tree.merge(mergeTree) == true) {
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




    public static BufferedReader getBufferedInputReader(File file) throws FileNotFoundException {
        return new BufferedReader(new InputStreamReader(new FileInputStream(file)));
    }

    private ArrayList<Branch> createBranchOfInputTree(ArrayList<Tree> trees) {
        ArrayList<Branch> ans = new ArrayList<Branch>();
        for(Tree tree: trees) {
            tree.createBranches(ans);
        }
        return ans;
    }

    private ArrayList<Branch> readBranchInStats(Config config, boolean isDropBf) throws IOException {
        BufferedReader input = getBufferedInputReader(new File(config.getPathParentFile(), "stats.txt"));
        int stage = 0;
        String s = "";

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

    public static void createInformationForGenome(Element parent, String name, HashSet<Character> nameSet, DrawerGenomes genomes) {
        XmlUtil.addElement(parent, "name", name);
        if (genomes.isCreateImage(nameSet)) {
            Element gen = new Element("genome");
            XmlUtil.addElement(gen, "resize", genomes.isResize(nameSet));
            parent.addContent(gen);
        }
    }

    public static void createTransformationToPNG(Element parent, Genome genome, String name, String path) {
        try {
            BufferedReader input = Transformer.getBufferedInputReader(new File(path, name + ".trs"));
            List<Transformation> transformations = new ArrayList<Transformation>();
            String s;

            while ((s = input.readLine())!=null) {
                transformations.add(new Transformation(s));
            }

            for (Transformation transformation : transformations) {
                transformation.update(genome);
            }

            XmlUtil.addElement(parent, "length", transformations.size());
            Element trs = new Element("transformations");

            for (Transformation transformation : transformations) {
                trs.addContent(transformation.toXml());
            }

            parent.addContent(trs);
        } catch (Exception e) {
            log.debug("Not find file " + name + ".gen");
        }
    }

    public static void main(String[] args) {
        ArrayList<Branch> inputSet = new ArrayList<Branch>();
        inputSet.add(new Branch("A", "BCDEFGH", 1));
        inputSet.add(new Branch("FG", "ABCDEH", 1));
        inputSet.add(new Branch("BC", "ADEFGH", 1));
        inputSet.add(new Branch("BCD", "AEFGH", 1));
        inputSet.add(new Branch("FGH", "ABCDE", 1));

        //createTree(inputSet, 0);
    }
}
