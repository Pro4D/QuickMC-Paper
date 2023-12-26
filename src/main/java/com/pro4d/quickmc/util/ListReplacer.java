package com.pro4d.quickmc.util;

import java.util.List;
import java.util.stream.Collectors;

public class ListReplacer {

    private List<String> inputList;
    private ListReplacer(List<String> inputList) {
        this.inputList = inputList;
    }

    public static ListReplacer withList(List<String> inputList) {
        return new ListReplacer(inputList);
    }

    public ListReplacer replaceSubstring(String target, String replacement) {
        this.inputList = inputList.stream()
                .map(str -> str.replace(target, replacement))
                .collect(Collectors.toList());
        return this;
    }

    public List<String> getResult() {
        return inputList;
    }

}
