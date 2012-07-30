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

    public void setLengthInBlock(String name) {
        for(Chromosome chromosome: chromosomes) {
            chromosome.setLengthInGene(name);
        }
    }

    public int countOfChromosomes() {
        return chromosomes.size();
    }

    public int getMaxCountGeneInChromosome() {
        int maxCountGen = 0;
        for(Chromosome i: chromosomes) {
            if (maxCountGen < i.getCountGene()) maxCountGen = i.getCountGene();
        }
        return maxCountGen;
    }

    public long getMaxLengthChromosome() {
        long maxLengthGen = 0;
        for(Chromosome chromosome: chromosomes) {
            if (maxLengthGen < chromosome.getLength()) maxLengthGen = chromosome.getLength();
        }
        return maxLengthGen;
    }
}
