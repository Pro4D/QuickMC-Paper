package com.pro4d.quickmc.util;

import com.pro4d.quickmc.QuickMC;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.Collection;

public final class GlowingUtil {

    public static void setGlobalGlowing(Entity target, ChatColor color) throws ReflectiveOperationException {
        for(Player p : Bukkit.getOnlinePlayers()) {
            QuickMC.getGlowingEntities().setGlowing(target, p, color);
        }
    }

    public static void setPerPlayerGlowing(Entity target, ChatColor color, Collection<Player> receivers) throws ReflectiveOperationException {
        for(Player p : receivers) {
            QuickMC.getGlowingEntities().setGlowing(target, p, color);
        }
    }


    public static void clearGlowing(Entity target) throws ReflectiveOperationException {
        for(Player p : Bukkit.getOnlinePlayers()) {
            QuickMC.getGlowingEntities().unsetGlowing(target, p);
        }
    }

    public static void perPlayerGlowingClear(Entity target, Collection<Player> receivers) throws ReflectiveOperationException {
        for(Player p : Bukkit.getOnlinePlayers()) {
            QuickMC.getGlowingEntities().unsetGlowing(target, p);
        }
    }





    public static void setGlobalBlockGlowing(Block target, ChatColor color) throws ReflectiveOperationException {
        for(Player p : Bukkit.getOnlinePlayers()) {
            QuickMC.getGlowingBlocks().setGlowing(target, p, color);
        }
    }

    public static void setPerPlayerBlockGlowing(Block target, ChatColor color, Collection<Player> receivers) throws ReflectiveOperationException {
        for(Player p : receivers) {
            QuickMC.getGlowingBlocks().setGlowing(target, p, color);
        }
    }


    public static void clearBlockGlowing(Block target) throws ReflectiveOperationException {
        for(Player p : Bukkit.getOnlinePlayers()) {
            QuickMC.getGlowingBlocks().unsetGlowing(target, p);
        }
    }

    public static void perPlayerBlockGlowingClear(Block target, Collection<Player> receivers) throws ReflectiveOperationException {
        for(Player p : Bukkit.getOnlinePlayers()) {
            QuickMC.getGlowingBlocks().unsetGlowing(target, p);
        }
    }


}
