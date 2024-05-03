package com.pro4d.quickmc.config;


import org.simpleyaml.configuration.file.YamlConfiguration;

public class KeyBuilder implements Cloneable {

    private final YamlConfiguration config;
    private final char separator;
    private final StringBuilder builder;

    public KeyBuilder(YamlConfiguration config, char separator) {
        this.config = config;
        this.separator = separator;
        this.builder = new StringBuilder();
    }

    private KeyBuilder(KeyBuilder keyBuilder) {
        this.config = keyBuilder.config;
        this.separator = keyBuilder.separator;
        this.builder = new StringBuilder(keyBuilder.toString());
    }

    public void parseLine(String line, boolean checkIfExists) {
        line = line.trim();

        String[] currentSplitLine = line.split(":");
        if (currentSplitLine.length > 2) currentSplitLine = line.split(": ");

        String key = currentSplitLine[0].replace("'", "").replace("\"", "");

        if (checkIfExists) {
            //Checks keyBuilder path against config to see if the path is valid.
            //If the path doesn't exist in the config it keeps removing last key in keyBuilder.
            while (!builder.isEmpty() && !config.contains(builder.toString() + separator + key)) {
                removeLastKey();
            }
        }

        //Add the separator if there is already a key inside keyBuilder
        //If currentSplitLine[0] is 'key2' and keyBuilder contains 'key1' the result will be 'key1.' if '.' is the separator
        if (!builder.isEmpty()) builder.append(separator);

        //Appends the current key to keyBuilder
        //If keyBuilder is 'key1.' and currentSplitLine[0] is 'key2' the resulting keyBuilder will be 'key1.key2' if separator is '.'
        builder.append(key);
    }

    public boolean isEmpty() {
        return builder.isEmpty();
    }
    public void clear() {
        builder.setLength(0);
    }

    //Input: 'key1.key2' Result: 'key1'
    public void removeLastKey() {
        if (builder.isEmpty()) return;

        String keyString = builder.toString();
        //Must be enclosed in brackets in case a regex special character is the separator
        String[] split = keyString.split("[" + separator + "]");
        //Makes sure begin index isn't < 0 (error). Occurs when there is only one key in the path
        int minIndex = Math.max(0, builder.length() - split[split.length - 1].length() - 1);
        builder.replace(minIndex, builder.length(), "");
    }

    @Override
    public String toString() {
        return builder.toString();
    }

    @Override
    protected KeyBuilder clone() {
        return new KeyBuilder(this);
    }
}
