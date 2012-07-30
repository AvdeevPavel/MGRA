package ru.spbau.bioinf.mgra.DataFile;

import java.util.HashMap;

public class Config {
    private static HashMap<String, String> alias = new HashMap<String, String>();
    private static String inputFormat = "";
    private static int stage = 0;
    private static int heightMonitor = 0;
    private static int widthMonitor = 0;

    Config() {
    }

    public static void putHeightMonitor(int heightMonitor_) {
        heightMonitor = heightMonitor_;
    }

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

    public static int getHeightMonitor() {
        return heightMonitor;
    }

    public static int getWidthMonitor() {
        return widthMonitor;
    }

}
