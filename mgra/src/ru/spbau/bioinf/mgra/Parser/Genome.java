package ru.spbau.bioinf.mgra.Parser;

import ru.spbau.bioinf.mgra.DataFile.BlocksInformation;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Genome {
    private String name;
    private long maxLengthChromosome = 0;
    private Chromosome longChromosome = null;
    private String inputFormat = "";
    private List<Chromosome> chromosomes = new ArrayList<Chromosome>();

    public Genome(String nameGenome) {
        name = nameGenome;
    }

    public void addChromosomes(BufferedReader input, BlocksInformation blocksInformation, String inputFormat_) throws IOException {
        String s;
        int count = 0;
        inputFormat = inputFormat_;
        while ((s = input.readLine())!=null) {
            s = s.trim();
            if (!s.startsWith("#") && s.length() > 0) {
                chromosomes.add(new Chromosome(count, s, blocksInformation, name));
            }
        }

        setMaxLength();
        if (inputFormat.equals("infercars"))
            setPercentInBlocks();
    }

    public String getFormat() {
        return inputFormat;
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
        longChromosome  = chromosomes.get(0);
        maxLengthChromosome = chromosomes.get(0).getLength();
        for(Chromosome chromosome: chromosomes) {
            if (maxLengthChromosome < chromosome.getLength()) {
                maxLengthChromosome = chromosome.getLength();
                longChromosome  = chromosome;
            }
        }
    }

    private void setPercentInBlocks() {
        for(Chromosome chromosome: chromosomes) {
            chromosome.setPercentInBlocks(maxLengthChromosome);
        }
    }
}
