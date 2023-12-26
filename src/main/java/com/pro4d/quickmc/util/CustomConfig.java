package com.pro4d.quickmc.util;

import com.pro4d.quickmc.QuickMC;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;

@Getter
public class CustomConfig {

    private final File file;
    private final String configName;
    private YamlDocument document;

    public CustomConfig(Plugin plugin, String name, String directory, String versioning) {
        if(!plugin.getDataFolder().exists()) plugin.getDataFolder().mkdirs();
        this.configName = name + ".yml";

        String dirName = directory == null ? "" : File.separator + directory + File.separator;
        this.file = new File(plugin.getDataFolder() + dirName, configName);

        String path = directory == null ?  configName : directory + File.separator + configName;
        try {

            UpdaterSettings.Builder updaterBuilder = UpdaterSettings.builder();
            updaterBuilder.setAutoSave(false);
            if(versioning != null && !versioning.isEmpty()) {
                updaterBuilder.setVersioning(new BasicVersioning(versioning));
            }

            document = YamlDocument.create(file, plugin.getResource(path),
                    GeneralSettings.builder()
                            .setKeyFormat(GeneralSettings.KeyFormat.STRING)
                            .setUseDefaults(false)
                            .build(),
                    LoaderSettings.DEFAULT, DumperSettings.DEFAULT, updaterBuilder.build());

        } catch (IOException e) {
            QuickMC.getSourcePlugin().logger.severe("Failed to load/create: " + configName);
        }
    }

    public CustomConfig(Plugin plugin, String name, String directory) {
        this(plugin, name, directory, "");
    }

    public void saveConfig() {
        try {
            document.save();

        } catch (IOException e) {
            Bukkit.getLogger().severe("Failed to save the config: " + configName);
        }
    }

    public void reloadConfig() {
        try {
            document.reload();

        } catch (IOException e) {
            Bukkit.getLogger().severe("Failed to reload the config: " + configName);
        }
    }

}
