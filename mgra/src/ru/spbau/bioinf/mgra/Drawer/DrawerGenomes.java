package ru.spbau.bioinf.mgra.Drawer;

import org.apache.log4j.Logger;
import ru.spbau.bioinf.mgra.DataFile.BlocksInformation;
import ru.spbau.bioinf.mgra.DataFile.Config;
import ru.spbau.bioinf.mgra.Parser.Genome;
import ru.spbau.bioinf.mgra.Parser.Transformer;
import ru.spbau.bioinf.mgra.Tree.Branch;
import ru.spbau.bioinf.mgra.Tree.Node;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class DrawerGenomes {
    private static final Logger log = Logger.getLogger(Node.class);

    class Data {
        Genome genome = null; //delete if change transformation and put to png
        boolean image = false;
        boolean resize = false ;
    }

    HashMap<HashSet<Character>, Data> genomes = new HashMap<HashSet<Character>, Data>();
    //HashMap<String, Data> transformation = new HashMap<String, Data>();

    public DrawerGenomes(ArrayList<String> nameVertex, Config config, BlocksInformation blocksInformation) {
        for(String nameFile: nameVertex) {
            createChromosomesToPNG(nameFile, config, blocksInformation);
        }
    }

    public void addGenomes(String[] nameVertex, Config config, BlocksInformation blocksInformation) {
        for(String nameFile: nameVertex) {
            createChromosomesToPNG(nameFile, config, blocksInformation);
        }
    }

    /*public static void createTransformationToPNG(Element parent, Genome genome, String name, String path) {
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
    } */

    public Genome getGenome(HashSet<Character> name) {
        return genomes.get(name).genome;
    }

    public boolean isCreateImage(HashSet<Character> name) {
        return genomes.get(name).image;
    }

    public boolean isResize(HashSet<Character> name) {
        return genomes.get(name).resize;
    }

    private void createChromosomesToPNG(String name, Config config, BlocksInformation blocksInformation) {
        HashSet<Character> key = Branch.convertToSet(name);
        if (genomes.get(key) == null) {
            Data data = new Data();
            data.genome = new Genome(name);

            try {
                BufferedReader input = getBufferedInputReader(new File(config.getPathParentFile(), name + ".gen"));
                data.genome.addChromosomes(input, blocksInformation, config.getInputFormat());
                Drawer picture = new Drawer(config.getInputFormat(), data.genome);
                data.image = picture.writeInPng(config.getPathParentFile() + "/" + name);
                data.resize = picture.isBigImage(config.getWidthMonitor());
            } catch (Exception e) {
                log.debug("Not find file " + name + ".gen");
            } finally {
                genomes.put(key, data);
            }
        }
    }

    private static BufferedReader getBufferedInputReader(File file) throws FileNotFoundException {
        return new BufferedReader(new InputStreamReader(new FileInputStream(file)));
    }
}
