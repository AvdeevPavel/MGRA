package ru.spbau.bioinf.mgra.Parser;

import org.apache.log4j.Logger;
import org.jdom.Element;
import org.jdom.Document;
import ru.spbau.bioinf.mgra.DataFile.BlocksInformation;
import ru.spbau.bioinf.mgra.DataFile.Config;
import ru.spbau.bioinf.mgra.Drawer.Drawer;
import ru.spbau.bioinf.mgra.Server.XmlUtil;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Transformer {
    private static final Logger log = Logger.getLogger(Node.class);

    public static void main(String[] args) throws Exception {
       String fileName = "data/toy";
       if (args.length > 0) {
          fileName = args[0];
       }


       new Transformer();
    }

    public Transformer() {
        ArrayList<String> stringTrees = new ArrayList<String>();
        //stringTrees.add("(ABCD,FCDE)");
        //stringTrees.add("((A,B),(C,D))");
        //stringTrees.add("(((A,B),(C,D)),FCDE)");
        //stringTrees.add("(FC,DE)");
        //stringTrees.add("(F,C)");
        //stringTrees.add("(D,E)");
        //stringTrees.add("((AB,(C,D)),((F,C),DE))");
        //stringTrees.add("((AB,CD),(FC,DE))");
        //stringTrees.add("(C,D)");
        //stringTrees.add("(A,B)");
        //stringTrees.add("(D,E)");
        //stringTrees.add("(HMR,DCPW)");
        //stringTrees.add("(H,(M,R))");
        //stringTrees.add("((D,C),HRMPW)");
        //stringTrees.add("((P,W),HRMDC)");

        ArrayList<Tree> trees = new ArrayList<Tree>();
        int countTree = 0;

        for(String s: stringTrees) {
            Tree tree = new Tree(s, countTree++);
            trees.add(tree);
        }

        trees = merge(trees);

        System.out.println("After stage 1");
        for(Tree tree: trees) {
            System.out.println(tree.toString());
        }

        System.out.println("After stage 2");
        System.out.println("Is full tree:");
        for(Tree tree: trees) {
            System.out.println(tree.isFullTree(5));
        }

        System.out.println();
        System.out.println("Is complete transformation:");
        for(Tree tree: trees) {
            System.out.println(tree.isCompleteTransformation("lalal")); //change to path
        }

        System.out.println("After stage 3");

        System.out.println("After stage 4");
    }

    //stage 1 done
    //stage 2 done
    //stage 3
    //stage 4 done
    public Transformer(Config config, BlocksInformation blocksInformation) throws IOException {
        Document doc = new Document();

        if (config.isUseTarget() && config.getTarget() != null) {
            Element rootXml = new Element("targetgenome");
            doc.setRootElement(rootXml);
            createChromosomesToPNG(rootXml, config.getTarget(), config.getPathParentFile(), config, blocksInformation);
        } else {
            Element rootXml = new Element("trees");
            doc.setRootElement(rootXml);

            ArrayList<String> stringTrees = config.getTrees();
            ArrayList<Tree> trees = new ArrayList<Tree>();
            int countTree = 0;

            for(String s: stringTrees) {
                Tree tree = new Tree(s, countTree++);
                trees.add(tree);
            }

            trees = merge(trees);

            System.out.println("After stage 1");
            for(Tree tree: trees) {
                System.out.println(tree.toString());
            }

            System.out.println("After stage 2");
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

            System.out.println("After stage 4");


            /*if (!flag) {
                reconstructedTrees(trees, config);
            } */

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
            log.error("Problems with " + name + ".gen file.", e);
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
            log.error("Problems with " + name + ".trs file.", e);
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

    private ArrayList<Tree> reconstructedTrees(ArrayList<Tree> oldTrees, Config config) {
        try {
            ArrayList<String> input = readBranchInStats(config);

            for(String s: input) {
                System.out.println(s);
            }

            ArrayList<Tree> newTrees = new ArrayList<Tree>();
            return newTrees;
        } catch (Exception e) {
            log.error("Problem read file stats.txt with information for reconstructed trees and branches", e);
            return null;
        }
    }

    /*private ArrayList<String> createBranchInInputTree(ArrayList<Tree> trees) {
        ArrayList<String> outputSet = new ArrayList<String>();
        return outputSet;
    } */

    private ArrayList<String> readBranchInStats(Config config) throws IOException {
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

        ArrayList<String> ans = new ArrayList<String>();
        while(!((s = input.readLine()).contains("\\hline"))) {
            if (s.contains("\\emptyset"))
                continue;
            if (s.contains("\\bf"))
                continue;
            ans.add(s.substring(s.indexOf('{') + 1, s.lastIndexOf('}')));
        }

        return ans;
    }
}
