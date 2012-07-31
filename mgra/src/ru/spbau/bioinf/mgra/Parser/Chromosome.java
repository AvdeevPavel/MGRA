package ru.spbau.bioinf.mgra.Parser;

import org.jdom.Element;
import ru.spbau.bioinf.mgra.DataFile.GenomeInInferCar;
import ru.spbau.bioinf.mgra.Server.XmlUtil;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Chromosome {

    private int id;
    private List<Gene> genes = new LinkedList<Gene>();

    public List<Gene> getGenes() {
        return genes;
    }

    public Chromosome(List<Gene> genes) {
        this.genes = genes;
    }

    public Chromosome(int id, String s) {
        this.id = id;
        String[] data = s.split(" ");
        for (String v : data) {
           if (!v.startsWith("$")) {
               Gene gene = new Gene(v.substring(1), Direction.getDirection(v.charAt(0)));
               genes.add(gene);
           }
        }

    }

    public void setLengthInGene(String name) {
        for(Gene gene: genes) {
            gene.setLength(GenomeInInferCar.getLength(gene.getId(), name));
        }
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCountGene() {
        return genes.size();
    }

    public long getLength() {
        long length = 0;

        for(Gene gene: genes) {
            length += gene.getLength();
        }

        return length;
    }

    public void setPercentInBlocks(long hundredPercent) {
        for(Gene gene: genes) {
            double curPercent = (double) gene.getLength() / (double) hundredPercent;
            gene.setPercent(curPercent);
        }
    }

    public double getMinPercentBlock() {
       double minPercentGene = genes.get(0).getPercent();
       for(Gene gene: genes) {
            if (minPercentGene > gene.getPercent())  minPercentGene = gene.getPercent();
       }
       return minPercentGene;
    }

    public boolean contains(End end) {
        for (Gene gene : genes) {
            if (gene.getId().equals(end.getId())) {
                return true;
            }
        }
        return false;
    }

    public void mark(End end) {
        for (Gene gene : genes) {
            if (gene.getId().equals(end.getId())) {
                gene.addEnd(end);
            }
        }
    }

    public void clearEnds() {
        for (Gene gene : genes) {
            gene.clearEnds();
        }
    }

    public void split(List<Chromosome> parts) {
        List<Gene> cur = new ArrayList<Gene>();
        for (Gene g : genes) {
            cur.add(g);
            for (End end : g.getEnds()) {
                if (g.getSide(end) == 1) {
                    parts.add(new Chromosome(cur));
                    cur = new ArrayList<Gene>();
                }
            }
        }
        if (cur.size() > 0) {
            parts.add(new Chromosome(cur));
        }
    }


    public boolean join(Chromosome other) {
        List<Gene> og = other.getGenes();
        End firstLeftEnd = getLeftEnd();
        End firstRightEnd = getRightEnd();
        End secondLeftEnd = other.getLeftEnd();
        End secondRightEnd = other.getRightEnd();
        if (firstLeftEnd != null) {
           if (secondLeftEnd != null) {
               if (secondLeftEnd.getColorType() == firstLeftEnd.getColorType()) {
                   for (Gene g : other.getGenes()) {
                       g.reverse();
                       genes.add(0, g);
                   }
                   return true;
               }
           }
           if (secondRightEnd != null) {
                if (secondRightEnd.getColorType() == firstLeftEnd.getColorType()) {
                    for (int i = 0; i < og.size(); i++) {
                        genes.add(i, og.get(i));
                    }
                    return true;
                }
           }
        }

        if (firstRightEnd != null) {
           if (secondLeftEnd != null) {
               if (secondLeftEnd.getColorType() == firstRightEnd.getColorType()) {
                   for (Gene g : og) {
                       genes.add(g);
                   }
                   return true;
               }
           }
           if (secondRightEnd != null) {
                if (secondRightEnd.getColorType() == firstRightEnd.getColorType()) {
                    for (int i = og.size()-1; i >=0; i--) {
                        Gene g = og.get(i);
                        g.reverse();
                        genes.add(g);
                    }
                    return true;
                }
           }
        }
        return false;
    }

    private End getLeftEnd() {
        return genes.get(0).getEnd(-1);
    }

    private End getRightEnd() {
        return genes.get(genes.size() - 1).getEnd(1);
    }



    public Element toXml() {
        Element chr = new Element("chromosome");
        XmlUtil.addElement(chr, "id", id);
        for (Gene gene : genes) {
            chr.addContent(gene.toXml());
        }
        return chr;
    }
}
