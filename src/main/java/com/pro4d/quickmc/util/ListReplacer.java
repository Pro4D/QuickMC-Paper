package com.pro4d.quickmc.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ListReplacer {

    private final Map<String, String> replacer;
    private List<String> inputList;

    public ListReplacer(List<String> inputList, String replace, String replaceWith) {
        this.inputList = inputList;

        Map<String, String> map = new HashMap<>();
        map.put(replace, replaceWith);
        this.replacer = map;
    }

    public ListReplacer(List<String> inputList, Map<String, String> replaceList) {
        this.inputList = inputList;
        this.replacer = replaceList;
    }

    public ListReplacer replaceSubstring(String target, String replacement) {
        this.inputList = inputList.stream()
                .map(str -> str.replace(target, replacement))
                .collect(Collectors.toList());
        return this;
    }

    public List<String> results() {
        List<String> results = new ArrayList<>();
        for (String originalString : inputList) {
            for (Map.Entry<String, String> entry : replacer.entrySet()) {
                originalString = originalString.replace(entry.getKey(), entry.getValue());
            }
            results.add(originalString);
        }
        return results;
    }

}
