package ru.spbau.bioinf.mgra.DataFile;

import ru.spbau.bioinf.mgra.Server.JettyServer;

import java.awt.*;
import java.io.*;
import java.util.HashMap;
import java.util.Properties;

public class BlocksInformation {
    private HashMap<String, HashMap<Character, Long>> genome = new HashMap<String, HashMap<Character, Long>>();
    private HashMap<String, Color> colorGenome = new HashMap<String, Color>();

    /*color RGB*/
    private int red = 45;
    private int green = 0;
    private int blue = 0;


    public BlocksInformation(Config config) throws IOException {
        if (config.getInputFormat().equals("infercars")) {
            readBloksInformation(new File(config.getPathParentFile(), JettyServer.GENOME_FILE_NAME), config);
        }
    }

    public static void writeGenomeFile(File genomeFileUpload, Properties properties, File datasetDir, Config config, String fileName) throws IOException {
        if (genomeFileUpload != null) {
            genomeFileUpload.renameTo(new File(datasetDir, fileName));
        } else {
            PrintWriter genomeFile = new PrintWriter(new OutputStreamWriter(new FileOutputStream(new File(datasetDir, fileName)), "UTF-8"));
            if (config.getInputFormat().equals("grimm")) {
                int genomeId = 1;
                String key = "genome" + genomeId;
                do {
                    String s = (String) properties.get(key);
                    genomeFile.println(s);
                    genomeId++;
                    genomeFile.println();
                    key = "genome" + genomeId;
                } while (properties.containsKey(key));
            } else {
                String key = "genome";
                String s = (String) properties.get(key);
                genomeFile.println(s);
            }
            genomeFile.close();
        }
        config.resolveFormat();
    }

    public Long getLength(String numberBlock, String key) {
        HashMap<Character, Long> blockSize = genome.get(numberBlock);
        if (blockSize != null) {
            if (key.length() > 1) {
                long length = 0;
                int count = 0;
                for(int i = 0; i < key.length(); ++i) {
                    Long tmp = blockSize.get(key.charAt(i));
                    if (tmp != null) {
                        length += tmp;
                        ++count;
                    }
                }
                if (count == 0)
                    return length;
                else
                    return length / (long) count;
            } else {
                return blockSize.get(key.charAt(0));
            }
        } else {
            return null;
        }
    }

    public Color getColor(String numberBlock) {
        if (colorGenome.get(numberBlock) == null) {
            colorGenome.put(numberBlock, nextColor()) ;
        }
        return colorGenome.get(numberBlock);
    }

    private void putLength(String key, HashMap<Character, Long> value) {
        HashMap<Character, Long> myValue = new HashMap<Character, Long>(value);
        genome.put(key, myValue);
    }

    private Color nextColor() {
        blue += 12;

        if (blue > 255) {
            blue = 0;
            green += 12;
            if (green > 255) {
                green = 0;
                red += 12;
                if (red > 255)
                    red = 20;
            }
        }

        return new Color(red, green, blue);
    }

    private void readBloksInformation(File file, Config config) throws IOException {
        BufferedReader input = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
        String s;
        String nameBlock = "";
        HashMap<Character, Long> map = new HashMap<Character, Long>();

        while((s = input.readLine()) != null) {
            s = s.trim();
            if (s.isEmpty()) {
                if (nameBlock != null && !nameBlock.isEmpty())  {
                    putLength(nameBlock, map);
                    map.clear();
                    nameBlock = "";
                }
            } else if (s.startsWith(">")) {
                nameBlock = s.substring(1);
            } else if (s.startsWith("#")) {
                continue;
            } else {
                int index = s.indexOf(".");
                Character key = config.getAliasName(s.substring(0, index));
                Long left = new Long(s.substring(s.indexOf(":") + 1, s.indexOf("-")));
                Long right = new Long(s.substring(s.indexOf("-") + 1, s.indexOf(" ")));
                map.put(key, right - left);
            }
        }
        input.close();
    }
}
