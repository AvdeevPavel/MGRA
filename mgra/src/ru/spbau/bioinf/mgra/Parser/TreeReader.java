package ru.spbau.bioinf.mgra.Parser;

import org.jdom.Document;
import org.jdom.Element;
import ru.spbau.bioinf.mgra.Server.XmlUtil;

import java.io.*;

public class TreeReader {

    public static void main(String[] args) throws Exception {
        BufferedReader input = new BufferedReader(new InputStreamReader(new FileInputStream("html/rosaceae/rosaceae.txt")));
        PrintWriter output = new PrintWriter(new OutputStreamWriter(new FileOutputStream(new File("html/rosaceae/rosaceae1.txt")), "UTF-8"));
        String s = input.readLine();
        while(s != null) {
            if ( s.startsWith(">") ) {
                output.println(s);
            } else if ( s.startsWith("scaffold") ) {
                int indexStart = s.indexOf("_") + 1;
                int indexFinish = s.indexOf(":");
                Integer n = Integer.valueOf(s.substring(indexStart, indexFinish));
                s = "prunus.chr" + n.toString() + ":" + s.substring(indexFinish);
                s.trim();
                output.println(s);
            } else if ( s.startsWith("LG") ) {
                int indexStart = s.indexOf("G") + 1;
                int indexFinish = s.indexOf(":");
                Integer n = Integer.valueOf(s.substring(indexStart, indexFinish));
                s = "fragaria.chr" + n.toString() + ":" + s.substring(indexFinish);
                s.trim();
                output.println(s);
            } else if ( s.isEmpty() ) {
                output.println();
            } else if (s.charAt(0) >= '0' && s.charAt(0) <= '9') {
                int indexFinish  = s.indexOf(":");
                Integer n = Integer.valueOf(s.substring(0, indexFinish));
                s = "malus.chr" + n.toString() + ":" + s.substring(indexFinish);
                s.trim();
                output.println(s);
            }
            s = input.readLine();
          }
        output.close();
        input.close();

        /*String fileName = "data/mam6/mam6.cfg";
      if (args.length > 0) {
          fileName = args[0];
      }
      new TreeReader(new File(fileName));*/
    }

    public TreeReader(File cfg) throws IOException{
            BufferedReader input = getBufferedInputReader(cfg);
            String s;
            while (!input.readLine().startsWith("[Trees]")) {}
            Document doc = new Document();
            Element root = new Element("trees");
            doc.setRootElement(root);
            while ((s = input.readLine().trim()).length() > 0) {
                Tree tree = new Tree(null, s);
                root.addContent(tree.toXml(cfg.getParentFile()));
            }
            XmlUtil.saveXml(doc, new File(cfg.getParent(), "tree.xml"));
    }

    public static BufferedReader getBufferedInputReader(File file) throws FileNotFoundException {
        return new BufferedReader(new InputStreamReader(new FileInputStream(file)));
    }
}
