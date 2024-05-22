package com.pro4d.quickmc.attributes;

import lombok.Getter;
import org.bukkit.attribute.Attribute;

@Getter
public class AttributeValueData {

    private final Attribute attribute;
    private final AttributeOperation operation;
    private final double value;

    public AttributeValueData(Attribute attribute, AttributeOperation operation, double value) {
        this.attribute = attribute;
        this.operation = operation;
        this.value = value;
    }

    public enum AttributeOperation {
        SET,
        ADJUST,
        NOTHING
    }

}
