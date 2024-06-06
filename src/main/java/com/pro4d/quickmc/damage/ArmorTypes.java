package com.pro4d.quickmc.damage;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.EnumSet;

public enum ArmorTypes {

    NETHERITE(EnumSet.of(Material.NETHERITE_HELMET, Material.NETHERITE_CHESTPLATE, Material.NETHERITE_LEGGINGS, Material.NETHERITE_BOOTS)),

    DIAMOND(EnumSet.of(Material.DIAMOND_HELMET, Material.DIAMOND_CHESTPLATE, Material.DIAMOND_LEGGINGS, Material.DIAMOND_BOOTS)),

    IRON(EnumSet.of(Material.IRON_HELMET, Material.IRON_CHESTPLATE, Material.IRON_LEGGINGS, Material.IRON_BOOTS)),

    CHAINMAIL(EnumSet.of(Material.CHAINMAIL_HELMET, Material.CHAINMAIL_CHESTPLATE, Material.CHAINMAIL_LEGGINGS, Material.CHAINMAIL_BOOTS)),

    GOLD(EnumSet.of(Material.GOLDEN_HELMET, Material.GOLDEN_CHESTPLATE, Material.GOLDEN_LEGGINGS, Material.GOLDEN_BOOTS)),

    LEATHER(EnumSet.of(Material.LEATHER_HELMET, Material.LEATHER_CHESTPLATE, Material.LEATHER_LEGGINGS, Material.LEATHER_BOOTS)),

    TURTLE(EnumSet.of(Material.TURTLE_HELMET)),

    NAKED(EnumSet.noneOf(Material.class));

    private final EnumSet<Material> armorPieces;
    ArmorTypes(EnumSet<Material> armorPieces) {this.armorPieces = armorPieces;}

    public EnumSet<Material> getArmorPieces() {
        return armorPieces;
    }

    public static boolean isArmorPiece(ItemStack item) {
        for(ArmorTypes type : ArmorTypes.values()) {
            if(type.getArmorPieces().contains(item.getType())) return true;
        }
        return false;
    }

    public boolean includes(ItemStack item) {
        return getArmorPieces().contains(item.getType());
    }

    public static ArmorTypes getByName(String s, ArmorTypes fallback) {
        for(ArmorTypes type : ArmorTypes.values()) {
            if(type.name().equalsIgnoreCase(s)) return type;
        }
        return fallback;
    }

    public static ArmorTypes getByItem(ItemStack item) {
        if(item == null) return NAKED;
        for(ArmorTypes type : ArmorTypes.values()) {
            if(item.getType().name().contains(type.name())) {
                return type;
            }
        }
        return NAKED;
    }

}
