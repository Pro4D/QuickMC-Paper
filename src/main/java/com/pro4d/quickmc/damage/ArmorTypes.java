package com.pro4d.quickmc.damage;

import org.bukkit.inventory.ItemStack;

public enum ArmorTypes {

    NETHERITE(20),
    DIAMOND(20),
    GOLD(11),
    IRON(15),
    LEATHER(7),
    NAKED(0);

    private final int dp;
    ArmorTypes(int dp) {
        this.dp = dp;
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

    public int getDefensePoints() {
        return dp;
    }

}
