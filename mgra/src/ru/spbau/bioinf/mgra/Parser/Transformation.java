package ru.spbau.bioinf.mgra.Parser;

import org.jdom.Element;
import ru.spbau.bioinf.mgra.DataFile.BlocksInformation;
import ru.spbau.bioinf.mgra.Server.XmlUtil;

import java.util.ArrayList;
import java.util.List;

public class Transformation {
    private ArrayList<Chromosome> beforeChromosomes = new ArrayList<Chromosome>();
    private ArrayList<Chromosome> afterChromosomes = new ArrayList<Chromosome>();
    private Chromosome longChromosome;
    End[] ends = new End[4];


    public Transformation(String text) {
        String[] data = text.split("[ \t]");
        for (int i = 0; i < ends.length; i++) {
            ends[i] = new End(i, data[i]);
        }
    }

    public void update(Genome genome,  BlocksInformation blocksInformation, String inputFormat) throws CloneNotSupportedException {
        List<Chromosome> all = genome.getChromosomes();
        List<Integer> ids = new ArrayList<Integer>();

        for (int i = 0; i < all.size(); i++) {
            Chromosome chromosome = all.get(i);
            for (End end : ends) {
                if (chromosome.contains(end)) {
                    this.beforeChromosomes.add(chromosome.clone());
                    ids.add(i);
                    break;
                }
            }
        }

        for (Chromosome chromosome : beforeChromosomes) {
            chromosome.clearEnds();
            for (End end : ends) {
                chromosome.mark(end);
            }
        }

        ArrayList<Chromosome> temp = new ArrayList<Chromosome>(beforeChromosomes);
        for (Chromosome chromosome : temp) {
            chromosome.split(afterChromosomes);
        }

        for (int i = 0; i < afterChromosomes.size(); ++i) {
            Chromosome first = afterChromosomes.get(i);
            for (int j = i + 1; j < afterChromosomes.size(); ++j) {
                Chromosome second = afterChromosomes.get(j);
                if (first.join(second)) {
                    afterChromosomes.remove(j);
                    j = i;
                }
            }
        }

        int order = 0;
        while (afterChromosomes.size() > order && ids.size() > order) {
            Chromosome chr = afterChromosomes.get(order);
            int id = ids.get(order);
            chr.setId(id);
            all.remove(id);
            all.add(id, chr);
            order++;
        }

        if (afterChromosomes.size() > order) {
            Chromosome chr = afterChromosomes.get(order);
            chr.setId(all.size());
            all.add(chr);
        }

        if (ids.size() > order) {
            int id = ids.get(order);
            all.remove(id);
        }

        for (Chromosome chromosome : afterChromosomes) {
            chromosome.clearEnds();
            for (End end : ends) {
                chromosome.mark(end);
            }
        }

        updateChromosome(genome.getName(), blocksInformation, inputFormat);
    }

    public ArrayList<Chromosome> getBeforeChromosomes() {
        return beforeChromosomes;
    }

    public ArrayList<Chromosome> getAfterChromosomes() {
        return afterChromosomes;
    }

    public int getCountChromosome() {
        return beforeChromosomes.size() + afterChromosomes.size();
    }

    public Chromosome getMaxLengthOfChromosome() {
        return longChromosome;
    }

    public String resolveTypeRearrangement() {
        if (ends[2].getType() == EndType.OO && ends[3].getType() == EndType.OO) {
            return "fusion";
        }

        if (ends[0].getType() == EndType.OO && ends[1].getType() == EndType.OO) {
            return "fission";
        }

        if (beforeChromosomes.size() < afterChromosomes.size()) {
            return "fission";
        }

        if (beforeChromosomes.size() > afterChromosomes.size()) {
            return "fusion/translocation";
        }

        if (beforeChromosomes.size() == afterChromosomes.size()) {
            return "reversal";
        }

        String st = "";
        for(End end: ends) {
            st += (end.getId() + end.getType());
        }
        return st;
    }

    public void toXml(Element parent) {
        Element before = new Element("before");
        for (Chromosome chromosome : beforeChromosomes) {
            before.addContent(chromosome.toXml());
        }
        parent.addContent(before);

        for (End end : ends) {
            parent.addContent(end.toXml());
        }

        Element after = new Element("after");
        for (Chromosome chromosome : afterChromosomes) {
            after.addContent(chromosome.toXml());
        }
        parent.addContent(after);
    }

    private void updateChromosome(String name, BlocksInformation blocksInformation, String inputFormat) {
        for (Chromosome chromosome : beforeChromosomes) {
            chromosome.setLengthInGene(name, blocksInformation, inputFormat);
            chromosome.setColorInGene(blocksInformation);
        }

        for (Chromosome chromosome : afterChromosomes) {
            chromosome.setColorInGene(blocksInformation);
            chromosome.setLengthInGene(name, blocksInformation, inputFormat);
        }

        setMaxLengthOfChromosome();

        for (Chromosome chromosome : beforeChromosomes) {
            if (inputFormat.equals("infercars")) {
                chromosome.setPercentInBlocks(longChromosome.getLength());
            }
        }

        for (Chromosome chromosome : afterChromosomes) {
            if (inputFormat.equals("infercars")) {
                chromosome.setPercentInBlocks(longChromosome.getLength());
            }
        }
    }

    private void setMaxLengthOfChromosome() {
        this.longChromosome  = beforeChromosomes.get(0);
        for(Chromosome chromosome: beforeChromosomes) {
            if (this.longChromosome.getLength() < chromosome.getLength()) {
                this.longChromosome  = chromosome;
            }
        }

        for(Chromosome chromosome: afterChromosomes) {
            if (this.longChromosome.getLength() < chromosome.getLength()) {
                this.longChromosome  = chromosome;
            }
        }
    }
}
