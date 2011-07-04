package ru.spbau.bioinf.mgra;

import org.jdom.Element;

import java.util.LinkedList;
import java.util.List;

public class Chromosome {

    private int id;

    private List<Gene> genes = new LinkedList<Gene>();

    public List<Gene> getGenes() {
        return genes;
    }


    public Chromosome(int id, List<Gene> genes) {
        this.id = id;
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
        XmlUtil.addElement(chr, "id", id);
        for (Gene gene : genes) {
            chr.addContent(gene.toXml());
        }
        return chr;
    }
}
