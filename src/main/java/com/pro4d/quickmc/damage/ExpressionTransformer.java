package com.pro4d.quickmc.damage;

import lombok.Getter;

import java.util.List;

@Getter
public class ExpressionTransformer {

    private final double priority;
    private final List<ExpressionTransformer> conflicts;
    private final DamageManager.DamageTransformer.ExpressionHandler handler;
    public ExpressionTransformer(double priority, List<ExpressionTransformer> conflicts, DamageManager.DamageTransformer.ExpressionHandler handler) {
        this.priority = priority;
        this.conflicts = conflicts;
        this.handler = handler;
    }

    public ExpressionTransformer(double priority, DamageManager.DamageTransformer.ExpressionHandler handler) {
        this(priority, List.of(), handler);
    }

}
