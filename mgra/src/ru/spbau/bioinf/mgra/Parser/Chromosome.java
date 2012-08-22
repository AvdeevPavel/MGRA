package ru.spbau.bioinf.mgra.Parser;

import org.jdom.Element;
import ru.spbau.bioinf.mgra.DataFile.BlocksInformation;
import ru.spbau.bioinf.mgra.Server.XmlUtil;

import java.awt.*;
import java.util.LinkedList;
import java.util.List;

public class Chromosome implements Cloneable {
    private int id;
    private long length = 0;
    private List<Gene> genes = new LinkedList<Gene>();

    public Chromosome(LinkedList<Gene> genes) {
        this.genes = genes;
    }

    public Chromosome(int id, String s, BlocksInformation blocksInformation, String name, String inputFormat) {
        this.id = id;
        String[] data = s.split(" ");
        for (String v : data) {
           if (!v.startsWith("$")) {
               Gene gene = new Gene(v.substring(1), Direction.getDirection(v.charAt(0)));
               genes.add(gene);
           }
        }
        setLengthInGene(name, blocksInformation, inputFormat);
        setColorInGene(blocksInformation);
    }

    public Chromosome clone() throws CloneNotSupportedException {
        Chromosome clone = (Chromosome) super.clone();
        LinkedList<Gene> newGenes = new LinkedList<Gene>();

        for(Gene gene: genes) {
            newGenes.add(gene.clone());
        }

        clone.genes = newGenes;

        return clone;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setPercentInBlocks(long hundredPercent) {
        for(Gene gene: genes) {
            double curPercent = (double) gene.getLength() / (double) hundredPercent;
            gene.setPercent(curPercent);
        }
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

    public Element toXml() {
        Element chr = new Element("chromosome");
        XmlUtil.addElement(chr, "id", id + 1);
        for (Gene gene : genes) {
            chr.addContent(gene.toXml());
        }
        return chr;
    }


    public void split(List<Chromosome> parts) throws CloneNotSupportedException {
        LinkedList<Gene> cur = new LinkedList<Gene>();
        for (Gene g : genes) {
            cur.add(g.clone());
            for (End end : g.getEnds()) {
                if (g.getSide(end) == 1) {
                    parts.add(new Chromosome(cur));
                    cur = new LinkedList<Gene>();
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

    public int getId() {
        return this.id;
    }

    public List<Gene> getGenes() {
        return genes;
    }

    public long getLength() {
        return length;
    }

    private End getLeftEnd() {
        return genes.get(0).getEnd(-1);
    }

    private End getRightEnd() {
        return genes.get(genes.size() - 1).getEnd(1);
    }

    private void setLengthInGene(String name, BlocksInformation blocksInformation, String inputFormat) {
        if (inputFormat.equals("grimm")) {
            length = genes.size();
        } else {
            length = 0;
            for(Gene gene: genes) {
                gene.setLength(blocksInformation.getLength(gene.getId(), name));
                length += gene.getLength();
            }
        }
    }

    private void setColorInGene(BlocksInformation blocksInformation) {
        for(Gene gene: genes) {
            gene.setColor(blocksInformation.getColor(gene.getId()));
        }
    }
}
