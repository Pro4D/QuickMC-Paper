package com.pro4d.quickmc;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.PacketEventsAPI;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.pro4d.quickmc.attributes.AttributeManager;
import com.pro4d.quickmc.damage.ArmorTypes;
import com.pro4d.quickmc.damage.DamageManager;
import com.pro4d.quickmc.util.PlayerBorderManager;
import com.pro4d.quickmc.visuals.WrappedVisual;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIBukkitConfig;
import fr.skytasul.glowingentities.GlowingBlocks;
import fr.skytasul.glowingentities.GlowingEntities;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import lombok.Getter;
import me.tofaa.entitylib.APIConfig;
import me.tofaa.entitylib.EntityLib;
import me.tofaa.entitylib.spigot.SpigotEntityLibPlatform;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public final class QuickMC {

    public static NamespacedKey CANT_DESTROY, VOID_EVENT, NO_DROP, NO_REMOVE;
    @Getter private static Set<Material> cantDestroy, voidEvent, noDrop, noRemove;

    @Getter private static List<WrappedVisual> wrappedVisuals;
    @Getter private static GlowingEntities glowingEntities;
    @Getter private static GlowingBlocks glowingBlocks;
    @Getter private static PlayerBorderManager playerBorderManager;

    @Getter private static JavaPlugin sourcePlugin;
    @Getter private static PacketEventsAPI<?> packetEventsAPI;

    public static boolean PAPI_LOADED;

    //public static final AtomicBoolean REGISTERED = new AtomicBoolean(false);

    public static void injectOnLoad(JavaPlugin plugin) {
        // load the command api
        CommandAPI.onLoad(new CommandAPIBukkitConfig(plugin).verboseOutput(false).silentLogs(true));

        // load PacketEvents
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(plugin));

        packetEventsAPI = PacketEvents.getAPI();
        packetEventsAPI.load();
        packetEventsAPI.getEventManager().registerListener(new AttributeManager(), PacketListenerPriority.LOW);
    }

    public static void init(JavaPlugin plugin) {
        Objects.requireNonNull(plugin, "plugin");
        sourcePlugin = plugin;

        //if(REGISTERED.getAndSet(true)) throw new IllegalStateException("QuickLib is already registered");

        wrappedVisuals = new ArrayList<>();
        glowingEntities = new GlowingEntities(plugin);
        glowingBlocks = new GlowingBlocks(plugin);
        playerBorderManager = new PlayerBorderManager(plugin);

        CommandAPI.onEnable();
        PacketEvents.getAPI().init();

        EntityLib.init(new SpigotEntityLibPlatform(plugin), new APIConfig(PacketEvents.getAPI()));

        PAPI_LOADED = Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null;

        createKeys(plugin);
        Bukkit.getPluginManager().registerEvents(new QuickListeners(plugin), plugin);
        Bukkit.getPluginManager().registerEvents(new DamageManager(), plugin);
    }

    public static void executeOnDisable() {
        if(packetEventsAPI != null) packetEventsAPI.terminate();
    }

    public static void setMetaArmor(ArmorTypes[] types) {
        DamageManager.HELMET = types.length > 0 ? types[0] : ArmorTypes.NAKED;
        if(DamageManager.HELMET == null) DamageManager.HELMET = ArmorTypes.NAKED;

        DamageManager.CHESTPLATE = types.length > 1 ? types[1] : ArmorTypes.NAKED;
        if(DamageManager.CHESTPLATE == null) DamageManager.CHESTPLATE = ArmorTypes.NAKED;

        DamageManager.LEGGINGS = types.length > 2 ? types[2] : ArmorTypes.NAKED;
        if(DamageManager.LEGGINGS == null) DamageManager.LEGGINGS = ArmorTypes.NAKED;

        DamageManager.BOOTS = types.length > 3 ? types[3] : ArmorTypes.NAKED;
        if(DamageManager.BOOTS == null) DamageManager.BOOTS = ArmorTypes.NAKED;
    }
    public static ArmorTypes[] getMetaArmor() {
        ArmorTypes[] type = new ArmorTypes[4];
        type[0] = DamageManager.HELMET;
        type[1] = DamageManager.CHESTPLATE;
        type[2] = DamageManager.LEGGINGS;
        type[3] = DamageManager.BOOTS;

        return type;
    }

    public static void setMetaProtection(int[] levels) {
        DamageManager.HELMET_PROT = levels.length > 0 ? levels[0] : 4;
        DamageManager.CHESTPLATE_PROT = levels.length > 1 ? levels[1] : 4;
        DamageManager.LEGGINGS_PROT = levels.length > 2 ? levels[2] : 4;
        DamageManager.BOOTS_PROT = levels.length > 3 ? levels[3] : 4;
    }
    public static int[] getMetaProtection() {
        int[] levels = new int[4];
        levels[0] = DamageManager.HELMET_PROT;
        levels[1] = DamageManager.CHESTPLATE_PROT;
        levels[2] = DamageManager.LEGGINGS_PROT;
        levels[3] = DamageManager.BOOTS_PROT;

        return levels;
    }

    private static void createKeys(JavaPlugin plugin) {
        CANT_DESTROY = new NamespacedKey(plugin, "quick-cant-destroy");
        cantDestroy = new HashSet<>();

        VOID_EVENT = new NamespacedKey(plugin, "quick-void-event");
        voidEvent = new HashSet<>();

        NO_DROP = new NamespacedKey(plugin, "quick-no-drop");
        noDrop = new HashSet<>();

        NO_REMOVE = new NamespacedKey(plugin, "quick-no-remove");
        noRemove = new HashSet<>();
    }

}
