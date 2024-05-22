package com.pro4d.quickmc.visuals;

import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class SimpleItemDisplay extends SimpleDisplay{

    private final WrappedDataWatcher.WrappedDataWatcherObject itemObject = new WrappedDataWatcher.WrappedDataWatcherObject(23, WrappedDataWatcher.Registry.getItemStackSerializer(false)),
            settingsObject = new WrappedDataWatcher.WrappedDataWatcherObject(24, WrappedDataWatcher.Registry.get(Byte.class));

    public SimpleItemDisplay(Location location, UUID uuid, int id) {
        super(EntityType.ITEM_DISPLAY, location, uuid, id);
    }

    public SimpleItemDisplay(Location location) {
        super(EntityType.ITEM_DISPLAY, location);
    }

    public SimpleItemDisplay setItem(ItemStack item) {
        super.getDataWatcher().setObject(itemObject, item);
        super.sendMetadata();
        return this;
    }

    public SimpleItemDisplay setDisplayType(SimpleDisplayType displayType) {
        super.getDataWatcher().setObject(settingsObject, (byte) displayType.getId());
        super.sendMetadata();
        return this;
    }

    public enum SimpleDisplayType {
        NONE(0),
        THIRDPERSON_LEFTHAND(1),
        THIRDPERSON_RIGHTHAND(2),
        FIRSTPERSON_LEFTHAND(3),
        FIRSTPERSON_RIGHTHAND(4),
        HEAD(5),
        GUI(6),
        GROUND(7),
        FIXED(8);

        private final int id;
        SimpleDisplayType(int var) {
            this.id = var;
        }

        public static SimpleDisplayType getFromID(int id) {
            for(SimpleDisplayType displayType : SimpleDisplayType.values()) {
                if(displayType.getId() == id) return displayType;
            }
            return NONE;
        }

        public int getId() {
            return id;
        }
    }

}
