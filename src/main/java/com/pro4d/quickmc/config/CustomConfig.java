package com.pro4d.quickmc.config;

import com.pro4d.quickmc.QuickUtils;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.simpleyaml.configuration.comments.CommentType;
import org.simpleyaml.configuration.comments.Commentable;
import org.simpleyaml.configuration.file.YamlConfiguration;
import org.simpleyaml.configuration.implementation.api.YamlImplementationCommentable;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class CustomConfig extends YamlConfiguration implements Commentable {

    private final File file;
    @Getter private final String path, configName;
    @Setter private List<String> exemptions;
    private final Plugin plugin;

    public CustomConfig(Plugin plugin, String name, String directory, List<String> exempt) {
        this.plugin = plugin;
        this.exemptions = exempt;

        if(!plugin.getDataFolder().exists()) plugin.getDataFolder().mkdirs();

        this.configName = name + ".yml";
        this.path = (directory == null || directory.isEmpty()) ? "" : directory + File.separator;

        File dir = new File(plugin.getDataFolder(), path);
        if(!dir.exists()) dir.mkdirs();

        this.file = new File(dir, configName);

        try {
            if(!file.exists()) plugin.saveResource(path + configName, false);

            this.options().useComments(true);
            this.load(file);

            ConfigUpdater.update(plugin, this, exemptions);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public CustomConfig(Plugin plugin, String name, String directory) {
        this(plugin, name, directory, List.of());
    }

    public void saveConfig() {
        QuickUtils.asyncLater(1L, () -> {
            try {
                this.save(this.file);
                ConfigUpdater.update(plugin, this, exemptions);

            } catch (IOException e) {
                Bukkit.getLogger().severe("Failed to save the config: " + configName);
                e.printStackTrace();
            }
        });
    }

    public void reloadConfig() {
        this.options().useComments(true);
        try {
            this.load(file);
            ConfigUpdater.update(plugin, this, exemptions);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public File getConfigFile() {
        return this.file;
    }

    @Override
    public void setComment(String s, String s1, CommentType commentType) {
        if (this.yamlImplementation instanceof YamlImplementationCommentable yamlImpl) {
            yamlImpl.setComment(path, s1, commentType);
        }
    }

    @Override
    public String getComment(String s, CommentType commentType) {
        return this.yamlImplementation instanceof YamlImplementationCommentable
                yamlImplCommentable ? yamlImplCommentable.getComment(path, commentType) : null;
    }

}
