package com.pro4d.quickmc.util;

import com.github.yannicklamprecht.worldborder.api.Position;
import com.github.yannicklamprecht.worldborder.api.WorldBorderApi;
import com.github.yannicklamprecht.worldborder.impl.Border;
import com.github.yannicklamprecht.worldborder.plugin.PersistenceWrapper;
import com.pro4d.quickmc.QuickMC;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.util.concurrent.TimeUnit;

public class PlayerBorderManager {

    private WorldBorderApi worldBorderApi;
    public PlayerBorderManager(JavaPlugin plugin) {
        register(plugin);

        RegisteredServiceProvider<WorldBorderApi> worldBorderApiRegisteredServiceProvider = plugin.getServer().getServicesManager()
                .getRegistration(WorldBorderApi.class);

        if(worldBorderApiRegisteredServiceProvider != null) {
           worldBorderApi = worldBorderApiRegisteredServiceProvider.getProvider();
        }
    }

    private void register(JavaPlugin plugin) {
        WorldBorderApi worldBorderApi = new PersistenceWrapper(plugin, new Border());
        plugin.getServer().getServicesManager().register(WorldBorderApi.class, worldBorderApi, plugin, ServicePriority.High);
    }

    public void resetWorldBorderToGlobal(Player player) {
        worldBorderApi.resetWorldBorderToGlobal(player);
    }

    public void setBorder(Player player, double size) {
        worldBorderApi.setBorder(player, size, player.getWorld().getSpawnLocation());
    }

    public void setBorder(Player player, double size, Location location) {
        worldBorderApi.setBorder(player, size, Position.of(location));
    }

    public void setBorder(Player player, double size, Vector vector) {
        setBorder(player, size, Position.of(vector));
    }

    public void setBorder(Player player, double size, Position position) {
        worldBorderApi.setBorder(player, size, position);
    }

    public void sendRedScreenForSeconds(Player player, long timeSeconds) {
        worldBorderApi.sendRedScreenForSeconds(player, timeSeconds, QuickMC.getSourcePlugin());
    }

    public void setBorder(Player player, double size, long milliSeconds) {
        worldBorderApi.setBorder(player, size, milliSeconds);
    }

    public void setBorder(Player player, double size, long time, TimeUnit timeUnit) {
        worldBorderApi.setBorder(player, size, timeUnit.toMillis(time));
    }

}
