package com.pro4d.quickmc.visuals;

import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.cryptomorin.xseries.ReflectionUtils;
import org.bukkit.Location;
import org.bukkit.block.BlockState;
import org.bukkit.entity.EntityType;

import java.lang.reflect.InvocationTargetException;
import java.util.UUID;

public class SimpleBlockDisplay extends SimpleDisplay {

    private final WrappedDataWatcher.WrappedDataWatcherObject blockObject = new WrappedDataWatcher.WrappedDataWatcherObject(23,
            WrappedDataWatcher.Registry.get(ReflectionUtils.getNMSClass("world.level.block.state", "IBlockData")));

    public SimpleBlockDisplay(Location location, UUID uuid, int id) {
        super(EntityType.BLOCK_DISPLAY, location, uuid, id);
    }

    public SimpleBlockDisplay(Location location) {
        super(EntityType.BLOCK_DISPLAY, location);
    }

    public SimpleBlockDisplay setBlock(BlockState state) {
        try {
            Object result = SpigotReflectionUtil.HANDLE_METHOD.invoke(state, null);
            super.getDataWatcher().setObject(blockObject, result);
            super.sendMetadata();

        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
        return this;
    }

}
