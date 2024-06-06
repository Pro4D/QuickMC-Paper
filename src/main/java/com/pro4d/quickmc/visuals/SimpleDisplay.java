package com.pro4d.quickmc.visuals;

import com.comphenix.protocol.wrappers.WrappedDataValue;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.google.common.collect.Lists;
import com.pro4d.quickmc.util.packets.*;
import it.unimi.dsi.fastutil.ints.IntList;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public abstract class SimpleDisplay {

    public static final WrappedDataWatcher.Serializer VECTOR_SERIALIZER = WrappedDataWatcher.Registry.get(Vector3f.class);
    public static final WrappedDataWatcher.Serializer QUATERNION_SERIALIZER = WrappedDataWatcher.Registry.getVectorSerializer();

    private final WrappedDataWatcher.WrappedDataWatcherObject
            entitySettings = new WrappedDataWatcher.WrappedDataWatcherObject(0, WrappedDataWatcher.Registry.get(Byte.class)),

            translationObject = new WrappedDataWatcher.WrappedDataWatcherObject(11, VECTOR_SERIALIZER),
            scaleObject = new WrappedDataWatcher.WrappedDataWatcherObject(12, VECTOR_SERIALIZER),
            leftRotationObject = new WrappedDataWatcher.WrappedDataWatcherObject(13, QUATERNION_SERIALIZER),
            rightRotationObject = new WrappedDataWatcher.WrappedDataWatcherObject(14, QUATERNION_SERIALIZER),
            billboardObject = new WrappedDataWatcher.WrappedDataWatcherObject(15, WrappedDataWatcher.Registry.get(Byte.class)),
            brightnessObject = new WrappedDataWatcher.WrappedDataWatcherObject(16, WrappedDataWatcher.Registry.get(Integer.class)),
            viewRangeObject = new WrappedDataWatcher.WrappedDataWatcherObject(17, WrappedDataWatcher.Registry.get(Float.class)),
            shadowStrengthObject = new WrappedDataWatcher.WrappedDataWatcherObject(18, WrappedDataWatcher.Registry.get(Float.class)),
            shadowRadiusObject = new WrappedDataWatcher.WrappedDataWatcherObject(19, WrappedDataWatcher.Registry.get(Float.class)),
            widthObject = new WrappedDataWatcher.WrappedDataWatcherObject(20, WrappedDataWatcher.Registry.get(Float.class)),
            heightObject = new WrappedDataWatcher.WrappedDataWatcherObject(21, WrappedDataWatcher.Registry.get(Float.class)),
            glowColorObject = new WrappedDataWatcher.WrappedDataWatcherObject(22, WrappedDataWatcher.Registry.get(Integer.class));


    @Getter private Entity riding;
    @Getter private Location loc;

    @Getter private final WrappedDataWatcher dataWatcher;
    private final EntityType type;
    @Getter private final List<UUID> received;

    @Getter private final UUID uuid;
    @Getter private final int entityID;
    public SimpleDisplay(EntityType displayType, Location location, UUID uuid, int id) {
        this.type = displayType;
        this.loc = location;
        this.uuid = uuid;
        this.entityID = id;

        this.received = new ArrayList<>();
        this.dataWatcher = new WrappedDataWatcher();
    }

    public SimpleDisplay(EntityType displayType, Location location) {
        this(displayType, location, UUID.randomUUID(), SpigotReflectionUtil.generateEntityId());
    }

    public SimpleDisplay spawn() {
        for(Player online : Bukkit.getOnlinePlayers()) {
            spawnForPlayer(online);
        }
        return this;
    }
    public SimpleDisplay spawnForPlayer(Player player) {
        WrapperPlayServerSpawnEntity spawn = new WrapperPlayServerSpawnEntity();
        spawn.setId(entityID);

        spawn.setType(type);
        spawn.setUuid(uuid);

        spawn.setX(loc.getX());
        spawn.setY(loc.getY());
        spawn.setZ(loc.getZ());

        byte pitch = (byte) (loc.getPitch() * (256.0F / 360.0F));
        spawn.getHandle().getBytes().write(0, pitch);

        byte yaw = (byte) (loc.getYaw() * (256.0F / 360.0F));
        spawn.getHandle().getBytes().write(1, yaw);
        spawn.getHandle().getBytes().write(2, yaw);

        spawn.sendPacket(player);
        this.received.add(player.getUniqueId());

        sendMetadataToPlayer(player);
        return this;
    }

    public SimpleDisplay remove() {
        for(Player online : Bukkit.getOnlinePlayers()) {
            removeForPlayer(online);
        }
        return this;
    }
    public SimpleDisplay removeForPlayer(Player player) {
        WrapperPlayServerEntityDestroy packet = new WrapperPlayServerEntityDestroy();
        packet.setEntityIds(IntList.of(entityID));

        packet.sendPacket(player);
        this.received.remove(player.getUniqueId());
        return this;
    }

    public SimpleDisplay setTranslation(Vector3f vector) {
        dataWatcher.setObject(translationObject, vector);
        return this;
    }

    public SimpleDisplay setScale(Vector3f vector) {
        dataWatcher.setObject(scaleObject, vector);
        return this;
    }

    public SimpleDisplay setLeftRotation(AxisAngle4f rotation) {
        dataWatcher.setObject(leftRotationObject, rotation);
        return this;
    }

    public SimpleDisplay setRightRotation(AxisAngle4f rotation) {
        dataWatcher.setObject(rightRotationObject, rotation);
        return this;
    }

    public SimpleDisplay setBillboard(SimpleBillboard billboard) {
        byte b = (byte) billboard.getId();
        dataWatcher.setObject(billboardObject, b);
        return this;
    }

    // TODO implement
//    public SimpleDisplay setBrightness(int brightness) {
//        dataWatcher.setObject(brightnessObject, brightness);
//        return this;
//    }

    public SimpleDisplay setGlowing(boolean glow) {
        Byte existing = dataWatcher.getByte(0);

        if(existing == null) {
            if(glow) existing = (byte) 0x40;
        } else {
            if(glow) {
                existing = (byte) (existing | 0x40);
            } else {
                existing = (byte) (existing & ~0x40);
            }
        }

        dataWatcher.setObject(entitySettings, existing);
        return this;
    }

    public SimpleDisplay setViewRange(float viewRange) {
        dataWatcher.setObject(viewRangeObject, viewRange);
        return this;
    }

    public SimpleDisplay setShadowStrength(float shadowStrength) {
        dataWatcher.setObject(shadowStrengthObject, shadowStrength);
        return this;
    }

    public SimpleDisplay setShadowRadius(float shadowRadius) {
        dataWatcher.setObject(shadowRadiusObject, shadowRadius);
        return this;
    }

    public SimpleDisplay setWidth(float width) {
        dataWatcher.setObject(widthObject, width);
        return this;
    }

    public SimpleDisplay setHeight(float height) {
        dataWatcher.setObject(heightObject, height);
        return this;
    }

    public SimpleDisplay passengerFor(Entity entity) {
        this.riding = entity;

        List<Entity> passengers = entity.getPassengers();
        int size = passengers.size();
        int[] array = new int[size + 1];
        for(int i = 0; i < size; i++) {
            array[i] = passengers.get(i).getEntityId();
        }
        array[size] = this.entityID;

        WrapperPlayServerMount mount = new WrapperPlayServerMount();
        mount.setPassengers(array);
        mount.setVehicle(this.riding.getEntityId());

        mount.broadcastPacket();
        return this;
    }

    public SimpleDisplay teleport(Location loc) {
        this.loc = loc;

        WrapperPlayServerEntityTeleport teleport = new WrapperPlayServerEntityTeleport();
        teleport.setId(entityID);
        teleport.setX(this.loc.getX());
        teleport.setY(this.loc.getY());
        teleport.setZ(this.loc.getZ());

        byte pitch = (byte) (this.loc.getPitch() * (256.0F / 360.0F));
        teleport.setXRot(pitch);

        byte yaw = (byte) (this.loc.getYaw() * (256.0F / 360.0F));
        teleport.setYRot(yaw);
        //spawn.getHandle().getBytes().write(2, yaw);

        teleport.broadcastPacket();
        return this;
    }

    public SimpleDisplay setGlowColor(Color color) {
        dataWatcher.setObject(glowColorObject, color.asARGB());
        return this;
    }
    public static int argbToInt(int alpha, int red, int green, int blue) {
        return (alpha << 24) | (red << 16) | (green << 8) | blue;
    }

    public SimpleDisplay sendMetadata() {
        for(Player online : Bukkit.getOnlinePlayers()) {
            sendMetadataToPlayer(online);
        }
        return this;
    }
    public SimpleDisplay sendMetadataToPlayer(Player player) {
        if(!received.contains(player.getUniqueId())) return this;
        WrapperPlayServerEntityMetadata packet = new WrapperPlayServerEntityMetadata();
        packet.setId(entityID);

        List<WrappedDataValue> wrappedDataValueList = Lists.newArrayList();
        dataWatcher.getWatchableObjects().stream().filter(Objects::nonNull).forEach(entry -> {
            final WrappedDataWatcher.WrappedDataWatcherObject dataWatcherObject = entry.getWatcherObject();
            wrappedDataValueList.add(new WrappedDataValue(dataWatcherObject.getIndex(), dataWatcherObject.getSerializer(), entry.getRawValue()));
        });

        packet.getHandle().getDataValueCollectionModifier().write(0, wrappedDataValueList);
        packet.sendPacket(player);
        return this;
    }

    public enum SimpleBillboard {
        FIXED(0),
        VERTICAL(1),
        HORIZONTAL(2),
        CENTER(3);

        private final int id;
        SimpleBillboard(int var) {
            this.id = var;
        }

        public static SimpleBillboard getFromID(int id) {
            for(SimpleBillboard billboard : SimpleBillboard.values()) {
                if(billboard.getId() == id) return billboard;
            }
            return FIXED;
        }

        public int getId() {
            return id;
        }
    }

}
