package com.pro4d.quickmc;

import com.cryptomorin.xseries.messages.ActionBar;
import de.themoep.minedown.MineDown;
import me.clip.placeholderapi.PlaceholderAPI;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.*;

public final class QuickUtils {

    public static final List<PotionEffectType> GOOD_POTIONS = Arrays.asList(PotionEffectType.SPEED,
            PotionEffectType.FAST_DIGGING, PotionEffectType.INCREASE_DAMAGE,
            PotionEffectType.HEAL, PotionEffectType.REGENERATION,
            PotionEffectType.DAMAGE_RESISTANCE, PotionEffectType.FIRE_RESISTANCE,
            PotionEffectType.WATER_BREATHING, PotionEffectType.INVISIBILITY, PotionEffectType.NIGHT_VISION,
            PotionEffectType.HEALTH_BOOST, PotionEffectType.ABSORPTION, PotionEffectType.SATURATION,
            PotionEffectType.DOLPHINS_GRACE, PotionEffectType.LUCK, PotionEffectType.HERO_OF_THE_VILLAGE);

    public static final List<PotionEffectType> BAD_POTIONS = Arrays.asList(PotionEffectType.SLOW,
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


    public static final Set<EntityType> HOSTILE_MOBS = EnumSet.of(EntityType.EVOKER, EntityType.EVOKER_FANGS,
            EntityType.VINDICATOR, EntityType.PILLAGER, EntityType.RAVAGER, EntityType.VEX, EntityType.ENDERMITE,
            EntityType.GUARDIAN, EntityType.ELDER_GUARDIAN, EntityType.SHULKER, EntityType.HUSK, EntityType.STRAY,
            EntityType.PHANTOM, EntityType.BLAZE, EntityType.CREEPER, EntityType.GHAST, EntityType.MAGMA_CUBE,
            EntityType.SILVERFISH, EntityType.SKELETON, EntityType.SLIME, EntityType.ZOMBIE, EntityType.ZOMBIE_VILLAGER,
            EntityType.DROWNED, EntityType.WITHER_SKELETON, EntityType.WITCH, EntityType.HOGLIN, EntityType.ZOGLIN,
            EntityType.PIGLIN_BRUTE, EntityType.SPIDER, EntityType.CAVE_SPIDER, EntityType.ENDERMAN, EntityType.PIGLIN,
            EntityType.ZOMBIFIED_PIGLIN);

    public static final Set<EntityType> PASSIVE_MOBS = EnumSet.of(EntityType.SHEEP, EntityType.COW, EntityType.FOX,
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

    /**
     * A method to serialize an inventory to Base64 string.
     *
     * <p />
     *
     * Special thanks to Comphenix in the Bukkit forums or also known
     * as aadnk on GitHub.
     *
     * <a href="https://gist.github.com/aadnk/8138186">Original Source</a>
     *
     * @param inventory to serialize
     * @return Base64 string of the provided inventory
     */
    public static String toBase64(Inventory inventory) throws IllegalStateException {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);

            // Write the size of the inventory
            dataOutput.writeInt(inventory.getSize());

            // Save every element in the list
            for (int i = 0; i < inventory.getSize(); i++) {
                dataOutput.writeObject(inventory.getItem(i));
            }

            dataOutput.close();
            return Base64Coder.encodeLines(outputStream.toByteArray());
        } catch (Exception e) {
            throw new IllegalStateException("Unable to save item stacks.", e);
        }
    }

    /**
     *
     * A method to get an {@link Inventory} from an encoded, Base64, string.
     *
     * <p />
     *
     * Special thanks to Comphenix in the Bukkit forums or also known
     * as aadnk on GitHub.
     *
     * <a href="https://gist.github.com/aadnk/8138186">Original Source</a>
     *
     * @param data Base64 string of data containing an inventory.
     * @return Inventory created from the Base64 string.
     */
    public static Inventory fromBase64(String data) throws IOException {
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(data));
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
            Inventory inventory = Bukkit.getServer().createInventory(null, dataInput.readInt());

            // Read the serialized inventory
            for (int i = 0; i < inventory.getSize(); i++) {
                inventory.setItem(i, (ItemStack) dataInput.readObject());
            }

            dataInput.close();
            return inventory;
        } catch (ClassNotFoundException e) {
            throw new IOException("Unable to decode class type.", e);
        }
    }


    /**
     *
     * A method to serialize an {@link ItemStack} array to Base64 String.
     *
     * <p />
     *
     * Based off of {@link #toBase64(Inventory)}.
     *
     * @param items to turn into a Base64 String.
     * @return Base64 string of the items.
     */
    public static String itemStackArrayToBase64(ItemStack[] items) throws IllegalStateException {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);

            // Write the size of the inventory
            dataOutput.writeInt(items.length);

            // Save every element in the list
            for(ItemStack item : items) {
                dataOutput.writeObject(item);
            }

            // Serialize that array
            dataOutput.close();
            return Base64Coder.encodeLines(outputStream.toByteArray());
        } catch (Exception e) {
            throw new IllegalStateException("Unable to save item stacks.", e);
        }
    }


    /**
     * Gets an array of ItemStacks from Base64 string.
     *
     * <p />
     *
     * Base off of {@link #fromBase64(String)}.
     *
     * @param data Base64 string to convert to ItemStack array.
     * @return ItemStack array created from the Base64 string.
     */
    public static ItemStack[] itemStackArrayFromBase64(String data) throws IOException {
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(data));
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
            ItemStack[] items = new ItemStack[dataInput.readInt()];

            // Read the serialized inventory
            for (int i = 0; i < items.length; i++) {
                items[i] = (ItemStack) dataInput.readObject();
            }

            dataInput.close();
            return items;
        } catch (ClassNotFoundException e) {
            throw new IOException("Unable to decode class type.", e);
        }
    }

    public static Color hexToColor(String hexString) {
        String hex = hexString.replace("#", "")
                .replace("&", "");

        int rgb = Integer.parseInt(hex, 16);

        int red = (rgb >> 16) & 0xFF;
        int green = (rgb >> 8) & 0xFF;
        int blue = rgb & 0xFF;

        return Color.fromRGB(red, green, blue);
    }

    public static String colorToHex(Color color) {
        int red = color.getRed();
        int green = color.getGreen();
        int blue = color.getBlue();

        int rgb = (red << 16) + (green << 8) + blue;

        return String.format("&#%06x&", rgb);
    }



    public static String color(String msg) {
        return BaseComponent.toLegacyText(component(msg));
    }
    public static List<String> colorList(List<String> lore) {
        List<String> translatedLore = new ArrayList<>();
        lore.forEach(l -> translatedLore.add(color(l)));
        return translatedLore;
    }

    public static BaseComponent[] component(String msg) {
        return MineDown.parse(msg);
    }
    public static List<BaseComponent[]> componentList(List<String> lore) {
        List<BaseComponent[]> translatedLore = new ArrayList<>();
        lore.forEach(l -> translatedLore.add(component(l)));
        return translatedLore;
    }

    public static Location stringToLocation(String s) {
        String[] parts = s.split(",");
        return new Location(Bukkit.getWorld(parts[0]),
                format(stringToDouble(parts[1])),
                format(stringToDouble(parts[2])),
                format(stringToDouble(parts[3]))
        );
    }

    public static String locationToString(Location loc) {
        return loc.getWorld().getName() + ", "
                + loc.getX() + ", "
                + loc.getY() + ", "
                + loc.getZ();

    }

    public static double stringToDouble(String s) {
        return Double.parseDouble(s);
    }

    public static double format(double d) {
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(2);

        return Double.parseDouble(nf.format(d));
    }

    // TODO implement
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
    public static List<String> placeholdersAndColor(Player player, List<String> messages) {
        if(QuickMC.PAPI_LOADED) {
            return PlaceholderAPI.setPlaceholders(player, colorList(messages));
        } else return colorList(messages);
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

    public static BukkitTask sync(Runnable runnable) {
        return Bukkit.getScheduler().runTask(QuickMC.getSourcePlugin(), runnable);
    }

    public static BukkitTask syncLater(long delay, Runnable runnable) {
        return Bukkit.getScheduler().runTaskLater(QuickMC.getSourcePlugin(), runnable, delay);
    }

    public static BukkitTask syncTimer(long delay, long runEvery, Runnable runnable) {
        return Bukkit.getScheduler().runTaskTimer(QuickMC.getSourcePlugin(), runnable, delay, runEvery);
    }

    public static BukkitTask async(Runnable runnable) {
        return Bukkit.getScheduler().runTaskAsynchronously(QuickMC.getSourcePlugin(), runnable);
    }

    public static BukkitTask asyncLater(long delay, Runnable runnable) {
        return Bukkit.getScheduler().runTaskLaterAsynchronously(QuickMC.getSourcePlugin(), runnable, delay);
    }

    public static BukkitTask asyncTimer(long delay, long runEvery, Runnable runnable) {
        return Bukkit.getScheduler().runTaskTimerAsynchronously(QuickMC.getSourcePlugin(), runnable, delay, runEvery);
    }

}
