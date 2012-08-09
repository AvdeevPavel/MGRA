package ru.spbau.bioinf.mgra.DataFile;

import java.util.HashMap;

public class BlocksInformation {
    private HashMap<String, HashMap<Character, Long>> genome = new HashMap<String, HashMap<Character, Long>>();

    public void putHashMap(String key, HashMap<Character, Long> value) {
        HashMap<Character, Long> myValue = new HashMap<Character, Long>(value);
        genome.put(key, myValue);
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
}
