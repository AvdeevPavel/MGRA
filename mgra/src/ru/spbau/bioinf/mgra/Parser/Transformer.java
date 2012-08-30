package ru.spbau.bioinf.mgra.Parser;

import org.apache.log4j.Logger;
import org.jdom.Element;
import org.jdom.Document;

import ru.spbau.bioinf.mgra.DataFile.BlocksInformation;
import ru.spbau.bioinf.mgra.DataFile.Config;
import ru.spbau.bioinf.mgra.Server.JettyServer;
import ru.spbau.bioinf.mgra.Server.XmlUtil;

import java.io.*;
import java.util.ArrayList;

public class Transformer {
    private static final Logger log = Logger.getLogger(JettyServer.class);

    public static void createGenome(String nameGenome, Config config) throws IOException {
        BlocksInformation blocksInformation = new BlocksInformation(config);
        Document doc = new Document();
        Element rootXml = createImagesForChromosomes(nameGenome, config, blocksInformation);
        if (rootXml != null) {
            doc.setRootElement(rootXml);
            XmlUtil.saveXml(doc, new File(config.getPathParentFile(), nameGenome + "_gen.xml"));
        }
    }

    public static void createTransformationToXml(String nameTrs, Config config) throws IOException {
        BlocksInformation blocksInformation = new BlocksInformation(config);
        Document doc= new Document();
        Element rootXml = createXmlForRearrangement(nameTrs, config, blocksInformation);
        if (rootXml != null) {
            doc.setRootElement(rootXml);
            XmlUtil.saveXml(doc, new File(config.getPathParentFile(), nameTrs + "_trs.xml"));
        }
    }

    public static String createTransformationToPng(String nameTrs, Config config, int id) throws IOException {
        BlocksInformation blocksInformation = new BlocksInformation(config);
        return createImagesForRearrangement(nameTrs, config, blocksInformation, id);
    }

    private static Element createImagesForChromosomes(String nameGenome, Config config, BlocksInformation blocksInformation) {
        try {
           BufferedReader input = getBufferedInputReader(new File(config.getPathParentFile(), nameGenome + ".gen"));
           Genome genome = new Genome(nameGenome);
           genome.addChromosomes(input, blocksInformation, config.getInputFormat());
           input.close();

           Element gen;
           try {
               Drawer picture = new Drawer(config.getInputFormat(), genome);
               picture.writeInPng(config.getPathParentFile() + "/" + nameGenome + "_gen");
               gen = new Element("genome_png");
               XmlUtil.addElement(gen, "name", nameGenome);
               XmlUtil.addElement(gen, "resize", picture.isBigImage(config.getWidthMonitor()));
               log.debug("Done save " + nameGenome + "_gen.png genome image file");
           } catch (OutOfMemoryError e) {
               gen = genome.toXml(nameGenome);
               log.debug("Image with genome is largest. Try to create in xml.");
           } catch (NegativeArraySizeException e) {
               gen = genome.toXml(nameGenome);
               log.debug("Image with genome have bag size. Try to create in xml.");
           } catch (Exception e) {
               gen = genome.toXml(nameGenome);
               log.debug("Problem with " + nameGenome + " " + e + ". Try to create in xml.");
           }
           return  gen;
       } catch (IOException e) {
           log.debug("Problem with genome " + nameGenome + ". Name problem: " + e);
           return null;
       }
    }

    private static Element createXmlForRearrangement(String nameTrs, Config config, BlocksInformation blocksInformation) {
        try {
            BufferedReader input = getBufferedInputReader(new File(config.getPathParentFile(), nameTrs + ".gen"));
            Genome genome = new Genome(nameTrs);
            genome.addChromosomes(input, blocksInformation, config.getInputFormat());
            input.close();

            input = getBufferedInputReader(new File(config.getPathParentFile(), nameTrs + ".trs"));
            ArrayList<Transformation> transformations = new ArrayList<Transformation>();
            String s;
            while ((s = input.readLine()) != null) {
                transformations.add(new Transformation(s));
            }
            input.close();

            for (Transformation transformation : transformations) {
                transformation.update(genome, blocksInformation, config.getInputFormat());
            }

            Element trs = new Element("transformation");
            XmlUtil.addElement(trs, "name", nameTrs);
            int id = 1;
            for (Transformation transformation : transformations) {
                Element rear = new Element("rearrangement");
                transformation.toXml(rear);
                XmlUtil.addElement(rear, "id", id++);
                trs.addContent(rear);
            }
            return trs;
        } catch (IOException e) {
            log.debug("Problem to transform with " + nameTrs + ". Name problem: " + e);
            return null;
        } catch (CloneNotSupportedException e) {
            log.error("Problem with clone " + nameTrs);
            return null;
        }
    }

    private static String createImagesForRearrangement(String nameTrs, Config config, BlocksInformation blocksInformation, int idRear) {
        try {
            BufferedReader input = getBufferedInputReader(new File(config.getPathParentFile(), nameTrs + ".gen"));
            Genome genome = new Genome(nameTrs);
            genome.addChromosomes(input, blocksInformation, config.getInputFormat());
            input.close();

            input = getBufferedInputReader(new File(config.getPathParentFile(), nameTrs + ".trs"));
            ArrayList<Transformation> transformations = new ArrayList<Transformation>();
            String s;
            while ((s = input.readLine()) != null) {
                transformations.add(new Transformation(s));
            }
            input.close();

            int id = 1;
            for (Transformation transformation : transformations) {
                transformation.update(genome, blocksInformation, config.getInputFormat());
                if (idRear == id)
                    break;
                ++id;
            }

            File transDir = new File(config.getPathParentFile(), nameTrs + "_trs");
            if (!transDir.exists()) {
                transDir.mkdir();
            }
            Transformation transformation = transformations.get(idRear - 1);

            Drawer picture = new Drawer(config.getInputFormat(), transformation);
            picture.writeInPng(transDir.getAbsolutePath() + "/" + id);
            String answer;

            if (picture.isBigImage(config.getWidthMonitor())) {
                answer = "<img src=\"" + nameTrs + "_trs/" + idRear + ".png\" width=\"100%\"></img>\n";
            } else {
                answer = "<img src=\"" + nameTrs + "_trs/" + idRear + ".png\"></img>\n";
            }
            answer = answer.concat("<div align=\"center\"><input name=\"download_image\" type=\"button\" value=\"Save as image\" onclick=\"window.location.href='" + nameTrs + "_trs/" + idRear + ".png'\"/></div>");
            return answer;
        } catch (OutOfMemoryError e) {
            log.debug("Image with " + nameTrs + " rearrangement is largest.");
        } catch (NegativeArraySizeException e) {
            log.debug("Image with " + nameTrs + " rearrangement have bad size.");
        } catch (CloneNotSupportedException e) {
            log.error("Problem with clone " + nameTrs);
        } catch (Exception e) {
            log.debug("Problem to transform with " + nameTrs + ". Name problem: " + e);
        }
        return null;
    }

    private static BufferedReader getBufferedInputReader(File file) throws FileNotFoundException {
        return new BufferedReader(new InputStreamReader(new FileInputStream(file)));
    }
}
