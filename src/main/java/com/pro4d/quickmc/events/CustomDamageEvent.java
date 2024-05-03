package com.pro4d.quickmc.events;

import com.pro4d.quickmc.damage.ExpressionTransformer;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@SuppressWarnings("deprecation")
@Getter
@Setter
public class CustomDamageEvent extends EntityDamageByEntityEvent implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();
    private boolean isCancelled;

    private LivingEntity target;
    private Entity source;
    private double damage, finalDmg;
    private final String causeName;
    private final List<ExpressionTransformer> transformersList;

    public CustomDamageEvent(LivingEntity target, Entity source, double damage, String causeName, List<ExpressionTransformer> transformers) {
        super(target, source, DamageCause.CUSTOM, damage);
        this.target = target;
        this.source = source;
        this.damage = damage;
        this.finalDmg = damage;
        this.causeName = causeName;
        this.transformersList = transformers;

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
