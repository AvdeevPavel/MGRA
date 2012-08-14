package ru.spbau.bioinf.mgra.Parser;

import org.jdom.Element;
import java.util.ArrayList;
import java.util.List;

public class Transformation {
    private ArrayList<Chromosome> beforeChromosomes = new ArrayList<Chromosome>();
    private ArrayList<Chromosome> afterChromosomes = new ArrayList<Chromosome>();
    End[] ends = new End[4];


    public Transformation(String text) {
        String[] data = text.split("[ \t]");
        for (int i = 0; i < ends.length; i++) {
            ends[i] = new End(i, data[i]);
        }
    }

    public void update(Genome genome) throws CloneNotSupportedException {
        List<Chromosome> all = genome.getChromosomes();
        List<Integer> ids = new ArrayList<Integer>();

        for (int i = 0; i < all.size(); i++) {
            Chromosome chromosome =  all.get(i);
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

        for (int i = 0; i < afterChromosomes.size(); i++) {
            Chromosome first = afterChromosomes.get(i);
            for (int j = i + 1; j < afterChromosomes.size(); j++) {
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
            chr.setId(id + 1);
            all.remove(id);
            all.add(id, chr);
            order++;
        }

        if (afterChromosomes.size() > order) {
            Chromosome chr = afterChromosomes.get(order);
            chr.setId(all.size() +1);
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
    }

    public int getSizeBeforeChromosome() {
        return beforeChromosomes.size();
    }

    public ArrayList<Chromosome> getBeforeChromosomes() {
        return beforeChromosomes;
    }

    public End[] getEnds() {
        return ends;
    }

    public int getSizeAfterChromosome() {
        return afterChromosomes.size();
    }

    public ArrayList<Chromosome> getAfterChromosomes() {
        return afterChromosomes;
    }

    public static long getLengthMaxLengthOfChromosomes(ArrayList<Transformation> transformations) {
        if (transformations != null || !transformations.isEmpty()) {
            long maxLengthChromosome = transformations.get(0).beforeChromosomes.get(0).getLength();
            for(Transformation transformation: transformations) {
                for(Chromosome chromosome: transformation.beforeChromosomes) {
                    if (maxLengthChromosome < chromosome.getLength()) {
                        maxLengthChromosome = chromosome.getLength();
                    }
                }

                for(Chromosome chromosome: transformation.afterChromosomes) {
                    if (maxLengthChromosome < chromosome.getLength()) {
                        maxLengthChromosome = chromosome.getLength();
                    }
                }
            }
            return maxLengthChromosome;
        }
        return 0;
    }

    public static Chromosome getMaxLengthOfChromosome(ArrayList<Transformation> transformations) {
        if (transformations != null || !transformations.isEmpty()) {
            Chromosome longChromosome  = transformations.get(0).beforeChromosomes.get(0);
            for(Transformation transformation: transformations) {
                for(Chromosome chromosome: transformation.beforeChromosomes) {
                    if (longChromosome.getLength() < chromosome.getLength()) {
                        longChromosome  = chromosome;
                    }
                }

                for(Chromosome chromosome: transformation.beforeChromosomes) {
                    if (longChromosome.getLength() < chromosome.getLength()) {
                        longChromosome  = chromosome;
                    }
                }
            }
            return longChromosome;
        }
        return null;
    }

    public Element toXml() {
        Element tr = new Element("transformation");

        Element before = new Element("before");
        for (Chromosome chromosome : beforeChromosomes) {
            before.addContent(chromosome.toXml());
        }
        tr.addContent(before);

        for (End end : ends) {
            tr.addContent(end.toXml());
        }

        Element after = new Element("after");
        for (Chromosome chromosome : afterChromosomes) {
            after.addContent(chromosome.toXml());
        }
        tr.addContent(after);

        return tr;
    }

}
