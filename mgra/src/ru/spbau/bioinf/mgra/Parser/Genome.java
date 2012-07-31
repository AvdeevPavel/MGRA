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

    public void setPercentInBlocks(long hundredPercent) {
        for(Chromosome chromosome: chromosomes) {
            chromosome.setPercentInBlocks(hundredPercent);
        }
    }

    public double getMinPercentBlock() {
        double minSizePercentBlock = chromosomes.get(0).getMinPercentBlock();
        for(Chromosome chromosome: chromosomes) {
            if (minSizePercentBlock > chromosome.getMinPercentBlock()) minSizePercentBlock = chromosome.getMinPercentBlock();
        }
        return minSizePercentBlock;
    }

    public Chromosome getMaxChromosome() {
        Chromosome maxChromosome = chromosomes.get(0);
        for(Chromosome chromosome: chromosomes) {
            if (maxChromosome.getLength() < chromosome.getLength()) maxChromosome = chromosome;
        }
        return maxChromosome;
    }

    public long getMaxLengthChromosome() {
        long maxChromosome = chromosomes.get(0).getLength();
        for(Chromosome chromosome: chromosomes) {
            if (maxChromosome < chromosome.getLength()) maxChromosome = chromosome.getLength();
        }
        return maxChromosome;
    }
}
