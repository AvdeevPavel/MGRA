package ru.spbau.bioinf.mgra.Parser;

import org.jdom.Element;
import ru.spbau.bioinf.mgra.Server.XmlUtil;

public class End implements Cloneable{
    private String id;
    private int color;
    private EndType type;

    public End(int color, String s) {
        this.color = color;
        if ("oo".equals(s)) {
            id = "";
            type = EndType.OO;
        } else {
            int lastChar = s.length() - 1;
            id = s.substring(0, lastChar);
            type = EndType.getType(s.charAt(lastChar));
        }
    }

    public End clone() throws CloneNotSupportedException {
        return (End) super.clone();
    }

    public String getId() {
        return id;
    }

    public int getColorType() {
        return color % 2;
    }

    public EndType getType() {
        return type;
    }

    public Element toXml() {
        Element end = new Element("end");
        XmlUtil.addElement(end, "id", id);
        XmlUtil.addElement(end, "type", type.toString());
        XmlUtil.addElement(end, "color", color);
        return end;
    }

    public int getColor() {
        return color;
    }
}
