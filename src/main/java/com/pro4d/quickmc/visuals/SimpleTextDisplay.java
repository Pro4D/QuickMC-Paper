package com.pro4d.quickmc.visuals;

import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;

import java.util.UUID;

public class SimpleTextDisplay extends SimpleDisplay{

    public static final WrappedDataWatcher.Serializer COMPONENT_SERIALIZER = WrappedDataWatcher.Registry.getChatComponentSerializer(false);

    private final WrappedDataWatcher.WrappedDataWatcherObject textObject = new WrappedDataWatcher.WrappedDataWatcherObject(23, COMPONENT_SERIALIZER),
            lineWidthObject = new WrappedDataWatcher.WrappedDataWatcherObject(24, WrappedDataWatcher.Registry.get(Integer.class)),
            backgroundColorObject = new WrappedDataWatcher.WrappedDataWatcherObject(25, WrappedDataWatcher.Registry.get(Integer.class)),
            textOpacityObject = new WrappedDataWatcher.WrappedDataWatcherObject(26, WrappedDataWatcher.Registry.get(Byte.class)),
            settingsObject = new WrappedDataWatcher.WrappedDataWatcherObject(27, WrappedDataWatcher.Registry.get(Byte.class));

    public SimpleTextDisplay(Location location, UUID uuid, int id) {
        super(EntityType.TEXT_DISPLAY, location, uuid, id);
    }

    public SimpleTextDisplay(Location location) {
        super(EntityType.TEXT_DISPLAY, location);
    }

    public SimpleTextDisplay setText(String s) {
        super.getDataWatcher().setObject(textObject, WrappedChatComponent.fromLegacyText(s).getHandle());
        super.sendMetadata();
        return this;
    }

    public SimpleTextDisplay setLineWidth(int lineWidth) {
        super.getDataWatcher().setObject(lineWidthObject, lineWidth);
        super.sendMetadata();
        return this;
    }

    // All values must be between 0 and 255
    public SimpleTextDisplay setBackgroundColor(Color color) {
        super.getDataWatcher().setObject(backgroundColorObject,
                SimpleDisplay.argbToInt(color.getAlpha(), color.getRed(), color.getGreen(), color.getBlue())
        );
        super.sendMetadata();
        return this;
    }

    // Between -128 to 127, roughly
    public void setTextOpacity(int opacity) {
        byte o = (byte) opacity;
        super.getDataWatcher().setObject(textOpacityObject, o);
        super.sendMetadata();
    }

    public SimpleTextDisplay setHasShadow(boolean v) {
        Byte existing = super.getDataWatcher().getByte(27);

        if(existing == null) {
            if(v) existing = (byte) 0x01;
        } else {
            if(v) {
                existing = (byte) (existing | 0x01);
            } else {
                existing = (byte) (existing & ~0x01);
            }
        }

        super.getDataWatcher().setObject(settingsObject, existing);
        super.sendMetadata();
        return this;
    }

    public SimpleTextDisplay setSeeThrough(boolean v) {
        Byte existing = super.getDataWatcher().getByte(27);

        if(existing == null) {
            if(v) existing = (byte) 0x02;
        } else {
            if(v) {
                existing = (byte) (existing | 0x02);
            } else {
                existing = (byte) (existing & ~0x02);
            }
        }

        super.getDataWatcher().setObject(settingsObject, existing);
        super.sendMetadata();
        return this;
    }
    public SimpleTextDisplay setHasDefaultBackground(boolean v) {
        Byte existing = super.getDataWatcher().getByte(27);

        if(existing == null) {
            if(v) existing = (byte) 0x04;
        } else {
            if(v) {
                existing = (byte) (existing | 0x04);
            } else {
                existing = (byte) (existing & ~0x04);
            }
        }

        super.getDataWatcher().setObject(settingsObject, existing);
        super.sendMetadata();
        return this;
    }

    public SimpleTextDisplay setAlignment(SimpleAlignment alignment) {
        Byte existing = super.getDataWatcher().getByte(27);
        if(existing == null) existing = 0;

        existing = (byte) (existing & ~0x18); // Clear existing alignment bits

        byte newAlignmentByte = (byte) (alignment.getId() * 0x08); // Calculate and set new alignment bits
        existing = (byte) (existing | newAlignmentByte);

        super.getDataWatcher().setObject(settingsObject, existing);
        super.sendMetadata();
        return this;
    }

    public enum SimpleAlignment {
        CENTER(0),
        LEFT(1),
        RIGHT(2);

        private final int id;
        SimpleAlignment(int var) {
            this.id = var;
        }

        public static SimpleAlignment getFromID(int id) {
            for(SimpleAlignment alignment : SimpleAlignment.values()) {
                if(alignment.getId() == id) return alignment;
            }
            return CENTER;
        }

        public int getId() {
            return id;
        }
    }

}
