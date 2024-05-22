package com.pro4d.quickmc.visuals;

import com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent;
import com.pro4d.quickmc.QuickMC;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;

public class WrappedVisual implements Listener {

    private Entity parent;

    private List<Entity> entities;
    private List<Block> blocks;
    private List<SimpleDisplay> displays;
    private List<BukkitTask> tasks;
    public WrappedVisual() {
        this.entities = new ArrayList<>();
        this.blocks = new ArrayList<>();
        this.displays = new ArrayList<>();
        this.tasks = new ArrayList<>();

        QuickMC.getWrappedVisuals().add(this);
        Bukkit.getPluginManager().registerEvents(this, QuickMC.getSourcePlugin());
    }

    public WrappedVisual entity(Entity entity) {
        this.entities.add(entity);
        return this;
    }

    public WrappedVisual display(SimpleDisplay display) {
        this.displays.add(display);
        return this;
    }

    public WrappedVisual parent(Entity entity) {
        this.parent = entity;
        return this;
    }

    public WrappedVisual block(Block block) {
        this.blocks.add(block);
        return this;
    }

    public WrappedVisual task(BukkitTask task) {
        this.tasks.add(task);
        return this;
    }

    public Entity getParent() {
        return parent;
    }
    public List<Entity> getEntities() {
        return entities;
    }
    public List<Block> getBlocks() {
        return blocks;
    }
    public List<SimpleDisplay> getDisplays() {
        return displays;
    }
    public List<BukkitTask> getTasks() {
        return tasks;
    }

    public void delete() {
        if(this.entities != null) {
            List<Entity> eList = new ArrayList<>(entities);
            entities.clear();
            for(Entity e : eList) {
                if(e != null && !e.isDead()) e.remove();
            }
            this.entities = null;
        }

        if(this.displays != null) {
            List<SimpleDisplay> dList = new ArrayList<>(displays);
            displays.clear();
            for(SimpleDisplay display : dList) {
                if(display != null) display.remove();
            }
            this.displays = null;
        }

        if(this.blocks != null) {
            List<Block> bList = new ArrayList<>(blocks);
            blocks.clear();
            for(Block b : bList) {
                if(b != null) b.setType(Material.AIR);
            }
            this.blocks = null;
        }

        if(this.tasks != null) {
            List<BukkitTask> tList = new ArrayList<>(tasks);
            tasks.clear();
            for(BukkitTask t : tList) {
                if(t != null) t.cancel();
            }
            this.tasks = null;
        }

        this.parent = null;

        QuickMC.getWrappedVisuals().remove(this);
    }

    @EventHandler
    private void destroyOnParentDeath(EntityRemoveFromWorldEvent event) {
        if(this.parent != null && event.getEntity().getUniqueId().equals(this.parent.getUniqueId())) delete();
    }

}
