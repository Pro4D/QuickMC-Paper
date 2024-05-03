package com.pro4d.quickmc.events;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

@Getter
@Setter
public class AttributeApplyEvent extends Event implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();
    private boolean isCancelled;

    private final LivingEntity target;
    private Attribute attribute;
    private double value;

    public AttributeApplyEvent(LivingEntity entity, Attribute attribute, double value) {
        this.target = entity;
        this.attribute = attribute;
        this.value = value;

        this.isCancelled = false;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @NotNull
    public HandlerList getHandlers() {
        return HANDLERS;
    }

}
