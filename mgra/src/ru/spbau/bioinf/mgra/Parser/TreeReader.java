package ru.spbau.bioinf.mgra.Parser;

import net.sf.saxon.s9api.*;
import org.jdom.Document;
import org.jdom.Element;
import ru.spbau.bioinf.mgra.Server.XmlUtil;

import java.io.*;
import java.util.ArrayList;

public class TreeReader {

    public static void main(String[] args) throws Exception {
       String fileName = "data/mam6/mam6.cfg";
       if (args.length > 0) {
          fileName = args[0];
       }
       new TreeReader(new File(fileName));
    }

    public TreeReader(File cfg) throws IOException, SaxonApiException {
            BufferedReader input = getBufferedInputReader(cfg);
            String s;
            while (!input.readLine().startsWith("[Trees]")) {
            }
            Document doc = new Document();
            Element rootXml = new Element("trees");
            doc.setRootElement(rootXml);

            ArrayList<Tree> trees = new ArrayList<Tree>();

            while ((s = input.readLine().trim()).length() > 0) {
                trees.add(new Tree(s));
            }

            //we can see varios of trees in stats.txt here

            for(Tree tree: trees) {
                rootXml.addContent(tree.toXml(cfg.getParentFile()));
            }

            XmlUtil.saveXml(doc, new File(cfg.getParent(), "tree.xml"));
    }

    public static BufferedReader getBufferedInputReader(File file) throws FileNotFoundException {
        return new BufferedReader(new InputStreamReader(new FileInputStream(file)));
    }
}
