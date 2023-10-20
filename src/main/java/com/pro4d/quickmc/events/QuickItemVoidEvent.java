package com.pro4d.quickmc.events;

import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class QuickItemVoidEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();

    private final Entity sourceEntity;
    private final ItemStack dropped;

    public QuickItemVoidEvent(Entity entity, ItemStack beingDropped) {
        this.sourceEntity = entity;
        this.dropped = beingDropped;
    }

    public Entity getSourceEntity() {
        return sourceEntity;
    }

    public ItemStack getDropped() {
        return dropped;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @NotNull
    public HandlerList getHandlers() {
        return HANDLERS;
    }

}
