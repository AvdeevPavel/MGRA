package ru.spbau.bioinf.mgra.Parser;

import org.jdom.Element;
import ru.spbau.bioinf.mgra.Server.XmlUtil;

import java.util.LinkedList;
import java.util.List;

public class Gene implements Cloneable {
    private String id;
    private Direction direction;
    private long length;
    private double percent = 0;
    private List<End> ends = new LinkedList<End>();

    public Gene(String id, Direction direction) {
        this.id = id;
        this.direction = direction;
    }

    public Gene clone() throws CloneNotSupportedException {
        Gene clone = (Gene) super.clone();
        List<End> newEnds = new LinkedList<End>();
        for(End end: ends) {
            newEnds.add(end.clone());
        }
        clone.ends = newEnds;
        return clone;
    }

    public void setPercent(double percent_) {
        percent = percent_;
    }

    public void setLength(long length_) {
        length = length_;
    }

    public void addEnd(End end) {
        ends.add(end);
    }

    public void reverse() {
        direction = direction.reverse();
    }

    public Element toXml() {
        Element gene = new Element("gene");
        XmlUtil.addElement(gene, "id", id);
        XmlUtil.addElement(gene, "direction", direction.toString());
        for (End end : ends) {
            gene.addContent(end.toXml());
        }

        return gene;
    }

    public void clearEnds() {
        ends.clear();
    }

    public double getPercent() {
        return percent;
    }

    public long getLength() {
        return length;
    }

    public String getId() {
        return id;
    }

    public int getSide(End end) {
        return getDirection().getSide(end);
    }

    public End getEnd(int side) {
        for (End end : ends) {
            if (getSide(end) == side) {
                return end;
            }
        }
        return null;
    }

    public List<End> getEnds() {
        return ends;
    }

    public Direction getDirection() {
        return direction;
    }

    public char getCharDirection() {
        if (direction.toString().equals("minus"))
            return '-';
        else if (direction.toString().equals("plus"))
            return '+';
        return '0';
    }

}
