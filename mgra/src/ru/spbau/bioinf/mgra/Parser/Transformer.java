package ru.spbau.bioinf.mgra.Parser;

import org.apache.log4j.Logger;
import org.jdom.Element;
import org.jdom.Document;
import ru.spbau.bioinf.mgra.DataFile.BlocksInformation;
import ru.spbau.bioinf.mgra.DataFile.Config;
import ru.spbau.bioinf.mgra.Drawer.Drawer;
import ru.spbau.bioinf.mgra.Server.JettyServer;
import ru.spbau.bioinf.mgra.Server.XmlUtil;
import sun.rmi.runtime.NewThreadAction;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Transformer {
    private static final Logger log = Logger.getLogger(Node.class);

    public static void main(String[] args) throws Exception {
       //String fileName = "data/toy";
       //if (args.length > 0) {
       //   fileName = args[0];
       //}
       Branch first = new Branch("ABC", "DEFG");
       Branch second = new Branch("CEFG", "ABD");

       System.out.println(first.compatibilityTo(second));

       //new Transformer();
    }

    public Transformer() {
        /*JettyServer.response(out, "Merge trees");
            trees = merge(trees);

            for(Tree tree: trees) {
                System.out.println(tree.toString());
            }

            System.out.println("After stage 2");
            JettyServer.response(out, "Check is full trees");
            System.out.println("Is full tree:");
            for(Tree tree: trees) {
                System.out.println(tree.isFullTree(config.getNumberOfGenome()));
            }

            System.out.println();
            System.out.println("Is complete transformation:");
            for(Tree tree: trees) {
                System.out.println(tree.isCompleteTransformation(config.getPathParentFile()));
            }

            System.out.println("After stage 3");
            ArrayList<Branch> dataBranch = createBranchOfInputTree(trees);
            for(Branch st: dataBranch) {
                System.out.println(st.toString());
            }

            System.out.println();
            System.out.println("After stage 4");
            ArrayList<Branch> inputBranch = null;
            try {
                inputBranch = readBranchInStats(config);
                for(Branch st: inputBranch) {
                    System.out.println(st.toString());
                }
            } catch (Exception e) {
                log.error("Problem read file stats.txt with information for reconstructed trees and branches", e);
            }

            System.out.println("After stage 5");
            ArrayList<ArrayList<Branch>> currentSet = Branch.screeningOfBranches(dataBranch, inputBranch);

            for(ArrayList<Branch> branches: currentSet) {
                System.out.println("is a new set branch");
                for (Branch branch: branches) {
                    System.out.println(branch.toString());
                }
            }

            System.out.println("After stage 6");
            if (currentSet == null || currentSet.isEmpty()) {

                config.createFile(false);
                String[] command = new String[]{JettyServer.exeFile.getAbsolutePath(), config.getNameFile()};
                Process process = Runtime.getRuntime().exec(command, new String[]{}, new File(config.getPathParentFile()));

                do {
                    try {
                        int value = process.waitFor();
                        log.debug("MGRA process return value : " + value);
                        break;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } while (true);

                //add check if inputBranch != dataBranch
                dataBranch = new ArrayList<Branch>();
                inputBranch = readBranchInStats(config);
                currentSet = Branch.screeningOfBranches(dataBranch, inputBranch);
                for(ArrayList<Branch> branches: currentSet) {
                    System.out.println("is a new set branch");
                    for (Branch branch: branches) {
                        System.out.println(branch.toString());
                    }
                }
            }*/
    }

    // if null ibnput no transfromation
    //stage 1 done
    //stage 2 done
    //stage 3
    //stage 4 done
    public Transformer(Config config, BlocksInformation blocksInformation, PrintWriter out) throws IOException, InterruptedException {
        Document doc = new Document();

        if (config.isUseTarget() && config.getTarget() != null) {
            JettyServer.response(out, "You entered target and now create target genome");
            Element rootXml = new Element("targetgenome");
            doc.setRootElement(rootXml);
            createChromosomesToPNG(rootXml, config.getTarget(), config.getPathParentFile(), config, blocksInformation);
        } else {
            JettyServer.response(out, "Read trees");
            Element rootXml = new Element("trees");
            doc.setRootElement(rootXml);

            ArrayList<String> stringTrees = config.getTrees();
            ArrayList<Tree> trees = new ArrayList<Tree>();
            int countTree = 0;

            for(String s: stringTrees) {
                Tree tree = new Tree(s, countTree++);
                trees.add(tree);
            }

            JettyServer.response(out, "Merge trees");
            trees = merge(trees);

            JettyServer.response(out, "Check is full trees");
            boolean isFull = true;
            for(Tree tree: trees) {
                isFull = isFull && tree.isFullTree(config.getNumberOfGenome());
            }

            JettyServer.response(out, "Is all transformation complete");
            boolean isCompleate = true;
            for(Tree tree: trees) {
                isCompleate = isCompleate && tree.isCompleteTransformation(config.getPathParentFile());
            }


            if (!isFull || !isCompleate) {
                JettyServer.response(out, "Start reconstructed tree");
                JettyServer.response(out, "Create branch for input tree");
                ArrayList<Branch> dataBranch = createBranchOfInputTree(trees);
                JettyServer.response(out, "Read branch for stats.txt");

                ArrayList<Branch> inputBranch = null;
                try {
                    inputBranch = readBranchInStats(config);
                    for(Branch st: inputBranch) {
                        System.out.println(st.toString());
                    }
                } catch (Exception e) {
                    log.error("Problem read file stats.txt with information for reconstructed trees and branches", e);
                }

                JettyServer.response(out, "Screening of branches and choose correct");
                ArrayList<ArrayList<Branch>> currentSet = Branch.screeningOfBranches(dataBranch, inputBranch);


                if (currentSet == null || currentSet.isEmpty()) {
                    JettyServer.response(out, "Not found correct branches. Is input tree correct? Run MGRA tool without input tree");
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

                    //add check if inputBranch != dataBranch
                    JettyServer.response(out, "Read branch in stats.txt and screening this");
                    dataBranch = new ArrayList<Branch>();
                    inputBranch = readBranchInStats(config);
                    currentSet = Branch.screeningOfBranches(dataBranch, inputBranch);

                    for(ArrayList<Branch> branches: currentSet) {
                        System.out.println("is a new set branch");
                        for (Branch branch: branches) {
                            System.out.println(branch.toString());
                        }
                    }
                }

                JettyServer.response(out, "Create trees with new correct branches");
            }

            JettyServer.response(out, "Convert input tree to xml");
            for(Tree tree: trees) {
                rootXml.addContent(tree.toXml(config.getPathParentFile(), config, blocksInformation));
            }
        }
        XmlUtil.saveXml(doc, new File(config.getPathParentFile(), "tree.xml"));
    }

    public static Genome createChromosomesToPNG(Element parent, String name, String path, Config config, BlocksInformation blocksInformation) {
        Genome genome = new Genome(name);
        XmlUtil.addElement(parent, "name", name);

        try {
            Element gen = new Element("genome");
            BufferedReader input = Transformer.getBufferedInputReader(new File(path, name + ".gen"));

            genome.addChromosomes(input, blocksInformation, config.getInputFormat());

            Drawer picture = new Drawer(config.getInputFormat(), genome);
            picture.writeInPng(path + "/" + name);

            XmlUtil.addElement(gen, "resize", picture.isBigImage(config.getWidthMonitor()));

            parent.addContent(gen);
        } catch (Exception e) {
            log.debug("Not find file " + name + ".gen");
        } finally {
            return genome;
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

    public static BufferedReader getBufferedInputReader(File file) throws FileNotFoundException {
        return new BufferedReader(new InputStreamReader(new FileInputStream(file)));
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

    private ArrayList<Branch> createBranchOfInputTree(ArrayList<Tree> trees) {
        ArrayList<Branch> ans = new ArrayList<Branch>();
        for(Tree tree: trees) {
            tree.createBranches(ans);
        }
        return ans;
    }

    private ArrayList<Branch> readBranchInStats(Config config) throws IOException {
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

        //drop string of table in latex
        for(int i = 0; i < 6; ++i) {
            input.readLine();
        }

        ArrayList<Branch> ans = new ArrayList<Branch>();
        while(!((s = input.readLine()).contains("\\hline"))) {
            if (s.contains("\\emptyset"))
                continue;
            if (s.contains("\\bf"))
                continue;
            String st = s.substring(s.indexOf('{') + 1, s.lastIndexOf('}'));
            String first = st.substring(0, st.indexOf('+')).trim();
            String second = st.substring(st.indexOf('+') + 1).trim();
            String weight = s.substring(s.indexOf('=') + 1, s.indexOf('&', s.indexOf('='))).trim();
            ans.add(new Branch(first, second, Integer.valueOf(weight)));
        }

        return ans;
    }
}
