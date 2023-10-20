package com.pro4d.quickmc;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.LivingEntity;

import java.util.ArrayList;
import java.util.List;

public class QuickAttributeManager {

    public static boolean addModifier(LivingEntity entity, Attribute attribute, AttributeModifier modifier) {
        AttributeInstance instance = entity.getAttribute(attribute);
        if(instance == null) return false;
        instance.addModifier(modifier);
        return true;
    }

    public static boolean removeModifier(LivingEntity entity, AttributeModifier modifier) {
        boolean success = false;
        for(Attribute attribute : Attribute.values()) {
            AttributeInstance instance = entity.getAttribute(attribute);
            if(instance == null) continue;
            instance.removeModifier(modifier);
            success = true;
        }

        return success;
    }
    public static boolean removeModifier(LivingEntity entity, Attribute attribute, AttributeModifier modifier) {
        AttributeInstance instance = entity.getAttribute(attribute);
        if(instance == null) return false;
        instance.removeModifier(modifier);
        return true;
    }

    public static boolean hasModifier(LivingEntity entity, AttributeModifier modifier) {
        for(Attribute attribute : Attribute.values()) {
            AttributeInstance instance = entity.getAttribute(attribute);
            if(instance == null) continue;
            for(AttributeModifier mod : instance.getModifiers()) {
                if(mod.getName().equalsIgnoreCase(modifier.getName()) ||
                        mod.getUniqueId().equals(modifier.getUniqueId())) return true;
            }
        }
        return false;
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

    public static List<AttributeModifier> getAllModifiers(LivingEntity entity) {
        List<AttributeModifier> modifiers = new ArrayList<>();
        for(Attribute attribute : Attribute.values()) {
            AttributeInstance instance = entity.getAttribute(attribute);
            if(instance != null) modifiers.addAll(instance.getModifiers());
        }
        return modifiers;
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
        return def ? attribute.getDefaultValue() : attribute.getBaseValue();
    }

}
