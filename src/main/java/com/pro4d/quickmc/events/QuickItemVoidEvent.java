package com.pro4d.quickmc.events;

import lombok.Getter;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

@Getter
public class QuickItemVoidEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();

    private final Entity sourceEntity;
    private final ItemStack dropped;
    private final Event event;

    public QuickItemVoidEvent(Entity entity, ItemStack beingDropped, Event event) {
        this.sourceEntity = entity;
        this.dropped = beingDropped;
        this.event = event;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @NotNull
    public HandlerList getHandlers() {
        return HANDLERS;
    }

}
