package com.pro4d.quickmc.potions;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BrewingStartEvent;
import org.bukkit.event.inventory.BrewEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.BrewerInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
public class PotionManager implements Listener {

    private final Set<BannedPotion> bannedPotions;
    public PotionManager() {
        bannedPotions = new HashSet<>();
    }

    @EventHandler
    private void preventPlacing(InventoryClickEvent event) {
        if(!(event.getWhoClicked() instanceof Player player)) return;

        Inventory clicked = event.getClickedInventory();
        if(!(clicked instanceof BrewerInventory brewerInv)) return;

        ItemStack placing = event.getCursor();
        if(placing == null) {
            Bukkit.broadcastMessage("cursor = null");
            return;
        }

        Bukkit.broadcastMessage("P: " + placing.getType());
        if(!isBannedIngredient(placing.getType())) {
            Bukkit.broadcastMessage("Not banned ingredient");
            return;
        } else Bukkit.broadcastMessage("banned ingredient");


        List<BannedPotion> bannedIngredientPotions =
                getAllBannedPotionsFromIngredient(placing.getType());

        boolean hasBannedBasePot = false;
        for(BannedPotion banned : bannedIngredientPotions) {
            for(ItemStack i : brewerInv.getContents()) {
                Bukkit.broadcastMessage("I: " + i.getType());
                if(isBannedBasePot(i)) hasBannedBasePot = true;
            }
        }
        if(!hasBannedBasePot) return;
        Bukkit.broadcastMessage("Banned base pot found!");

    }

    @EventHandler
    private void preventBrew(BrewingStartEvent event) {}

    @EventHandler
    private void cancelBrew(BrewEvent event) {

    }

    private boolean isBannedIngredient(Material mat) {
        for(BannedPotion bannedPot : bannedPotions) {
            if(bannedPot.addedIngredient() == mat) return true;
        }
        return false;
    }

    private boolean isBannedBasePot(ItemStack item) {
        if(!(item.getItemMeta() instanceof PotionMeta meta)) return false;
        PotionEffectType type = meta.getBasePotionData().getType().getEffectType();

        for(BannedPotion bannedPot : bannedPotions) {
            if(bannedPot.existing() == type) return true;
        }
        return false;
    }

    private boolean isBannedPotion(ItemStack item) {
        if(!(item.getItemMeta() instanceof PotionMeta meta)) return false;
        PotionEffectType type = meta.getBasePotionData().getType().getEffectType();

        for(BannedPotion bannedPot : bannedPotions) {
            if(bannedPot.existing() == type) return true;
        }
        return false;
    }

    private List<BannedPotion> getAllBannedPotionsFromIngredient(Material mat) {
        List<BannedPotion> possibleOutcomes = new ArrayList<>();
        for(BannedPotion pots : bannedPotions) {
            if(pots.addedIngredient() == mat) possibleOutcomes.add(pots);
        }
        return possibleOutcomes;
    }

}
