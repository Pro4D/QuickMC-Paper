package com.pro4d.quickmc.damage;

import org.bukkit.inventory.ItemStack;

public enum ArmorTypes {

    NETHERITE,
    DIAMOND,
    IRON,
    CHAINMAIL,
    GOLD,
    LEATHER,
    TURTLE,
    NAKED;

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
