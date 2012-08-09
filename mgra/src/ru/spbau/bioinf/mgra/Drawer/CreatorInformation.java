package ru.spbau.bioinf.mgra.Drawer;

import org.apache.log4j.Logger;
import org.jdom.Element;
import ru.spbau.bioinf.mgra.DataFile.BlocksInformation;
import ru.spbau.bioinf.mgra.DataFile.Config;
import ru.spbau.bioinf.mgra.Parser.Genome;
import ru.spbau.bioinf.mgra.Parser.Transformation;
import ru.spbau.bioinf.mgra.Parser.Transformer;
import ru.spbau.bioinf.mgra.Server.XmlUtil;
import ru.spbau.bioinf.mgra.Tree.Node;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

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

    public CreatorInformation(Config config, BlocksInformation blocksInformation) {
        HashMap<HashSet<Character>, Genome> genomes = new HashMap<HashSet<Character>, Genome>();
        createGenomeInformation(config, blocksInformation, genomes);
        createTransformationInformation(config, genomes);
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

    private void createGenomeInformation(Config config, BlocksInformation blocksInformation, HashMap<HashSet<Character>, Genome> genomes) {
        gens = new Element("genomes");
        File[] files = new File(config.getPathParentFile()).listFiles();
        for(File file: files) {
            if (file.getName().endsWith(".gen")) {
                createChromosomesToPNG(gens, file, config, blocksInformation, genomes);
            }
        }
    }

    private void createTransformationInformation(Config config, HashMap<HashSet<Character>, Genome> genomes) {
        trss = new Element("alltransformations");
        File[] files = new File(config.getPathParentFile()).listFiles();
        for(File file: files) {
            if (file.getName().endsWith(".trs")) {
                createTransformationToPNG(trss, genomes.get(Transformer.convertToSet(file.getName().substring(0, file.getName().indexOf('.')))), file);
            }
        }
    }

    private void createChromosomesToPNG(Element parent, File file, Config config, BlocksInformation blocksInformation, HashMap<HashSet<Character>, Genome> genomes) {
        String name = file.getName().substring(0, file.getName().indexOf('.'));
        HashSet<Character> key = Transformer.convertToSet(name);

        if (information.get(key) == null) {
            try {
                DataGenome data = new DataGenome();
                Genome genome = new Genome(name);

                BufferedReader input = getBufferedInputReader(file);
                genome.addChromosomes(input, blocksInformation, config.getInputFormat());

                Drawer picture = new Drawer(config.getInputFormat(), genome);

                Element gen = null;

                try {
                    picture.writeInPng(config.getPathParentFile() + "/" + name);
                    gen = new Element("genome");
                    XmlUtil.addElement(gen, "name", name);
                    XmlUtil.addElement(gen, "resize", picture.isBigImage(config.getWidthMonitor()));
                    data.image = true;
                } catch (IOException e) {
                    log.debug("Not write file picture of " + file.getName());
                }

                data.name = name;
                data.exists = true;

                genomes.put(key, genome);
                information.put(key, data);

                parent.addContent(gen);
            } catch (IOException e) {
                log.debug("Not find file " + file.getName());
            }
        }
    }

    private void createTransformationToPNG(Element parent, Genome genome, File file) {
        try {
            BufferedReader input = getBufferedInputReader(file);
            List<Transformation> transformations = new ArrayList<Transformation>();
            String s;

            while ((s = input.readLine())!=null) {
                transformations.add(new Transformation(s));
            }

            for (Transformation transformation : transformations) {
                transformation.update(genome);
            }

            Element trs = new Element("transformations");

            XmlUtil.addElement(trs, "name", file.getName().substring(0, file.getName().indexOf('.')));

            for (Transformation transformation : transformations) {
                trs.addContent(transformation.toXml());
            }

            parent.addContent(trs);
        } catch (Exception e) {
            log.debug("Not find file " + file.getName());
        }
    }

    private static BufferedReader getBufferedInputReader(File file) throws FileNotFoundException {
        return new BufferedReader(new InputStreamReader(new FileInputStream(file)));
    }
}
