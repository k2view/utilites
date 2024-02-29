package com.k2view.broadway.actors.masking.format;

import java.util.*;
import java.util.stream.Collectors;

public class FormatUtils {

    record FormatAndValue(List<Character> format, String value) {};

    /**
     * Separate format and clean value
     * @param whiteList - defines white list of characters of the input value
     * @param inputValue
     */
    static FormatAndValue unwrap(String whiteList, String inputValue) {
        List<Character> format = new ArrayList<>();
        StringBuilder cleanValue = new StringBuilder();

        Set<Character> chars = whiteList.chars().mapToObj(e -> (char) e).collect(Collectors.toCollection(HashSet::new));
        inputValue.chars().forEach(c -> {
            if (chars.contains((char) c)) {
                cleanValue.append((char) c);
                format.add(null);
            } else {
                format.add((char) c);
            }
        });

        return new FormatAndValue(format, cleanValue.toString());
    }

    static String wrap(List<Character> format, String value) {
        StringBuilder outputValue = new StringBuilder();
        int i = 0;
        for (Character c : format) {
            if (c == null) {
                if (i < value.length()) {
                    outputValue.append(value.charAt(i));
                }
                i++;
            } else {
                outputValue.append((char) c);
            }
        }
        // add all remain characters from masked value
        if (i < value.length()) {
            outputValue.append(value.substring(i));
        }
        return outputValue.toString();
    }
}
