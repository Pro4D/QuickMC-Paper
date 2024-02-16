package com.pro4d.quickmc.util;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

@Getter
public class CustomConfig {

    private final File file;
    private final String configName;
    private FileConfiguration ymlConfig;

    public CustomConfig(Plugin plugin, String name, String directory) {
        if(!plugin.getDataFolder().exists()) plugin.getDataFolder().mkdirs();

        this.configName = name + ".yml";
        String path = (directory == null || directory.isEmpty()) ? "" : directory + File.separator;

        File dir = new File(plugin.getDataFolder(), path);
        if(!dir.exists()) dir.mkdirs();

        this.file = new File(dir, configName);

        try {
            if(!file.exists()) plugin.saveResource(path + configName, false);

            this.ymlConfig = YamlConfiguration.loadConfiguration(file);

            ymlConfig.setDefaults(YamlConfiguration.loadConfiguration(new InputStreamReader(plugin.getResource(path + configName), "UTF-8")));

            ymlConfig.options().copyHeader();
            ymlConfig.options().copyDefaults(true);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveConfig() {
        try {
            ymlConfig.save(file);
        } catch (IOException e) {
            Bukkit.getLogger().severe("Failed to save the config: " + configName);
            e.printStackTrace();
        }
    }

    public void reloadConfig() {
        try {
            ymlConfig.load(file);

        } catch (IOException e) {
            Bukkit.getLogger().severe("Failed to reload the config: " + configName);
        } catch (InvalidConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

}
