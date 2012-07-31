package ru.spbau.bioinf.mgra.DataFile;

import java.util.HashMap;

public class GenomeInInferCar {
    private static HashMap<String, HashMap<String, Long>> genome = new HashMap<String, HashMap<String, Long>>();

    public static void putHashMap(String key, HashMap<String, Long> value) {
        genome.put(key, value);
    }

    public static Long getLength(String numberBlock, String key) {
        HashMap<String, Long> blockSize = genome.get(numberBlock);
        if (blockSize != null) {
            if (key.length() > 1) {
                long length = 0;
                int count = 0;
                for(int i = 0; i < key.length(); ++i) {
                    Long tmp = blockSize.get(String.valueOf(key.charAt(i)));
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
                return blockSize.get(key);
            }
        } else {
            return null;
        }
    }


    public static void clear() {
        genome.clear();
    }
}
