package com.pro4d.quickmc.attributes;

import com.pro4d.quickmc.QuickMC;
import com.pro4d.quickmc.QuickUtils;
import com.pro4d.quickmc.events.AttributeApplyEvent;
import de.tr7zw.changeme.nbtapi.NBTCompound;
import de.tr7zw.changeme.nbtapi.NBTCompoundList;
import de.tr7zw.changeme.nbtapi.NBTEntity;
import de.tr7zw.changeme.nbtapi.NBTFile;
import de.tr7zw.changeme.nbtapi.iface.ReadWriteNBT;
import lombok.Getter;
import org.bukkit.attribute.Attributable;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

@Getter
public class AttributeManager {

//    private final Map<UUID, Set<AttributeModifier>> entityModifiers;
//    public AttributeManager() {
//        entityModifiers = new HashMap<>();
//    }

    public static void setBukkitAttribute(LivingEntity target, Attribute attribute, double value) {
        AttributeInstance instance = target.getAttribute(attribute);
        assert instance != null;
        instance.setBaseValue(value);
    }
    public static void setTimedBukkitAttribute(LivingEntity target, Attribute attribute, double value, double restore, long duration) {
        AttributeInstance instance = target.getAttribute(attribute);
        assert instance != null;
        instance.setBaseValue(value);

        QuickUtils.syncLater(duration, () -> setBukkitAttribute(target, attribute, restore));
    }

    public static void setAttribute(LivingEntity target, Attribute attribute, double value) {
        AttributeApplyEvent event = new AttributeApplyEvent(target, attribute, value);
        QuickMC.getSourcePlugin().getServer().getPluginManager().callEvent(event);
        if(event.isCancelled()) return;

        attribute = event.getAttribute();
        value = event.getValue();

        NBTCompound compound = getCompound(target);
        getNBT(compound, attribute).setDouble("Base", value);

        AttributeInstance instance = target.getAttribute(attribute);
        assert instance != null;
        instance.setBaseValue(value);

        if(!(compound instanceof NBTFile nbtFile)) return;
        CompletableFuture.runAsync(() -> {
            try {
                nbtFile.save();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
    public static void setTimedAttribute(LivingEntity target, Attribute attribute, double value, double restore, long duration) {
        AttributeApplyEvent event = new AttributeApplyEvent(target, attribute, value);
        QuickMC.getSourcePlugin().getServer().getPluginManager().callEvent(event);
        if(event.isCancelled()) return;

        attribute = event.getAttribute();
        value = event.getValue();

        NBTCompound compound = getCompound(target);
        getNBT(compound, attribute).setDouble("Base", value);

        AttributeInstance instance = target.getAttribute(attribute);
        assert instance != null;
        instance.setBaseValue(value);

        if(compound instanceof NBTFile nbtFile) {
            CompletableFuture.runAsync(() -> {
                try {
                    nbtFile.save();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }

        Attribute a = attribute;
        QuickUtils.syncLater(duration, () -> setAttribute(target, a, restore));
    }

//    public static void addAttributeModifier(LivingEntity target, Attribute attribute, String name, double value) {
//        AttributeInstance instance = target.getAttribute(attribute);
//        assert instance != null;
//        instance.setBaseValue(value);
//    }
//    public static void addTimedAttributeModifier(LivingEntity target, Attribute attribute, String name, double value, double restore, long duration) {
//        AttributeInstance instance = target.getAttribute(attribute);
//        assert instance != null;
//        instance.setBaseValue(value);
//
//        QuickUtils.syncLater(duration, () -> setBukkitAttribute(target, attribute, restore));
//    }
//    public static void removeAttributeModifier(LivingEntity target, Attribute attribute, String name) {
//        AttributeInstance instance = target.getAttribute(attribute);
//        assert instance != null;
//        instance.setBaseValue(value);
//
//        QuickUtils.syncLater(duration, () -> setBukkitAttribute(target, attribute, restore));
//    }

    public static NBTCompound getCompound(LivingEntity entity) {
        if(!(entity instanceof Player player)) {
            return new NBTEntity(entity);
        } else if(player.isOnline()) return new NBTEntity(entity);

        NBTFile file;
        try {
            String path = entity.getServer().getWorldContainer().getPath();
            path = path.substring(0, path.length() - 1);

            String defWorld = entity.getServer().getWorlds().get(0).getName();
            file = new NBTFile(new File(path + defWorld + "/playerdata/" + player.getUniqueId() + ".dat"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return file;
    }

    public static double getAttributeValue(LivingEntity entity, Attribute attribute) {
        AttributeInstance instance = entity.getAttribute(attribute);
        return instance != null ? instance.getValue() : -1;
    }

    public static double getDefaultValue(LivingEntity entity, Attribute attribute) {
        Attributable defAttributes = entity.getType().getDefaultAttributes();
        AttributeInstance instance = defAttributes.getAttribute(attribute);
        return instance != null ? instance.getBaseValue() : -1;
    }

    public static Set<Attribute> getAllAttributesFromFile(NBTCompound compound) {
        Set<Attribute> attributes = new HashSet<>();

        NBTCompoundList attributeCompoundList = compound.getCompoundList("Attributes");
        for(ReadWriteNBT lc : attributeCompoundList) {
            Attribute a = getAttributeFromName(lc.getString("Name"));
            if(a != null) attributes.add(a);
        }
        return attributes;
    }

    public static Attribute getAttributeFromName(String n) {
        for(Attribute attribute : Attribute.values()) {
            if(attribute.getKey().toString().equalsIgnoreCase(n)) return attribute;
        }
        return null;
    }

    public static ReadWriteNBT getNBT(NBTCompound compound, Attribute attribute) {
        NBTCompoundList attributes = compound.getCompoundList("Attributes");
        ReadWriteNBT nbt = null;
        for(ReadWriteNBT lc : attributes) {
            if(!lc.getString("Name").equals
                    (attribute.getKey().getNamespace() + ":" + attribute.getKey().getKey())) {
                continue;
            }
            nbt = lc;
            break;
        }
        return nbt;
    }

    public static void resetAllAttributes(LivingEntity entity) {
        for(Attribute attribute : Attribute.values()) {
            AttributeManager.setAttribute(entity, attribute, AttributeManager.getDefaultValue(entity, attribute));
        }
    }

    public static void healEntity(LivingEntity entity, double amount) {
        double sum = entity.getHealth() + amount;
        entity.setHealth(Math.min(sum, getEntityMaxHealth(entity)));
    }

    public static double getEntityMaxHealth(LivingEntity entity) {
        AttributeInstance instance = entity.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        return instance != null ? instance.getBaseValue() : 0;
    }

}
