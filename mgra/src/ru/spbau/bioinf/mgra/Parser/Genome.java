package ru.spbau.bioinf.mgra.Parser;

import org.jdom.Element;
import ru.spbau.bioinf.mgra.Drawer.Drawer;

import java.util.ArrayList;
import java.util.List;

public class Genome {
    private List<Chromosome> chromosomes = new ArrayList<Chromosome>();

    public List<Chromosome> getChromosomes() {
        return chromosomes;
    }

    public void addChromosome(Chromosome chr) {
        chromosomes.add(chr);
    }

    public Element toXml() {
        Element genome = new Element("genome");
        for (Chromosome chromosome : chromosomes) {
            genome.addContent(chromosome.toXml());
        }
        return genome;
    }

    public void toPng(String nameFile, String inputFormat) {
        if (inputFormat.equals("grimm")) {
            Drawer picture = new Drawer(inputFormat, chromosomes.size(), getMaxCountGeneInChromosome());
            picture.writeInPng(nameFile);
        } else {
            Drawer picture = new Drawer(inputFormat, chromosomes.size(), getMaxLengthChromosome());
            picture.writeInPng(nameFile);
        }
    }

    public int countOfChromosomes() {
        return chromosomes.size();
    }

    private int getMaxCountGeneInChromosome() {
        int maxCountGen = 0;
        for(Chromosome i: chromosomes) {
            if (maxCountGen < i.getCountGene()) maxCountGen = i.getCountGene();
        }
        return maxCountGen;
    }

    private long getMaxLengthChromosome() {
        long maxLengthGen = 0;
        for(Chromosome i: chromosomes) {
            if (maxLengthGen < i.getLength()) maxLengthGen = i.getLength();
        }
        return maxLengthGen;
    }
}
