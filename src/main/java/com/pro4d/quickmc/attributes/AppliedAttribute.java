package com.pro4d.quickmc.attributes;

import com.pro4d.quickmc.QuickUtils;
import com.pro4d.quickmc.util.CustomConfig;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitTask;

/**
 *  An object containing information about a temporary attribute modification.
 **/
@Getter
public class AppliedAttribute {

    private final Attribute attribute;
    private final AttributeModifier modifier;
    private final FileOptions options;

    private BukkitTask timer;
    @Setter private int time;

    public AppliedAttribute(Attribute attribute, AttributeModifier modifier, FileOptions options) {
        this.attribute = attribute;
        this.modifier = modifier;
        this.options = options;

        timer = null;
        time = 0;
    }

    public void startTimer(LivingEntity target, int time) {
        if(timer != null) timer.cancel();
        setTime(time);
        timer = QuickUtils.syncTimer(0, 20, () -> {
            int t = getTime() - 1;
            if(t <= 0) {
                AttributeManager.removeModifier(target, this, true);
                timer.cancel();
                return;
            }
            setTime(t);
            saveStatsToConfig(target);
        });
    }

    public void pauseTimer(LivingEntity target) {
        if(timer != null) timer.cancel();
        AttributeManager.removeModifier(target, this, false);
        saveStatsToConfig(target);
    }

    public void saveStatsToConfig(LivingEntity target) {
        if(getOptions() == null) return;

        CustomConfig cConfig = getOptions().getConfig();
        String path = getOptions().getPath() + target.getUniqueId() + "." + attribute.getKey().getKey();
        cConfig.getDocument().set(path + getModifier().getName(), time);
        cConfig.saveConfig();
    }

    /**
     * An object containing the config information, to be used by AppliedAttributes, if provided.
     * Providing a FileOptions, with the config file, and intended config path, will allow for
     * the saving of information related to the AppliedAttribute object, to be used across server restarts or crashes.
     **/
    @Getter
    public static class FileOptions {

        private final CustomConfig config;
        private final String path;

        public FileOptions(CustomConfig config, String path) {
            this.config = config;
            this.path = path;
        }
    }

}
