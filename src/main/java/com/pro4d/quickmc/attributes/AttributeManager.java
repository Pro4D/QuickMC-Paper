package com.pro4d.quickmc.attributes;

import com.pro4d.quickmc.events.AttributeApplyEvent;
import com.pro4d.quickmc.events.AttributeRemoveEvent;
import com.pro4d.quickmc.util.CustomConfig;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.LivingEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AttributeManager {

    public static void addModifier(LivingEntity target, AppliedAttribute applying, int time) {
        AttributeInstance instance = target.getAttribute(applying.getAttribute());
        if(instance == null) return;
        instance.addModifier(applying.getModifier());

        if(time != -1) applying.startTimer(target, time);

        Bukkit.getServer().getPluginManager().callEvent(new AttributeApplyEvent(target, applying, time));
        if(applying.getOptions() != null) applying.saveStatsToConfig(target);
    }

    public static void removeModifier(LivingEntity target, AppliedAttribute applying, boolean delete) {
        AttributeInstance instance = target.getAttribute(applying.getAttribute());
        if(instance == null) return;
        instance.removeModifier(applying.getModifier());

        Bukkit.getServer().getPluginManager().callEvent(new AttributeRemoveEvent(target, applying));

        AppliedAttribute.FileOptions options = applying.getOptions();
        if(options == null || !delete) return;
        CustomConfig config = options.getConfig();
        String path = options.getPath() + target.getUniqueId();
        config.getDocument().set(path + applying.getModifier().getName(), null);
        config.saveConfig();
    }

    public static boolean hasModifier(LivingEntity entity, Attribute attribute, AttributeModifier modifier) {
        AttributeInstance instance = entity.getAttribute(attribute);
        if(instance == null) return false;
        for(AttributeModifier mod : instance.getModifiers()) {
            if(mod.getName().equalsIgnoreCase(modifier.getName()) ||
                    mod.getUniqueId().equals(modifier.getUniqueId())) return true;
        }

        return false;
    }
    public static List<AttributeModifier> getAllModifiersFromAttribute(LivingEntity entity, Attribute attribute) {
        List<AttributeModifier> modifiers = new ArrayList<>();
        AttributeInstance instance = entity.getAttribute(attribute);
        if(instance == null) return modifiers;

        modifiers.addAll(instance.getModifiers());
        return modifiers;
    }


    public static void healEntity(LivingEntity entity, double amount) {
        double sum = entity.getHealth() + amount;
        entity.setHealth(Math.min(sum, getEntityMaxHealth(entity, false, true)));
    }

    /*
    * Return an entity's maximum health.
    * param def - return the default vanilla value, ignoring any modifications
    * param modifiers - overrides def param, returns the value with all modifications
    * */
    public static double getEntityMaxHealth(LivingEntity entity, boolean def, boolean modifiers) {
        AttributeInstance attribute = entity.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if(attribute == null) return 0;

        if(modifiers) return attribute.getValue();
        return def ? Objects.requireNonNull(entity.getType().getDefaultAttributes().
                getAttribute(Attribute.GENERIC_MAX_HEALTH)).getValue() : attribute.getBaseValue();
    }

}
