package ru.spbau.bioinf.mgra.DataFile;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

public class Config {

    private class InformationGenome{
        char uniqueName;
        String aliasName;
        InformationGenome(char key, String value) {
            uniqueName = key;
            aliasName = value;
        }
    }

    private static final String CFG_FILE_NAME = "mgra.cfg";
    private ArrayList<InformationGenome> nameGenome = new ArrayList<InformationGenome>();
    private String inputFormat = "";
    private ArrayList<String> trees = new ArrayList<String>();
    private int stage = 0;
    private boolean useTarget = false;
    private String target = null;
    private String completion = null;
    private int widthMonitor = 0;
    private String pathParentFile = "";

    private HashMap<Character, String> alias = new HashMap<Character, String>();

    private char shortName[] = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q',
            'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};

    public Config(String path, Properties properties) {
        /*[Genomes]*/
        int aliasId = 1;
        String key = "alias" + aliasId;
        do {
            String aliasName = properties.getProperty(key);
            String name = properties.getProperty("name" + aliasId);

            nameGenome.add(new InformationGenome(name, aliasName));
            String[] tmp = aliasName.split(" ");
            for(String data: tmp) {
                alias.put(data, name);
            }

            ++aliasId;
            key = "alias" + aliasId;
        } while (properties.containsKey(key));


        /*[Blocks]*/
        inputFormat = properties.getProperty("useFormat").trim();

        /*[Trees]*/
        String inputTrees = properties.getProperty("trees");
        String[] treesString = inputTrees.split("\n");
        for(String s: treesString) {
            s = s.trim();
            trees.add(s);
        }

        /*[Algorithm]*/
        stage = new Integer(properties.getProperty("stages"));
        useTarget = "1".equals(properties.getProperty("useTarget"));

        if (useTarget) {
            String st = properties.getProperty("target");
            if (st == null)
                target = st;
            else if (st.isEmpty())
                target = null;
            else
                target = st.trim();
        }

        if (useTarget) {
            completion = properties.getProperty("completion");
        }

        pathParentFile = path;
        widthMonitor = new Integer(properties.getProperty("widthMonitor"));
    }

    public String getAliasName(String name) {
        return alias.get(name);
    }

    public static String getNameFile() {
        return CFG_FILE_NAME;
    }

    public ArrayList<String> getNameGenomes() {
        ArrayList<String> st = new ArrayList<String>();
        for(InformationGenome genome: nameGenome) {
            st.add(genome.uniqueName + " " + genome.aliasName);
        }
        return st;
    }

    public String getInputFormat() {
        return inputFormat;
    }

    public ArrayList<String> getTrees() {
        return trees;
    }

    public int getStage() {
        return stage;
    }

    public boolean isUseTarget() {
        return useTarget;
    }

    public String getTarget() {
        return target;
    }

    public String getPathParentFile() {
        return pathParentFile;
    }

    public int getWidthMonitor() {
        return widthMonitor;
    }

    public int getNumberOfGenome() {
        return nameGenome.size();
    }

    public void createFile(boolean isShowTree) throws IOException {
        PrintWriter cfgFile = new PrintWriter(new OutputStreamWriter(new FileOutputStream(new File(pathParentFile, CFG_FILE_NAME)), "UTF-8"));

        cfgFile.println("[Genomes]");
        for(InformationGenome genome: nameGenome) {
            cfgFile.println(genome.uniqueName + " " + genome.aliasName);
        }
        cfgFile.println();

        cfgFile.println("[Blocks]");
        cfgFile.println("format " + inputFormat);
        cfgFile.println("file genome.txt");
        cfgFile.println();

        cfgFile.println("[Trees]");
        if (isShowTree) {
            for(String tree: trees) {
                cfgFile.println(tree);
            }
        }
        cfgFile.println();

        cfgFile.println("[Algorithm]");
        cfgFile.println();

        cfgFile.println("stages " + stage);
        cfgFile.println();

        if (useTarget) {
            if (target != null)
                cfgFile.println("target " + target);
            cfgFile.println();
        }

        cfgFile.println("[Graphs]");
        cfgFile.println();

        cfgFile.println("filename stage");
        cfgFile.println();

        cfgFile.println("colorscheme set19");
        cfgFile.println();

        if (useTarget) {
            cfgFile.println("[Completion]");
            cfgFile.println(completion);
            cfgFile.println();
        }

        cfgFile.close();
    }
}
