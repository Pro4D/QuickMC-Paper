package com.pro4d.quickmc.attributes;

import com.cryptomorin.xseries.reflection.XReflection;
import com.cryptomorin.xseries.reflection.minecraft.MinecraftClassHandle;
import com.cryptomorin.xseries.reflection.minecraft.MinecraftConnection;
import com.cryptomorin.xseries.reflection.minecraft.MinecraftPackage;
import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.attribute.Attributes;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerUpdateAttributes;
import com.pro4d.quickmc.QuickMC;
import com.pro4d.quickmc.QuickUtils;
import de.tr7zw.changeme.nbtapi.NBTCompound;
import de.tr7zw.changeme.nbtapi.NBTCompoundList;
import de.tr7zw.changeme.nbtapi.iface.ReadWriteNBT;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attributable;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

@Getter
public class AttributeManager implements PacketListener {

    @Getter private static AttributeManager instance;

    private static String INSTANCE_M_RT, INSTANCE_P_TYPE, CONVERTER_M_N, INSTANCE_V_N, CALCULATE_M_N;

    private static Field ATTRIBUTE_MAP, HANDLE, INSTANCE_VALUE;
    private static Method INSTANCE_METHOD, CONVERTOR_METHOD;
    public static Method CALCULATE_METHOD;

    private final Map<UUID, AttributeValueData> valueOverrides;
    private final Map<UUID, AttributeValueData> valueModifiers;

    public AttributeManager() {
        instance = this;

        this.valueOverrides = new HashMap<>();
        this.valueModifiers = new HashMap<>();

        loadFieldStrings();
        try {
            cacheReflection();
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    /* Reflection Methods */
    private void loadFieldStrings() {
        // TODO for future. remove string search entirely

        int minor = XReflection.MINOR_NUMBER;
        int patch = XReflection.PATCH_NUMBER;

        if(minor == 21) {
            //Bukkit.getLogger().info("Loaded NMS support for 1.21.0.");

            INSTANCE_M_RT = "net.minecraft.world.entity.ai.attributes.AttributeInstance";
            INSTANCE_P_TYPE = "net.minecraft.core.Holder";
            CONVERTER_M_N = "bukkitToMinecraftHolder";
            INSTANCE_V_N = "cachedValue";
            CALCULATE_M_N = "calculateValue";

        } else if(minor == 20 && patch == 4) {
            //Bukkit.getLogger().info("Loaded NMS support for 1.20.4.");

            INSTANCE_M_RT = "net.minecraft.world.entity.ai.attributes.AttributeModifiable";
            INSTANCE_P_TYPE = "net.minecraft.world.entity.ai.attributes.AttributeBase";
            CONVERTER_M_N = "bukkitToMinecraft";
            INSTANCE_V_N = "g";
            CALCULATE_M_N = "h";

        } else {
            Bukkit.getLogger().info("QuickMC Attribute Manager could not find supported game version!");
            INSTANCE_M_RT = "";
            INSTANCE_P_TYPE = "";
            CONVERTER_M_N = "";
            INSTANCE_V_N = "";
            CALCULATE_M_N = "";
        }
    }
    private void cacheReflection() throws ReflectiveOperationException {
        MinecraftClassHandle attributeMap = XReflection.ofMinecraft()
                .inPackage(MinecraftPackage.CB, "attribute")
                .named("CraftAttributeMap");

        ATTRIBUTE_MAP = XReflection.ofMinecraft()
                .inPackage(MinecraftPackage.NMS, "world.entity")
                .named("EntityLiving").field().returns(attributeMap)
                .named("craftAttributes").makeAccessible().setter().reflectJvm();


        MinecraftClassHandle attributeMapBase = XReflection.namespaced()
                .ofMinecraft("package net.minecraft.world.entity.ai.attributes; class AttributeMapBase {}");

        HANDLE = attributeMap.field().returns(attributeMapBase).named("handle").makeAccessible().setter().reflectJvm();

        MinecraftClassHandle attributeMod = XReflection.namespaced()
                .ofMinecraft("package net.minecraft.world.entity.ai.attributes; class AttributeModifiable {}");

        // get java class. check if it auto converts ?
        // TODO IT DOES.

        try {
            Class<?> c = Class.forName(INSTANCE_M_RT);
            for(Method dm : c.getDeclaredMethods()) {
                if(!dm.getName().equalsIgnoreCase(CALCULATE_M_N)) continue;
                CALCULATE_METHOD = dm;
                CALCULATE_METHOD.setAccessible(true);
                break;
            }
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        INSTANCE_METHOD = null;
        try {
            Class<?> rr = attributeMapBase.reflect();
            for(Method m : rr.getMethods()) {
                if(m.getParameters().length != 1) continue;
                if(!m.getReturnType().getName().equalsIgnoreCase(INSTANCE_M_RT)) continue;
                if(!m.getParameters()[0].getType().getName().equalsIgnoreCase(INSTANCE_P_TYPE)) continue;

                INSTANCE_METHOD = m;
                break;
            }

        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        CONVERTOR_METHOD = null;
        try {
            Class<?> rr = XReflection.ofMinecraft()
                    .inPackage(MinecraftPackage.CB, "attribute")
                    .named("CraftAttribute").reflect();

            for(Method m : rr.getMethods()) {
                if(!m.getName().equalsIgnoreCase(CONVERTER_M_N)) continue;
                CONVERTOR_METHOD = m;
                break;
            }

        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        INSTANCE_VALUE = null;
        try {
            for(Field f1 : attributeMod.reflect().getDeclaredFields()) {
                if(!f1.getName().equalsIgnoreCase(INSTANCE_V_N)) continue;
                INSTANCE_VALUE = f1;
                INSTANCE_VALUE.setAccessible(true);
                break;
            }

        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if(!event.getPacketType().equals(PacketType.Play.Server.UPDATE_ATTRIBUTES)) return;

        WrapperPlayServerUpdateAttributes updateAttributes = new WrapperPlayServerUpdateAttributes(event);
        Player player = event.getPlayer();

        if(!(SpigotConversionUtil.getEntityById(player.getWorld(), updateAttributes.getEntityId()) instanceof Player target)) return;

        Map<com.github.retrooper.packetevents.protocol.attribute.Attribute, WrapperPlayServerUpdateAttributes.Property> props = new HashMap<>();

        AttributeValueData overrideData = getValueOverrideData(player);
        AttributeValueData modifyData = getValueModifyData(player);

        boolean pass = overrideData == null && modifyData == null;

        for(Attribute bukkit : Attribute.values()) {
            AttributeInstance inst = target.getAttribute(bukkit);
            if(inst == null) continue;

            com.github.retrooper.packetevents.protocol.attribute.Attribute attribute = Attributes.getByName(bukkit.getKey().asString());
            if(attribute == null) continue;
            double v = getRealValue(target, bukkit);
            if(v == -1) v = inst.getValue();

            if(!pass) {
                if (modifyData != null) {
                    double valueModifier = modifyData.getValueForAttribute(bukkit);
                    if (valueModifier != Double.MIN_VALUE) v += valueModifier;
                }

                if (overrideData != null) {
                    double override = overrideData.getValueForAttribute(bukkit);
                    if (override != Double.MIN_VALUE) v = override;
                }
            }

//            Collection<AttributeModifier> modifiers = inst.getModifiers();
//            List<WrapperPlayServerUpdateAttributes.PropertyModifier> propertyModifiers = new ArrayList<>();
//            for(AttributeModifier mod : modifiers) {
//                try {
//                    propertyModifiers.add(new WrapperPlayServerUpdateAttributes.PropertyModifier(mod.getUniqueId(), mod.getAmount(), convertOperation(mod.getOperation())));
//
//                } catch (IllegalArgumentException ignore) {} // TODO fix
//            }

            // TODO check current inst vs set value ?
            props.put(attribute, new WrapperPlayServerUpdateAttributes.Property(attribute, v, List.of())); // TODO certain modifiers need to not be provided ?
        }

        List<WrapperPlayServerUpdateAttributes.Property> presetProperties = updateAttributes.getProperties();
        presetProperties.removeIf(p -> props.containsKey(p.getAttribute()));
        presetProperties.addAll(props.values());

        updateAttributes.setProperties(presetProperties);
        event.markForReEncode(true);
    }

    /* Bukkit Attribute Manipulation */

    public static void setBukkitAttribute(LivingEntity target, Attribute attribute, double value) {
        AttributeInstance instance = target.getAttribute(attribute);
        if(instance != null) instance.setBaseValue(value);
    }
    public static void setTimedBukkitAttribute(LivingEntity target, Attribute attribute, double value, double restore, long duration) {
        setBukkitAttribute(target, attribute, value);

        QuickUtils.syncLater(duration, () -> setBukkitAttribute(target, attribute, restore));
    }

//    public static void setAttribute(LivingEntity target, Attribute attribute, double value) {
//        AttributeApplyEvent event = new AttributeApplyEvent(target, attribute, value);
//        QuickMC.getSourcePlugin().getServer().getPluginManager().callEvent(event);
//        if(event.isCancelled()) return;
//
//        attribute = event.getAttribute();
//        value = event.getValue();
//
//        if(target instanceof Player player) setInternalValue(player, attribute, value);
//    }
//    public static void setTimedAttribute(LivingEntity target, Attribute attribute, double value, double restore, long duration) {
//        setAttribute(target, attribute, value);
//
//        QuickUtils.syncLater(duration, () -> setAttribute(target, attribute, restore));
//    }

    public static double getBukkitAttributeValue(LivingEntity entity, Attribute attribute) {
        AttributeInstance instance = entity.getAttribute(attribute);
        return instance != null ? instance.getValue() : -1;
    }
    public static double getDefaultValue(LivingEntity entity, Attribute attribute) {
        Attributable defAttributes = entity.getType().getDefaultAttributes();
        AttributeInstance instance = defAttributes.getAttribute(attribute);
        return instance != null ? instance.getBaseValue() : -1;
    }
    public static Set<Attribute> getAllAttributesFromFile(NBTCompound compound) {
        Set<Attribute> attributes = new HashSet<>();

        NBTCompoundList attributeCompoundList = compound.getCompoundList("Attributes");
        for(ReadWriteNBT lc : attributeCompoundList) {
            Attribute a = getAttributeFromName(lc.getString("Name"));
            if(a != null) attributes.add(a);
        }
        return attributes;
    }

    public static Attribute getAttributeFromName(String n) {
        for(Attribute attribute : Attribute.values()) {
            if(attribute.getKey().toString().equalsIgnoreCase(n)) return attribute;
        }
        return null;
    }
    public static ReadWriteNBT getNBT(NBTCompound compound, Attribute attribute) {
        NBTCompoundList attributes = compound.getCompoundList("Attributes");
        ReadWriteNBT nbt = null;
        for(ReadWriteNBT lc : attributes) {
            if(!lc.getString("Name").equals
                    (attribute.getKey().getNamespace() + ":" + attribute.getKey().getKey())) {
                continue;
            }
            nbt = lc;
            break;
        }
        return nbt;
    }


    /* Packet Attribute Manipulation */

    private static @NotNull AttributeValueData createValueOverrideData(LivingEntity entity) {
        UUID uuid = entity.getUniqueId();
        if(getInstance().getValueOverrides().containsKey(uuid)) return getInstance().getValueOverrides().get(uuid);

        AttributeValueData valueData = new AttributeValueData();
        getInstance().getValueOverrides().put(uuid, valueData);
        return valueData;
    }

    public static void addValueOverrideData(LivingEntity entity, Attribute attribute, double val) {
        AttributeValueData valueData = getValueOverrideData(entity);
        if(valueData == null) valueData = createValueOverrideData(entity);
        valueData.addDataValuePair(attribute, val);

        if(entity instanceof Player p) setInternalValue(p, attribute, val);
    }
    public static void removeValueOverrideData(LivingEntity entity, Attribute attribute) {
        AttributeValueData valueData = getValueOverrideData(entity);
        if(valueData == null) return;
        valueData.removeDataValuePair(attribute);
        if(entity instanceof Player p) resetToRealValue(p, attribute);
    }
    public static AttributeValueData getValueOverrideData(LivingEntity entity) {
        return getInstance().getValueOverrides().getOrDefault(entity.getUniqueId(), null);
    }

    private static @NotNull AttributeValueData createValueModifyData(LivingEntity entity) {
        UUID uuid = entity.getUniqueId();
        if(getInstance().getValueModifiers().containsKey(uuid)) return getInstance().getValueModifiers().get(uuid);

        AttributeValueData valueData = new AttributeValueData();
        getInstance().getValueModifiers().put(uuid, valueData);
        return valueData;
    }

    public static void addValueModifyData(LivingEntity entity, Attribute attribute, double val) {
        AttributeValueData valueData = getValueModifyData(entity);
        if(valueData == null) valueData = createValueModifyData(entity);
        valueData.addDataValuePair(attribute, val);

        if(entity instanceof Player p) {
            double real = getRealValue(p, attribute);
            if(real == -1) real = getBukkitAttributeValue(entity, attribute);
            setInternalValue(p, attribute, real + val);
        }
    }
    public static void removeValueModifyData(LivingEntity entity, Attribute attribute) {
        AttributeValueData valueData = getValueModifyData(entity);
        if(valueData == null) return;
        valueData.removeDataValuePair(attribute);
        if(entity instanceof Player p) resetToRealValue(p, attribute);
    }
    public static AttributeValueData getValueModifyData(LivingEntity entity) {
        return getInstance().getValueModifiers().getOrDefault(entity.getUniqueId(), null);
    }


    // TODO add support for other entities ?
    public static Object getAttributeInstance(Player player, Attribute attribute) {
        Object o = MinecraftConnection.getHandle(player);

        try {
            return INSTANCE_METHOD.invoke(HANDLE.get(ATTRIBUTE_MAP.get(o)), CONVERTOR_METHOD.invoke(null, attribute));
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
        return null;
    }
    public static void setInternalValue(Player player, Attribute attribute, double val) {
        if(player.isDead() || !player.isOnline() || INSTANCE_VALUE == null) return;

        Object attributeInst = getAttributeInstance(player, attribute);
        if(attributeInst == null) return;

        try {
            INSTANCE_VALUE.setDouble(attributeInst, Math.max(val, 0.0));
            WrapperPlayServerUpdateAttributes updateAttributes = new WrapperPlayServerUpdateAttributes(player.getEntityId(), List.of());
            QuickMC.getPacketEventsAPI().getPlayerManager().sendPacket(player, updateAttributes);

        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
    public static void resetToRealValue(Player player, Attribute attribute) {
        if(player.isDead() || !player.isOnline() || INSTANCE_VALUE == null) return; // TODO log error

        Object attributeInst = getAttributeInstance(player, attribute);
        if(attributeInst == null) return;

        try {
            double realValue = (double) CALCULATE_METHOD.invoke(attributeInst);
            INSTANCE_VALUE.setDouble(attributeInst, realValue);
            WrapperPlayServerUpdateAttributes updateAttributes = new WrapperPlayServerUpdateAttributes(player.getEntityId(), List.of());
            QuickMC.getPacketEventsAPI().getPlayerManager().sendPacket(player, updateAttributes);

        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
    public static double getRealValue(Player player, Attribute attribute) {
        if(player.isDead() || !player.isOnline() || INSTANCE_VALUE == null) return -1;

        Object attributeInst = getAttributeInstance(player, attribute);
        if(attributeInst == null) return -1;

        try {
            return (double) CALCULATE_METHOD.invoke(attributeInst);

        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }



    /* Helper Methods */

    public static void resetAllAttributes(LivingEntity entity) {
        if(!(entity instanceof Player player)) return;
        for(Attribute attribute : Attribute.values()) {
            resetToRealValue(player, attribute);
        }
    }

    public static void clearAllAttributeModifiers(LivingEntity entity, String key) {
        for(Attribute attribute : Attribute.values()) {
            AttributeInstance inst = entity.getAttribute(attribute);
            if(inst == null) continue;

            List<AttributeModifier> modifiers = new ArrayList<>();
            for(AttributeModifier mod : inst.getModifiers()) {
                if(key.isEmpty() || mod.getName().contains(key)) modifiers.add(mod);
            }

            for(AttributeModifier modifier : modifiers) {
                inst.removeModifier(modifier);
            }
        }
    }
    public static void clearAttributeModifier(LivingEntity entity, String key) {
        for(Attribute attribute : Attribute.values()) {
            AttributeInstance inst = entity.getAttribute(attribute);
            if(inst == null) continue;

            List<AttributeModifier> modifiers = new ArrayList<>();
            for(AttributeModifier mod : inst.getModifiers()) {
                if(key.isEmpty() || mod.getName().contains(key)) modifiers.add(mod);
            }

            for(AttributeModifier modifier : modifiers) {
                inst.removeModifier(modifier);
            }
        }
    }

    private static WrapperPlayServerUpdateAttributes.PropertyModifier.Operation convertOperation(AttributeModifier.Operation operation) {
        if(operation == AttributeModifier.Operation.ADD_NUMBER) return WrapperPlayServerUpdateAttributes.PropertyModifier.Operation.ADDITION;
        if(operation == AttributeModifier.Operation.ADD_SCALAR) return WrapperPlayServerUpdateAttributes.PropertyModifier.Operation.MULTIPLY_BASE;
        if(operation == AttributeModifier.Operation.MULTIPLY_SCALAR_1) return WrapperPlayServerUpdateAttributes.PropertyModifier.Operation.MULTIPLY_TOTAL;
        return null;
    }

    public static void healEntity(LivingEntity entity, double amount) {
        double sum = entity.getHealth() + amount;
        entity.setHealth(Math.min(sum, getEntityMaxHealth(entity)));
    }
    public static double getEntityMaxHealth(LivingEntity entity) {
        AttributeInstance instance = entity.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        return instance != null ? instance.getBaseValue() : -1;
    }

}
