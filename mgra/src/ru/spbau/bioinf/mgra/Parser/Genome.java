package ru.spbau.bioinf.mgra.Parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Genome {
    private String name;
    private String format = "";
    private int maxCountGene = 0;
    private long maxLengthChromosome = 0; 
    private Chromosome lenghestChromosome = null;
    private List<Chromosome> chromosomes = new ArrayList<Chromosome>();

    public Genome(String nameGenome) {
        name = nameGenome;
    }

    public void addChromosomes(BufferedReader input, String inputFormat) throws IOException {
        String s;
        int count = 0;
        while ((s = input.readLine())!=null) {
            s = s.trim();
            if (!s.startsWith("#") && s.length() > 0) {
                chromosomes.add(new Chromosome(count, s, inputFormat, name));
            }
        }
        format = inputFormat;

        if (inputFormat.equals("grimm")) {
            setMaxCountGene();
        } else {
            setMaxLength();
            setPercentInBlocks(getMaxLengthChromosome());
        }
    }

    public List<Chromosome> getChromosomes() {
        return chromosomes;
    }

    public String getFormat() {
        return format;
    }

    public int getCountOfChromosomes() {
        return chromosomes.size();
    }

    public int getMaxCountGeneInChromosome() {
        return maxCountGene;
    }

    public long getMaxLengthChromosome() {
        return maxLengthChromosome;
    }

    public Chromosome getLenghestChromosome() {
        return lenghestChromosome;
    }

    private void setPercentInBlocks(long hundredPercent) {
        for(Chromosome chromosome: chromosomes) {
            chromosome.setPercentInBlocks(hundredPercent);
        }
    }
    
    private void setMaxCountGene() {
        int gen = 0;
        for(Chromosome i: chromosomes) {
            if (gen < i.getCountGene()) gen = i.getCountGene();
        }
        maxCountGene = gen;
    }

    private void setMaxLength() {
        Chromosome maxChromosome = chromosomes.get(0);
        long maxLength = chromosomes.get(0).getLength();
        for(Chromosome chromosome: chromosomes) {
            if (maxLength < chromosome.getLength()) {
                maxLength = chromosome.getLength();
                maxChromosome = chromosome;
            }
        }
        maxLengthChromosome = maxLength;
        lenghestChromosome = maxChromosome;
    }
}

/*public Element toXml() {
    Element genome = new Element("genome");
    for (Chromosome chromosome : chromosomes) {
        genome.addContent(chromosome.toXml());
    }
    return genome;
}*/
