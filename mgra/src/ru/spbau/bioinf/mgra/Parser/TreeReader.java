package ru.spbau.bioinf.mgra.Parser;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import ru.spbau.bioinf.mgra.DataFile.Config;
import ru.spbau.bioinf.mgra.Server.XmlUtil;

import java.io.*;
import java.util.ArrayList;

public class TreeReader {
    private static final Logger log = Logger.getLogger(Node.class);

    public static void main(String[] args) throws Exception {
       String fileName = "data/mam7/mam7.cfg";
       if (args.length > 0) {
          fileName = args[0];
       }
       Config.putStage(3);
       new TreeReader(new File(fileName));
    }

    public TreeReader(File cfg) throws IOException {
            BufferedReader input = getBufferedInputReader(cfg);
            String s;
            while (!input.readLine().startsWith("[Trees]")) {
            }
            Document doc = new Document();
            Element rootXml = new Element("trees");
            doc.setRootElement(rootXml);

            ArrayList<Tree> trees = new ArrayList<Tree>();
            boolean flag = true;
            int countTree = 0;

            while ((s = input.readLine().trim()).length() > 0) {
                Tree tree = new Tree(s, countTree++);
                if (tree.isValid() == false)
                    flag = false;
                trees.add(tree);
            }

            if (!flag) {
                 reconstructedTrees(new File(cfg.getParent(), "stats.txt"), trees);
            }

            for(Tree tree: trees) {
                rootXml.addContent(tree.toXml(cfg.getParentFile()));
            }

            XmlUtil.saveXml(doc, new File(cfg.getParent(), "tree.xml"));
    }

    public static BufferedReader getBufferedInputReader(File file) throws FileNotFoundException {
        return new BufferedReader(new InputStreamReader(new FileInputStream(file)));
    }

    private ArrayList<Tree> reconstructedTrees(File stats, ArrayList<Tree> oldTrees) {
        try {
            ArrayList<String> input = readValidTrees(stats);

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


    private ArrayList<String> readValidTrees(File stats) throws IOException {
        BufferedReader input = getBufferedInputReader(stats);
        int stage = 0;
        String s = "";

        while((s = input.readLine()) != null) {
            if (s.contains("Rearrangement characters")) {
                ++stage;
                if (stage == Config.getStage()) {
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
