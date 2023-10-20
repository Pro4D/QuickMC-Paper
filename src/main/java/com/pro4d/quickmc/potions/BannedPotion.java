package com.pro4d.quickmc.potions;

import org.bukkit.Material;
import org.bukkit.potion.PotionEffectType;

public record BannedPotion(PotionEffectType existing, PotionEffectType bannedResult,
                           Material addedIngredient) {

}
