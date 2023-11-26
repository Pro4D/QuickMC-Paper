package com.pro4d.quickmc.util;

import com.pro4d.quickmc.QuickMC;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import lombok.Getter;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;

@Getter
public class CustomConfig {

    private final String configName;
    private YamlDocument document;

    public CustomConfig(String name, String directory, String versioning) {
        Plugin plugin = QuickMC.getSourcePlugin();
        if(!plugin.getDataFolder().exists()) plugin.getDataFolder().mkdirs();
        this.configName = name + ".yml";

        String dirName = directory == null ? "" : directory;
        String path = File.separator + dirName + File.separator + configName;
        File file = new File(plugin.getDataFolder() + File.separator + dirName, configName);

        try {
            UpdaterSettings.Builder builder = UpdaterSettings.builder();
            builder.setOptionSorting(UpdaterSettings.OptionSorting.SORT_BY_DEFAULTS);
            if(versioning != null &&
                    !versioning.isEmpty()) builder.setVersioning(new BasicVersioning(versioning));

            document = YamlDocument.create(file, getClass().getResourceAsStream(path),
                    GeneralSettings.builder().setKeyFormat(GeneralSettings.KeyFormat.OBJECT).build(),
                    LoaderSettings.builder().setAutoUpdate(true).build(),
                    DumperSettings.DEFAULT, builder.build());

            document.update();
            document.save();

        } catch (IOException e) {
            QuickMC.getSourcePlugin().logger.severe("Failed to load/create: " + configName);
        }
    }

    public CustomConfig(String name, String directory) {
        this(name, directory, "");
    }

    public void saveConfig() {
        try {
            document.save();
            document.update();

        } catch (IOException e) {
            QuickMC.getSourcePlugin().logger.severe("Failed to save the config: " + configName);
        }
    }

    public void reloadConfig() {
        try {
            document.reload();
            document.update();
//            document.save();

        } catch (IOException e) {
            QuickMC.getSourcePlugin().logger.severe("Failed to reload the config: " + configName);
        }
    }

}
