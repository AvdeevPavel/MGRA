package ru.spbau.bioinf.mgra.Parser;

import org.jdom.Element;
import ru.spbau.bioinf.mgra.DataFile.BlocksInformation;
import ru.spbau.bioinf.mgra.Server.XmlUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Genome {
    private String name;
    private long maxLengthChromosome = 0;
    private Chromosome longChromosome = null;
    private List<Chromosome> chromosomes = new ArrayList<Chromosome>();

    public Genome(String nameGenome) {
        name = nameGenome;
    }

    public void addChromosomes(BufferedReader input, BlocksInformation blocksInformation, String inputFormat) throws IOException {
        String s;
        int count = 0;
        while ((s = input.readLine())!=null) {
            s = s.trim();
            if (!s.startsWith("#") && s.length() > 0) {
                chromosomes.add(new Chromosome(count++, s, blocksInformation, name, inputFormat));
            }
        }

        setMaxLength();
        if (inputFormat.equals("infercars"))
            setPercentInBlocks();
    }

    public List<Chromosome> getChromosomes() {
        return chromosomes;
    }

    public int getNumberOfChromosomes() {
        return chromosomes.size();
    }

    public long getLengthMaxLengthOfChromosomes() {
        return maxLengthChromosome;
    }

    public Chromosome getMaxLengthOfChromosome() {
        return longChromosome;
    }

    protected void setMaxLength() {
        if (chromosomes != null || !chromosomes.isEmpty()) {
            longChromosome  = chromosomes.get(0);
            maxLengthChromosome = chromosomes.get(0).getLength();
            for(Chromosome chromosome: chromosomes) {
                if (maxLengthChromosome < chromosome.getLength()) {
                    maxLengthChromosome = chromosome.getLength();
                    longChromosome  = chromosome;
                }
            }
        }
    }

    private void setPercentInBlocks() {
        for(Chromosome chromosome: chromosomes) {
            chromosome.setPercentInBlocks(maxLengthChromosome);
        }
    }

    public Element toXml(String name) {
        Element genome = new Element("genome_xml");
        XmlUtil.addElement(genome, "name", name);
        for (Chromosome chromosome : chromosomes) {
            genome.addContent(chromosome.toXml());
        }
        return genome;
    }
}
