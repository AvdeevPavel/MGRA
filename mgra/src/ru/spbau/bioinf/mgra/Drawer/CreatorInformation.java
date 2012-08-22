package ru.spbau.bioinf.mgra.Drawer;

import org.apache.log4j.Logger;
import org.jdom.Element;
import ru.spbau.bioinf.mgra.DataFile.BlocksInformation;
import ru.spbau.bioinf.mgra.DataFile.Config;
import org.jdom.Document;
import ru.spbau.bioinf.mgra.Parser.Genome;
import ru.spbau.bioinf.mgra.Parser.Transformation;
import ru.spbau.bioinf.mgra.Server.JettyServer;
import ru.spbau.bioinf.mgra.Server.XmlUtil;
import java.io.*;
import java.util.ArrayList;

public class CreatorInformation {
    private static final Logger log = Logger.getLogger(JettyServer.class);

    public static void createGenome(String nameGenome, Config config) throws IOException {
        BlocksInformation blocksInformation = new BlocksInformation(config);
        Document doc = new Document();
        Element rootXml = createImagesForChromosomes(new File(config.getPathParentFile(), nameGenome + ".gen"), config, blocksInformation);
        if (rootXml != null) {
            doc.setRootElement(rootXml);
            XmlUtil.saveXml(doc, new File(config.getPathParentFile(), nameGenome + "_gen.xml"));
        }
    }

    public static void createTransformation(String nameTrs, Config config) throws IOException {
        BlocksInformation blocksInformation = new BlocksInformation(config);
        Document doc= new Document();
        Element rootXml = createImagesForRearrangement(new File(config.getPathParentFile(), nameTrs + ".trs"), config, blocksInformation);

        if (rootXml != null) {
            doc.setRootElement(rootXml);
            XmlUtil.saveXml(doc, new File(config.getPathParentFile(), nameTrs + "_trs.xml"));
        }
    }

    private static Element createImagesForChromosomes(File file, Config config, BlocksInformation blocksInformation) {
        String name = file.getName().substring(0, file.getName().indexOf('.'));
        try {
           BufferedReader input = getBufferedInputReader(file);
           Genome genome = new Genome(name);

           try {
               genome.addChromosomes(input, blocksInformation, config.getInputFormat());
           } catch (IOException e) {
               log.debug("Can not read file with " + name + " genome");
               return null;
           }

           Element gen;
           try {
               Drawer picture = new Drawer(config.getInputFormat(), genome);
               try {
                   picture.writeInPng(config.getPathParentFile() + "/" + name + "_gen");
                   gen = new Element("genome_png");
                   XmlUtil.addElement(gen, "name", name);
                   XmlUtil.addElement(gen, "resize", picture.isBigImage(config.getWidthMonitor()));
                   log.debug("Done save " + name + " genome file");
               } catch (IOException e) {
                   gen = genome.toXml(name);
                   log.debug("Can not save image file with genome " + file.getName() + "_gen");
               }
           } catch (OutOfMemoryError e) {
               gen = genome.toXml(name);
           } catch (NegativeArraySizeException e) {
               gen = genome.toXml(name);
           } catch (Exception e) {
               gen = genome.toXml(name);
           } finally {
               input.close();
           }
           return  gen;
       } catch (IOException e) {
           log.debug("Algorithm not created " + file.getName());
           return null;
       }
    }

    private static Element createImagesForRearrangement(File file, Config config, BlocksInformation blocksInformation) {
        String name = file.getName().substring(0, file.getName().indexOf('.'));
        try {
            BufferedReader input = getBufferedInputReader(new File(file.getParentFile().getAbsolutePath(), name + ".gen"));
            Genome genome = new Genome(name);

            try {
                genome.addChromosomes(input, blocksInformation, config.getInputFormat());
            } catch (IOException e) {
                log.error("Can not read file with " + name + " genome");
                return null;
            }
            input.close();

            input = getBufferedInputReader(file);
            ArrayList<Transformation> transformations = new ArrayList<Transformation>();

            try {
                String s;
                while ((s = input.readLine()) != null) {
                    transformations.add(new Transformation(s));
                }
            } catch (IOException e) {
                log.error("Can not read file with " + name + " transformation. Full file name " + file.getName());
                return null;
            }
            input.close();

            for (Transformation transformation : transformations) {
                transformation.update(genome);
            }

            Element trs = new Element("transformation");
            XmlUtil.addElement(trs, "name", name);

            int id = 1;
            for (Transformation transformation : transformations) {
                Element rear = new Element("rearrangement_xml");
                XmlUtil.addElement(rear, "id", id++);
                transformation.toXml(rear);
                trs.addContent(rear);
            }

            return trs;
        } catch (IOException e) {
            log.debug("Algorithm not created " + file.getName());
            return null;
        } catch (CloneNotSupportedException e) {
            log.error("Problem with clone " + file.getName());
            return null;
        }
    }

   /*
            Element trs;
            try {
                Drawer picture = new Drawer(config.getInputFormat(), transformations);
                try {
                    picture.writeInPng(config.getPathParentFile() + "/" + name + "_trs");
                    trs = new Element("transformations_png");
                    XmlUtil.addElement(trs, "name", name);
                    XmlUtil.addElement(trs, "resize", picture.isBigImage(config.getWidthMonitor()));
                    JettyServer.responseInformation(out, "Done save " + name + " transformation file");
                } catch (IOException e) {
                     JettyServer.responseErrorServer(out, "Can not save image file with " + name + " transformation. Full file name " + file.getName());
                     JettyServer.responseStage(out, "Try save information in xml");
                     trs = new Element("transformations_xml");
                     XmlUtil.addElement(trs, "name", name);
                     log.debug("Can not save image file with transformation " + file.getName() + "_trs");
                }
            } catch (OutOfMemoryError e) {
                JettyServer.responseInformation(out, "<strong> Image with transformations is largest </strong>. Try to create in xml.");
                trs = new Element("transformations_xml");
                XmlUtil.addElement(trs, "name", name);
                for (Transformation transformation : transformations) {
                    trs.addContent(transformation.toXml());
                }
            } catch (NegativeArraySizeException e) {
                JettyServer.responseInformation(out, "<strong> Image with genome is largest </strong>. Try to create in xml.");
                trs = new Element("transformations_xml");
                XmlUtil.addElement(trs, "name", name);
                for (Transformation transformation : transformations) {
                    trs.addContent(transformation.toXml());
                }
            } catch (Exception e) {
                JettyServer.responseInformation(out, "<strong> Image with genome is largest </strong>. Try to create in xml.");
                trs = new Element("transformations_xml");
                XmlUtil.addElement(trs, "name", name);
                for (Transformation transformation : transformations) {
                    trs.addContent(transformation.toXml());
                }
            }
  */

    private static BufferedReader getBufferedInputReader(File file) throws FileNotFoundException {
        return new BufferedReader(new InputStreamReader(new FileInputStream(file)));
    }
}
