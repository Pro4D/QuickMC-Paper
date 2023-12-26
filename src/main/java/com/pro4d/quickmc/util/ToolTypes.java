package com.pro4d.quickmc.util;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.EnumSet;

public enum ToolTypes {

    SWORDS(EnumSet.of(Material.WOODEN_SWORD, Material.STONE_SWORD, Material.IRON_SWORD, Material.GOLDEN_SWORD,
            Material.DIAMOND_SWORD, Material.NETHERITE_SWORD)),

    PICKAXES(EnumSet.of(Material.WOODEN_PICKAXE, Material.STONE_PICKAXE, Material.IRON_PICKAXE, Material.GOLDEN_PICKAXE,
            Material.DIAMOND_PICKAXE, Material.NETHERITE_PICKAXE)),

    SHOVELS(EnumSet.of(Material.WOODEN_SHOVEL, Material.STONE_SHOVEL, Material.IRON_SHOVEL, Material.GOLDEN_SHOVEL,
            Material.DIAMOND_SHOVEL, Material.NETHERITE_SHOVEL)),

    AXES(EnumSet.of(Material.WOODEN_AXE, Material.STONE_AXE, Material.IRON_AXE, Material.GOLDEN_AXE,
            Material.DIAMOND_AXE, Material.NETHERITE_AXE)),

    HOES(EnumSet.of(Material.WOODEN_HOE, Material.STONE_HOE, Material.IRON_HOE, Material.GOLDEN_HOE,
            Material.DIAMOND_HOE, Material.NETHERITE_HOE)),

    SHIELD(EnumSet.of(Material.SHIELD)),

    BOW(EnumSet.of(Material.BOW)),

    CROSSBOW(EnumSet.of(Material.CROSSBOW));

    private final EnumSet<Material> tools;
    ToolTypes(EnumSet<Material> tools) {this.tools = tools;}

    public EnumSet<Material> getTools() {
        return tools;
    }

    public static boolean isToolPiece(ItemStack item) {
        for(ToolTypes type : ToolTypes.values()) {
            if(type.getTools().contains(item.getType())) return true;
        }
        return false;
    }

    public boolean includes(ItemStack item) {
        return getTools().contains(item.getType());
    }

}
