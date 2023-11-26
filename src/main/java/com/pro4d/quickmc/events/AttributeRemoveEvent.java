package com.pro4d.quickmc.events;

import com.pro4d.quickmc.attributes.AppliedAttribute;
import lombok.Getter;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

@Getter
public class AttributeRemoveEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();

    private final LivingEntity target;
    private final AppliedAttribute appliedAttribute;

    public AttributeRemoveEvent(LivingEntity entity, AppliedAttribute applied) {
        this.target = entity;
        this.appliedAttribute = applied;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @NotNull
    public HandlerList getHandlers() {
        return HANDLERS;
    }

}
