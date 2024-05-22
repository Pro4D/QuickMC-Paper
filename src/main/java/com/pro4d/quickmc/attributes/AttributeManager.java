package com.pro4d.quickmc.attributes;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedAttribute;
import com.cryptomorin.xseries.ReflectionUtils;
import com.pro4d.quickmc.QuickMC;
import com.pro4d.quickmc.QuickUtils;
import com.pro4d.quickmc.events.AttributeApplyEvent;
import com.pro4d.quickmc.util.packets.WrapperPlayServerUpdateAttributes;
import de.tr7zw.changeme.nbtapi.NBTCompound;
import de.tr7zw.changeme.nbtapi.NBTCompoundList;
import de.tr7zw.changeme.nbtapi.NBTEntity;
import de.tr7zw.changeme.nbtapi.NBTFile;
import de.tr7zw.changeme.nbtapi.iface.ReadWriteNBT;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.attribute.Attributable;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Getter
public class AttributeManager implements Listener {

    @Getter public static AttributeManager instance;

    private static Field ATTRIBUTE_MAP, HANDLE, INSTANCE_VALUE;
    private static Method INSTANCE_METHOD, CONVERTOR_METHOD;

    // TODO adjust the setVariable (NMS) related methods. so they adjust according to the real value. aka if changing armor

    private final Map<UUID, Set<AttributeValueData>> customAttributeValues;
    public AttributeManager() {
        instance = this;
        this.customAttributeValues = new HashMap<>();

        cacheReflection();

        ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
        protocolManager.addPacketListener(new PacketAdapter(QuickMC.getSourcePlugin(), ListenerPriority.NORMAL, PacketType.Play.Server.UPDATE_ATTRIBUTES) {
            @Override
            public void onPacketSending(PacketEvent event) {
                WrapperPlayServerUpdateAttributes updateAttributes = new WrapperPlayServerUpdateAttributes(event.getPacket());
                LivingEntity entity = null;
                int eid = updateAttributes.getEntityId();
                for(World world : Bukkit.getServer().getWorlds()) {
                    for(LivingEntity living : world.getLivingEntities()) {
                        if(living.getEntityId() != eid) continue;
                        entity = living;
                        break;
                    }
                }
                if(entity == null) return;

                Set<AttributeValueData> attributeValues = getAttributeValueData(entity.getUniqueId());
                if(attributeValues.isEmpty()) return;

                List<WrappedAttribute> attributes = updateAttributes.getAttributes();

                Map<Integer, WrappedAttribute> replaceMap = new HashMap<>();
                for(int a = 0; a < attributes.size(); a++) {
                    String key = attributes.get(a).getAttributeKey();
                    for(AttributeValueData data : attributeValues) {
                        String k1 = data.getAttribute().getKey().getKey();
                        if(!k1.equalsIgnoreCase(key)) continue;

                        AttributeInstance instance = entity.getAttribute(data.getAttribute());
                        if(instance != null) replaceMap.put(a, WrappedAttribute.newBuilder().baseValue(instance.getValue()).attributeKey(k1).build());
                    }
                }

                for(int i : replaceMap.keySet()) {
                    WrappedAttribute associated = replaceMap.getOrDefault(i, null);
                    if(associated != null) attributes.set(i, associated);
                }
                updateAttributes.setAttributes(attributes);

                event.setPacket(updateAttributes.getHandle());
            }
        });
    }

    private void cacheReflection() {
        Class<?> LIVING_ENTITY = ReflectionUtils.getNMSClass("world.entity", "EntityLiving");

        Field craftAttributeMap = null;
        for(Field df : LIVING_ENTITY.getDeclaredFields()) {
            if(!getNameOfClass(df.getType().getName()).equals("CraftAttributeMap")) continue;
            craftAttributeMap = df;
            break;
        }

        if(craftAttributeMap != null) {
            ATTRIBUTE_MAP = craftAttributeMap;
            ATTRIBUTE_MAP.setAccessible(true);
        }

        try {
            HANDLE = ReflectionUtils.getCraftClass("attribute.CraftAttributeMap").getDeclaredField("handle");
            HANDLE.setAccessible(true);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }

        Method targetMethod = null;
        for(Method m : ReflectionUtils.getNMSClass("world.entity.ai.attributes.AttributeMapBase").getMethods()) {
            Parameter[] parameters = m.getParameters();
            if(parameters.length != 1) continue;

            String two = getNameOfClass(m.getReturnType().getName());
            if(!two.equals("AttributeModifiable")) continue;

            String four = getNameOfClass(parameters[0].getType().getName());
            if(!four.equals("AttributeBase")) continue;
            targetMethod = m;
            break;
        }
        if(targetMethod != null) {
            INSTANCE_METHOD = targetMethod;
            INSTANCE_METHOD.setAccessible(true);
        }

        try {
            CONVERTOR_METHOD = ReflectionUtils.getCraftClass("attribute.CraftAttribute").getDeclaredMethod("bukkitToMinecraft", Attribute.class);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }

        try {
            INSTANCE_VALUE = ReflectionUtils.getNMSClass("world.entity.ai.attributes", "AttributeModifiable").getDeclaredField("g");
            INSTANCE_VALUE.setAccessible(true);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }
    private String getNameOfClass(String clazz) {
        String[] parts = clazz.split("\\.");
        return parts[parts.length - 1];
    }

    public static void setBukkitAttribute(LivingEntity target, Attribute attribute, double value) {
        AttributeInstance instance = target.getAttribute(attribute);
        if(instance != null) instance.setBaseValue(value);
    }
    public static void setTimedBukkitAttribute(LivingEntity target, Attribute attribute, double value, double restore, long duration) {
        setBukkitAttribute(target, attribute, value);

        QuickUtils.syncLater(duration, () -> setBukkitAttribute(target, attribute, restore));
    }

    public static void setAttribute(LivingEntity target, Attribute attribute, double value) {
        AttributeApplyEvent event = new AttributeApplyEvent(target, attribute, value);
        QuickMC.getSourcePlugin().getServer().getPluginManager().callEvent(event);
        if(event.isCancelled()) return;

        attribute = event.getAttribute();
        value = event.getValue();

        NBTCompound compound = getCompound(target);
        getNBT(compound, attribute).setDouble("Base", value);

        AttributeInstance instance = target.getAttribute(attribute);
        assert instance != null;
        instance.setBaseValue(value);

        if(!(compound instanceof NBTFile nbtFile)) return;
        CompletableFuture.runAsync(() -> {
            try {
                nbtFile.save();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
    public static void setTimedAttribute(LivingEntity target, Attribute attribute, double value, double restore, long duration) {
        setAttribute(target, attribute, value);

        QuickUtils.syncLater(duration, () -> setAttribute(target, attribute, restore));
    }

    public static void setAttributeValue(Player target, Attribute attribute, double value, AttributeValueData.AttributeOperation operation) {
        AttributeApplyEvent event = new AttributeApplyEvent(target, attribute, value);
        QuickMC.getSourcePlugin().getServer().getPluginManager().callEvent(event);
        if(event.isCancelled()) return;

        attribute = event.getAttribute();
        value = event.getValue();

        AttributeInstance instance = target.getAttribute(attribute);
        if(instance == null) return;

        double v = instance.getValue();
        if(operation == AttributeValueData.AttributeOperation.SET) {
            v = value;
        } else if(operation == AttributeValueData.AttributeOperation.ADJUST) {
            v += value;
        }

        set(target, attribute, v);

        if(operation != AttributeValueData.AttributeOperation.NOTHING) {
            Set<AttributeValueData> set = getInstance().getCustomAttributeValues().getOrDefault(target.getUniqueId(), new HashSet<>());
            if(getValueData(set, attribute) == null) {
                set.add(new AttributeValueData(attribute, operation, value));
                getInstance().getCustomAttributeValues().put(target.getUniqueId(), set);
            }
        }

        WrapperPlayServerUpdateAttributes attributesPacket = new WrapperPlayServerUpdateAttributes();
        attributesPacket.setAttributes(List.of(WrappedAttribute.newBuilder().baseValue(v).attributeKey(attribute.getKey().getKey()).build()));
        attributesPacket.setEntityId(target.getEntityId());
        attributesPacket.broadcastPacket();
    }
    public static void setTimedAttributeValue(Player target, Attribute attribute, double value, AttributeValueData.AttributeOperation operation,
                                              double restore, long duration) {
        setAttributeValue(target, attribute, value, operation);

        QuickUtils.syncLater(duration, () -> clearAttributeValue(target, attribute, restore));
    }

    public static void clearAttributeValue(Player target, Attribute attribute, double restore) {
        Set<AttributeValueData> set = getInstance().getCustomAttributeValues().getOrDefault(target.getUniqueId(), new HashSet<>());
        AttributeValueData data = getValueData(set, attribute);
        if(data != null) {
            set.remove(data);
            getInstance().getCustomAttributeValues().put(target.getUniqueId(), set);
        }

        set(target, attribute, restore);

        WrapperPlayServerUpdateAttributes attributesPacket = new WrapperPlayServerUpdateAttributes();
        attributesPacket.setAttributes(List.of(WrappedAttribute.newBuilder().baseValue(restore).attributeKey(attribute.getKey().getKey()).build()));
        attributesPacket.setEntityId(target.getEntityId());
        attributesPacket.broadcastPacket();
    }

    private static void set(Player player, Attribute attribute, double val) {
        // TODO add support for other entities
        Object entityPlayer = ReflectionUtils.getHandle(player);
        if(entityPlayer == null) return;

        try {
            Object attributeMap = ATTRIBUTE_MAP.get(entityPlayer);
            Object mapBase = HANDLE.get(attributeMap);

            Object attributeInst = INSTANCE_METHOD.invoke(mapBase, CONVERTOR_METHOD.invoke(null, attribute));
            if(attributeInst != null) INSTANCE_VALUE.setDouble(attributeInst, val);

        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    public static Set<AttributeValueData> getAttributeValueData(UUID uuid) {
        return getInstance().getCustomAttributeValues().getOrDefault(uuid, new HashSet<>());
    }
    public static AttributeValueData getValueData(Set<AttributeValueData> set, Attribute attribute) {
        String key = attribute.getKey().getKey();
        for(AttributeValueData data : set) {
            if(data.getAttribute().getKey().getKey().equalsIgnoreCase(key)) return data;
        }
        return null;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private void refresh(EntityDamageEvent event) {
        if(!(event.getEntity() instanceof Player player)) return;
        Set<AttributeValueData> valueData = getAttributeValueData(player.getUniqueId());
        if(valueData.isEmpty()) return;

        for(AttributeValueData data : valueData) {
            Attribute attribute = data.getAttribute();
            AttributeInstance inst = player.getAttribute(attribute);
            if(inst == null) continue;

            double v = inst.getValue();

            //setAttributeValue(player, attribute, v, AttributeValueData.AttributeOperation.NOTHING);
            QuickUtils.syncLater(2, () -> setAttributeValue(player, attribute, v, AttributeValueData.AttributeOperation.SET));
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private void refresh(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Set<AttributeValueData> valueData = getAttributeValueData(player.getUniqueId());
        if(valueData.isEmpty()) return;

        for(AttributeValueData data : valueData) {
            Attribute attribute = data.getAttribute();
            AttributeInstance inst = player.getAttribute(attribute);
            if(inst == null) continue;

            double v = inst.getValue();

            //setAttributeValue(player, attribute, v, AttributeValueData.AttributeOperation.NOTHING);
            QuickUtils.syncLater(2, () -> setAttributeValue(player, attribute, v, AttributeValueData.AttributeOperation.SET));
        }
    }

    public static double getAttributeValue(LivingEntity entity, Attribute attribute) {
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
    public static NBTCompound getCompound(LivingEntity entity) {
        if(!(entity instanceof Player player)) {
            return new NBTEntity(entity);
        } else if(player.isOnline()) return new NBTEntity(entity);

        NBTFile file;
        try {
            String path = entity.getServer().getWorldContainer().getPath();
            path = path.substring(0, path.length() - 1);

            String defWorld = entity.getServer().getWorlds().get(0).getName();
            file = new NBTFile(new File(path + defWorld + "/playerdata/" + player.getUniqueId() + ".dat"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return file;
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


    public static void resetAllAttributes(LivingEntity entity) {
        for(Attribute attribute : Attribute.values()) {
            AttributeManager.setAttribute(entity, attribute, AttributeManager.getDefaultValue(entity, attribute));
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


    public static void healEntity(LivingEntity entity, double amount) {
        double sum = entity.getHealth() + amount;
        entity.setHealth(Math.min(sum, getEntityMaxHealth(entity)));
    }
    public static double getEntityMaxHealth(LivingEntity entity) {
        AttributeInstance instance = entity.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        return instance != null ? instance.getBaseValue() : 0;
    }

}
