package ru.spbau.bioinf.mgra.Drawer;

import org.apache.log4j.Logger;
import org.jdom.Element;
import ru.spbau.bioinf.mgra.DataFile.BlocksInformation;
import ru.spbau.bioinf.mgra.DataFile.Config;
import ru.spbau.bioinf.mgra.Parser.Genome;
import ru.spbau.bioinf.mgra.Parser.Transformation;
import ru.spbau.bioinf.mgra.Parser.Transformer;
import ru.spbau.bioinf.mgra.Server.JettyServer;
import ru.spbau.bioinf.mgra.Server.XmlUtil;
import ru.spbau.bioinf.mgra.Tree.Node;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class CreatorInformation {
    private static final Logger log = Logger.getLogger(Node.class);
    private Element gens;
    private Element trss;

    class DataGenome {
        String name = "";
        boolean exists = false;
        boolean image = false;
    }

    HashMap<HashSet<Character>, DataGenome> information = new HashMap<HashSet<Character>, DataGenome>();

    public CreatorInformation(Config config, BlocksInformation blocksInformation, PrintWriter out) {
        HashMap<HashSet<Character>, Genome> genomes = new HashMap<HashSet<Character>, Genome>();

        JettyServer.responseStage(out, "Read information in *.gen and create output");
        createGenomeInformation(config, blocksInformation, genomes, out);
        JettyServer.responseStage(out, "Read information in *.trs and create output");
        createTransformationInformation(config, genomes, out);
    }

    public String getGenomeName(HashSet<Character> nameRequest) {
        if (information.get(nameRequest) != null) {
            return information.get(nameRequest).name;
        }
        return null;
    }

    public Element getGenomesXml() {
        return gens;
    }

    public Element getTransformationXml() {
        return trss;
    }

    public boolean existsGen(HashSet<Character> nameRequest) {
        if (information.get(nameRequest) != null) {
            return information.get(nameRequest).exists;
        }
        return false;
    }

    public boolean isCreateImage(HashSet<Character> nameRequest) {
        if (information.get(nameRequest) != null) {
            return information.get(nameRequest).image;
        }
        return false;
    }

    private void createGenomeInformation(Config config, BlocksInformation blocksInformation, HashMap<HashSet<Character>, Genome> genomes, PrintWriter out) {
        gens = new Element("genomes");
        File[] files = new File(config.getPathParentFile()).listFiles();
        for(File file: files) {
            if (file.getName().endsWith(".gen")) {
                createChromosomesToPNG(gens, file, config, blocksInformation, genomes, out);
            }
        }
    }

    private void createTransformationInformation(Config config, HashMap<HashSet<Character>, Genome> genomes, PrintWriter out) {
        trss = new Element("all_transformations");
        File[] files = new File(config.getPathParentFile()).listFiles();
        for(File file: files) {
            if (file.getName().endsWith(".trs")) {
                createTransformationToPNG(trss, file, config, genomes.get(Transformer.convertToSet(file.getName().substring(0, file.getName().indexOf('.')))), out);
            }
        }
    }

    private void createChromosomesToPNG(Element parent, File file, Config config, BlocksInformation blocksInformation, HashMap<HashSet<Character>, Genome> genomes, PrintWriter out) {
        String name = file.getName().substring(0, file.getName().indexOf('.'));
        HashSet<Character> key = Transformer.convertToSet(name);

        if (information.get(key) == null) {
            try {
                BufferedReader input = getBufferedInputReader(file);

                DataGenome data = new DataGenome();
                Genome genome = new Genome(name);
                data.name = name;
                data.exists = true;

                try {
                    genome.addChromosomes(input, blocksInformation, config.getInputFormat());
                } catch (IOException e) {
                    JettyServer.responseErrorServer(out, "Can not read file with " + name + " genome");
                    return;
                }

                Element gen;
                try {
                    Drawer picture = new Drawer(config.getInputFormat(), genome);

                    try {
                        picture.writeInPng(config.getPathParentFile() + "/" + name + "_gen");
                        gen = new Element("genome_png");
                        XmlUtil.addElement(gen, "name", name);
                        XmlUtil.addElement(gen, "resize", picture.isBigImage(config.getWidthMonitor()));
                        data.image = true;
                    } catch (IOException e) {
                        JettyServer.responseErrorServer(out, "Can not save image file with " + name + " genome. Full file name " + file.getName());
                        JettyServer.responseStage(out, "Try save information in xml");
                        gen = genome.toXml(name);
                        log.debug("Can not save image file with genome " + file.getName() + "_gen");
                    }
                } catch (Exception e) {
                    JettyServer.responseInformation(out, "<strong> Image with genome is largest </strong>. Try to create in xml.");
                    gen = genome.toXml(name);
                }
                genomes.put(key, genome);
                information.put(key, data);
                parent.addContent(gen);
            } catch (IOException e) {
                JettyServer.responseInformation(out, "Algorithm not created " + name + " genome");
                log.debug("Algorithm not created " + file.getName());
            }
        }
    }

    private void createTransformationToPNG(Element parent, File file, Config config, Genome genome, PrintWriter out) {
        String name = file.getName().substring(0, file.getName().indexOf('.'));

        try {
            BufferedReader input = getBufferedInputReader(file);
            ArrayList<Transformation> transformations = new ArrayList<Transformation>();

            try {
                String s;
                while ((s = input.readLine()) != null) {
                    transformations.add(new Transformation(s));
                }
            } catch (IOException e) {
                JettyServer.responseErrorServer(out, "Can not read file with " + name + " transformation. Full file name " + file.getName());
                return;
            }

            for (Transformation transformation : transformations) {
                transformation.update(genome);
            }

            Element trs;
            try {
                Drawer picture = new Drawer(config.getInputFormat(), transformations);
                try {
                    picture.writeInPng(config.getPathParentFile() + "/" + name + "_trs");
                    trs = new Element("transformations_png");
                    XmlUtil.addElement(trs, "name", name);
                    XmlUtil.addElement(trs, "resize", picture.isBigImage(config.getWidthMonitor()));
                } catch (IOException e) {
                     JettyServer.responseErrorServer(out, "Can not save image file with " + name + " transformation. Full file name " + file.getName());
                     JettyServer.responseStage(out, "Try save information in xml");
                     trs = new Element("transformations_xml");
                     XmlUtil.addElement(trs, "name", name);
                     log.debug("Can not save image file with transformation " + file.getName() + "_trs");
                }
            } catch (Exception e) {
                JettyServer.responseInformation(out, "<strong> Image with transformations is largest </strong>. Try to create in xml.");
                trs = new Element("transformations_xml");
                XmlUtil.addElement(trs, "name", name);
                for (Transformation transformation : transformations) {
                    trs.addContent(transformation.toXml());
                }
            }
            parent.addContent(trs);
        } catch (Exception e) {
            JettyServer.responseInformation(out, "Algorithm not created " + name + " transformation");
            log.debug("Algorithm not created " + file.getName());
        }
    }

    private static BufferedReader getBufferedInputReader(File file) throws FileNotFoundException {
        return new BufferedReader(new InputStreamReader(new FileInputStream(file)));
    }
}
