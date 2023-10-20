package com.pro4d.quickmc;

import com.cryptomorin.xseries.messages.ActionBar;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public final class QuickUtils {

    private static final List<PotionEffectType> GOOD_POTIONS = Arrays.asList(PotionEffectType.SPEED,
            PotionEffectType.FAST_DIGGING, PotionEffectType.INCREASE_DAMAGE,
            PotionEffectType.HEAL, PotionEffectType.REGENERATION,
            PotionEffectType.DAMAGE_RESISTANCE, PotionEffectType.FIRE_RESISTANCE,
            PotionEffectType.WATER_BREATHING, PotionEffectType.INVISIBILITY, PotionEffectType.NIGHT_VISION,
            PotionEffectType.HEALTH_BOOST, PotionEffectType.ABSORPTION, PotionEffectType.SATURATION,
            PotionEffectType.DOLPHINS_GRACE, PotionEffectType.LUCK, PotionEffectType.HERO_OF_THE_VILLAGE);

    private static final List<PotionEffectType> BAD_POTIONS = Arrays.asList(PotionEffectType.SLOW,
            PotionEffectType.SLOW_DIGGING, PotionEffectType.HARM,
            PotionEffectType.JUMP, PotionEffectType.CONFUSION,
            PotionEffectType.BLINDNESS, PotionEffectType.HUNGER,
            PotionEffectType.WEAKNESS, PotionEffectType.POISON, PotionEffectType.WITHER,
            PotionEffectType.GLOWING, PotionEffectType.LEVITATION, PotionEffectType.UNLUCK,
            PotionEffectType.SLOW_FALLING, PotionEffectType.BAD_OMEN, PotionEffectType.DARKNESS);

    public static boolean isPositivePotion(PotionEffectType type) {
        return GOOD_POTIONS.contains(type);
    }
    public static boolean isNegativePotion(PotionEffectType type) {
        return BAD_POTIONS.contains(type);
    }


    private static final Set<EntityType> HOSTILE_MOBS = EnumSet.of(EntityType.EVOKER, EntityType.EVOKER_FANGS,
            EntityType.VINDICATOR, EntityType.PILLAGER, EntityType.RAVAGER, EntityType.VEX, EntityType.ENDERMITE,
            EntityType.GUARDIAN, EntityType.ELDER_GUARDIAN, EntityType.SHULKER, EntityType.HUSK, EntityType.STRAY,
            EntityType.PHANTOM, EntityType.BLAZE, EntityType.CREEPER, EntityType.GHAST, EntityType.MAGMA_CUBE,
            EntityType.SILVERFISH, EntityType.SKELETON, EntityType.SLIME, EntityType.ZOMBIE, EntityType.ZOMBIE_VILLAGER,
            EntityType.DROWNED, EntityType.WITHER_SKELETON, EntityType.WITCH, EntityType.HOGLIN, EntityType.ZOGLIN,
            EntityType.PIGLIN_BRUTE, EntityType.SPIDER, EntityType.CAVE_SPIDER, EntityType.ENDERMAN, EntityType.PIGLIN,
            EntityType.ZOMBIFIED_PIGLIN);

    private static final Set<EntityType> PASSIVE_MOBS = EnumSet.of(EntityType.SHEEP, EntityType.COW, EntityType.FOX,
            EntityType.BAT, EntityType.CHICKEN, EntityType.COD, EntityType.OCELOT, EntityType.PIG, EntityType.SNOWMAN,
            EntityType.RABBIT, EntityType.SALMON, EntityType.MUSHROOM_COW, EntityType.SQUID, EntityType.STRIDER,
            EntityType.TROPICAL_FISH, EntityType.TURTLE, EntityType.VILLAGER, EntityType.WANDERING_TRADER,
            EntityType.PUFFERFISH, EntityType.AXOLOTL, EntityType.GLOW_SQUID, EntityType.DONKEY, EntityType.HORSE,
            EntityType.CAT, EntityType.PARROT, EntityType.MULE, EntityType.SKELETON_HORSE);

    public static boolean isHostileMob(LivingEntity entity) {
        return HOSTILE_MOBS.contains(entity.getType());
    }
    public static boolean isPassiveMob(LivingEntity entity) {
        return PASSIVE_MOBS.contains(entity.getType());
    }


    public static int randomInteger(int min, int max) {
        return (int)Math.floor(Math.random()*(max-min+1) + min);
    }
    public static boolean isInt(String s) {
        try {
            Integer.parseInt(s);
            return true;
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    public static double randomDouble(double min, double max) {
        return Math.random() * (max - min) + min;
    }
    public static boolean isDouble(String s) {
        try {
            Double.parseDouble(s);
            return true;
        } catch (NumberFormatException ex) {
            return false;
        }
    }


    public static String color(String msg) {
        return ChatColor.translateAlternateColorCodes('&', msg);
//        return BaseComponent.toLegacyText(MineDown.parse(msg));
    }
    public static List<String> colorList(List<String> lore) {
        List<String> translatedLore = new ArrayList<>();
        lore.forEach(l -> translatedLore.add(color(l)));
        return translatedLore;
    }

    public static Component component(String msg) {
        return QuickMC.getComponentSerializer().deserialize(msg);
    }
    public static List<Component> componentList(List<String> lore) {
        List<Component> translatedLore = new ArrayList<>();
        lore.forEach(l -> translatedLore.add(component(l)));
        return translatedLore;
    }


    public static void sendTitle(Player player, String title, String subtitle, double in, double out, double stay) {
//        player.showTitle(Title.title());
    }

    public static void sendActionBar(Player player, String message) {
        ActionBar.sendActionBar(player, placeholdersAndColor(player, message));
    }

    public static void sendMessage(CommandSender sender, List<String> messages) {
        messages.forEach(message -> sendMessage(sender, message));
    }
    public static void sendMessage(CommandSender sender, String message) {
        if(message.isEmpty()) return;

        if(sender instanceof Player player && QuickMC.PAPI_LOADED) {
            sender.sendMessage(placeholdersAndColor(player, message));
            return;
        }
        sender.sendMessage(color(message));
    }

    public static String placeholdersAndColor(Player player, String msg) {
        if(QuickMC.PAPI_LOADED) {
            return PlaceholderAPI.setPlaceholders(player, color(msg));
        } else return color(msg);
    }

    public static void log(String msg) {
        Bukkit.getConsoleSender().sendMessage(QuickMC.getSourcePlugin().getName() + " " + color(msg));
    }
    public static void log(List<String> messages) {
        messages.forEach(m -> Bukkit.getConsoleSender().sendMessage(QuickMC.getSourcePlugin().getName() + " " + color(m)));
    }

    public static void kickPlayer(Player player, String msg) {
        player.kickPlayer(placeholdersAndColor(player, msg));
    }
    public static void banPlayer(Player player, String msg) {
        Bukkit.getServer().getBanList(BanList.Type.NAME).addBan(player.getName(), placeholdersAndColor(player, msg), null, null);
        kickPlayer(player, msg);
    }


    public static void broadcast(String msg) {
        Bukkit.getServer().broadcast(component(msg));
    }

}
