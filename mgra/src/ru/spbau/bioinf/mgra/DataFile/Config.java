package ru.spbau.bioinf.mgra.DataFile;

import java.util.ArrayList;
import java.util.HashMap;

public class Config {
    private static HashMap<String, String> alias = new HashMap<String, String>();
    //private static ArrayList<String> trees = new ArrayList<String>();      //remove if i'm not change
    private static String inputFormat = "";
    private static int stage = 0;
    private static int widthMonitor = 0;

    Config() {
    }

    public static void clear() {
        alias.clear();
        //trees.clear();
    }

    /*public static void putTree(String tree) {
        trees.add(tree);
    } */

    public static void putWidthMonitor(int widthMonitor_) {
        widthMonitor = widthMonitor_;
    }

    public static void putAlias(String key, String value) {
        alias.put(key, value);
    }

    public static void putStage(int stage_) {
        stage = stage_;
    }

    public static void putInputFormat(String format) {
        inputFormat = format;
    }

    public static int getStage() {
        return stage;
    }

    public static String getInputFormat() {
        return inputFormat;
    }

    public static String getAliasName(String name) {
        return alias.get(name);
    }

    public static int getWidthMonitor() {
        return widthMonitor;
    }

    /*public static ArrayList<String> getTrees() {
        return trees;
    } */

}
