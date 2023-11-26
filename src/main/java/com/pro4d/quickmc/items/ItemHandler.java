package com.pro4d.quickmc.items;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.List;

public class ItemHandler {

    public static void dropOrGiveItems(Player target, ItemStack... items) {
        HashMap<Integer, ItemStack> remaining = target.getInventory().addItem(items);
        if(remaining.isEmpty()) return;
        World world = target.getWorld();
        Location loc = target.getLocation();
        remaining.values().forEach(i -> world.dropItemNaturally(loc, i));
    }

    public static <T, Z> void addPersistentData(ItemStack item, PersistentDataType<T, Z> type, Z value, NamespacedKey key) {
        ItemMeta meta = item.getItemMeta();
        assert meta != null;

        meta.getPersistentDataContainer().set(key, type, value);

        item.setItemMeta(meta);
    }

    public static ItemStack createItem(Material material, int amount, String displayName, int cmd, List<String> lore, boolean glowing) {
        ItemBuilder builder = new ItemBuilder(material)
                .amount(amount)
                .name(displayName)
                .customModelData(cmd)
                .lore(lore);

        if(glowing) builder.enchant(Enchantment.DURABILITY, 1);

        return builder.build();
    }

//    // TODO implement
//    public static ItemStack createItemFromFile(String path) {}

}
