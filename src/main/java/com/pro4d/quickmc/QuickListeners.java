package com.pro4d.quickmc;

import com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent;
import com.pro4d.quickmc.events.QuickItemVoidEvent;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.ShulkerBox;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

import static com.pro4d.quickmc.QuickMC.REGISTERED;

public class QuickListeners implements Listener {

    private final Set<Player> shouldUpdateInventory;
    private final Set<UUID> playerSet;

    private final JavaPlugin plugin;
    public QuickListeners(JavaPlugin instance) {
        this.plugin = instance;
        shouldUpdateInventory = new HashSet<>();
        playerSet = new HashSet<>();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private void cancelMove(InventoryClickEvent event) {
        if(!(event.getWhoClicked() instanceof Player player) ||
                player.getGameMode() == GameMode.CREATIVE) return;

        ClickType click = event.getClick();
        Inventory clickedInv = event.getClickedInventory();

        if(clickedInv != null && player.getOpenInventory().getType() != InventoryType.CRAFTING) {
            switch(click) {
                case NUMBER_KEY -> {
                    ItemStack item = player.getInventory().getItem(event.getHotbarButton());
                    if(item != null && clickedInv.getType() != InventoryType.PLAYER && preventRemoval(item)) event.setCancelled(true);
                }

                case LEFT, RIGHT -> {
                    ItemStack cursor = event.getCursor();
                    if(cursor != null && clickedInv.getType() != InventoryType.PLAYER
                            && preventRemoval(cursor)) event.setCancelled(true);
                }

                case SHIFT_LEFT, SHIFT_RIGHT -> {
                    ItemStack clicked = event.getCurrentItem();
                    if(clicked != null && clickedInv.getType() == InventoryType.PLAYER
                            && preventRemoval(clicked)) event.setCancelled(true);
                }

                case SWAP_OFFHAND -> {
                    if(clickedInv.getType() != InventoryType.PLAYER &&
                            preventRemoval(player.getInventory().getItemInOffHand())) event.setCancelled(true);
                }
            }
        }

        shouldUpdateInventory.add(player);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private void cancelInteract(PlayerInteractEvent event) {
        if(playerSet.contains(event.getPlayer().getUniqueId())) event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void cancelRemoveWithItemFrameAndAllay(PlayerInteractEntityEvent event) {
        Entity clicked = event.getRightClicked();
        if(!(clicked instanceof ItemFrame) && clicked.getType() != EntityType.ALLAY) return;
        if(clicked instanceof ItemFrame frame && frame.getItem().getType() != Material.AIR) return;

        EntityEquipment equipment = event.getPlayer().getEquipment();
        if(preventRemoval(equipment.getItemInMainHand()) ||
                preventRemoval(equipment.getItemInOffHand())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void cancelDragOutInventory(InventoryDragEvent event) {
        if(!(event.getInventory() instanceof PlayerInventory)
                && preventDrop(event.getOldCursor())) event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private void cancelDrop(PlayerDropItemEvent event) {
        ItemStack stack = event.getItemDrop().getItemStack();
        Player player = event.getPlayer();
        if(player.getGameMode() == GameMode.CREATIVE || !preventDrop(stack)) return;
        playerSet.add(player.getUniqueId());

        if(noFreeSpotsInInv(player)) player.setItemOnCursor(stack);
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private void cancelDestroy(EntityDamageEvent event) {
        if(!(event.getEntity() instanceof Item item)) return;
        if(preventDestroy(item.getItemStack())) event.setCancelled(true);
    }

    

    @EventHandler(priority = EventPriority.LOWEST)
    private void callVoidEvent(EntityRemoveFromWorldEvent event) {
        Entity entity = event.getEntity();
        if(entity instanceof InventoryHolder holder) {
            getAllNoVoidItems(holder.getInventory()).forEach(i ->
                    Bukkit.getServer().getPluginManager().
                            callEvent(new QuickItemVoidEvent(entity, i)));


        } else if(entity instanceof Item item) {
            ItemStack itemStack = item.getItemStack();
            if(preventVoid(itemStack)) {
                QuickItemVoidEvent voidEvent = new QuickItemVoidEvent(entity, itemStack);
                Bukkit.getServer().getPluginManager().callEvent(voidEvent);
            }

            if(itemStack.getType() != Material.SHULKER_BOX) return;
            BlockStateMeta bsm = (BlockStateMeta) itemStack.getItemMeta();
            if(!(bsm instanceof ShulkerBox shulker)) return;

            getAllNoVoidItems(shulker.getInventory()).forEach(i ->
                    Bukkit.getServer().getPluginManager().
                            callEvent(new QuickItemVoidEvent(entity, i)));
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private void playerFallInVoid(PlayerDeathEvent event) {
        Player player = event.getPlayer();
        getAllNoVoidItems(player.getInventory()).forEach(i ->
                Bukkit.getServer().getPluginManager().
                        callEvent(new QuickItemVoidEvent(player, i)));
    }



    private boolean preventRemoval(ItemStack item) {
        if(QuickMC.getNoRemove().contains(item.getType())) return true;

        ItemMeta meta = item.getItemMeta();
        if(meta == null) return false;
        return meta.getPersistentDataContainer().has(QuickMC.NO_REMOVE, PersistentDataType.INTEGER);
    }

    private boolean preventDrop(ItemStack item) {
        if(QuickMC.getNoDrop().contains(item.getType())) return true;

        ItemMeta meta = item.getItemMeta();
        if(meta == null) return false;
        return meta.getPersistentDataContainer().has(QuickMC.NO_DROP, PersistentDataType.INTEGER);
    }

    private boolean preventDestroy(ItemStack item) {
        if(QuickMC.getCantDestroy().contains(item.getType())) return true;

        ItemMeta meta = item.getItemMeta();
        if(meta == null) return false;
        return meta.getPersistentDataContainer().has(QuickMC.CANT_DESTROY, PersistentDataType.INTEGER);
    }

    private boolean preventVoid(ItemStack item) {
        if(QuickMC.getVoidEvent().contains(item.getType())) return true;

        ItemMeta meta = item.getItemMeta();
        if(meta == null) return false;
        return meta.getPersistentDataContainer().has(QuickMC.VOID_EVENT, PersistentDataType.INTEGER);
    }

    private List<ItemStack> getAllNoVoidItems(Inventory inv) {
        List<ItemStack> items = new ArrayList<>();
        for(ItemStack i : inv.getContents()) {
            if(i != null && preventVoid(i)) items.add(i);
        }
        return items;
    }

    private boolean noFreeSpotsInInv(Player player) {
        return player.getInventory().firstEmpty() == -1;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void updateInventory(InventoryCloseEvent event) {
        if(event.getPlayer() instanceof Player player && shouldUpdateInventory.remove(player)) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    player.updateInventory();
                }
            }.runTaskLater(plugin, 5L);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPluginDisable(PluginDisableEvent e) {
        if(e.getPlugin() == this.plugin) {
            REGISTERED.set(false);
            QuickMC.getGlowingEntities().disable();
        }
    }

}
