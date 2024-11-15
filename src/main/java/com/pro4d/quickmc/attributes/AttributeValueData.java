package com.pro4d.quickmc.attributes;

import lombok.Getter;
import org.bukkit.attribute.Attribute;

import java.util.HashMap;
import java.util.Map;

@Getter
public class AttributeValueData {

    private final Map<Attribute, Double> attributeValues;

    public AttributeValueData() {
        this.attributeValues = new HashMap<>();
    }

    public void addDataValuePair(Attribute attribute, double val) {
        attributeValues.put(attribute, val);
    }

    public void removeDataValuePair(Attribute attribute) {
        attributeValues.remove(attribute);
    }

    public double getValueForAttribute(Attribute attribute) {
        return attributeValues.getOrDefault(attribute, Double.MIN_VALUE);
    }

    public Map<Attribute, Double> getAllAttributeValues() {
        return attributeValues;
    }
}
