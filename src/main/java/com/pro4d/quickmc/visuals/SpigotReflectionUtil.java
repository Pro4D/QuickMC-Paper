/*
 * This file is part of packetevents - https://github.com/retrooper/packetevents
 * Copyright (C) 2022 retrooper and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.pro4d.quickmc.visuals;

import com.cryptomorin.xseries.ReflectionUtils;
import com.google.common.collect.MapMaker;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
@author* Cut-down class. Originally from "PacketEvents" by retrooper
 **/
public final class SpigotReflectionUtil {
    private static final String MODIFIED_PACKAGE_NAME, LEGACY_NMS_PACKAGE, OBC_PACKAGE;

    static {
        String cbPackage = Bukkit.getServer().getClass().getPackage().getName();
        String temp;
        try {
            temp = cbPackage.replace(".", ",").split(",")[3];
        } catch (Exception ex) {
            temp = "";
        }
        MODIFIED_PACKAGE_NAME = temp;

        LEGACY_NMS_PACKAGE = "net.minecraft.server." + MODIFIED_PACKAGE_NAME + ".";
        OBC_PACKAGE = cbPackage + ".";
    }

    //Minecraft classes
    public static Class<?> NMS_ENTITY_CLASS, ENTITY_PLAYER_CLASS, PLAYER_CONNECTION_CLASS, WORLD_SERVER_CLASS, CRAFT_WORLD_CLASS, CRAFT_PLAYER_CLASS,
            CRAFT_ENTITY_CLASS, LEVEL_ENTITY_GETTER_CLASS, PERSISTENT_ENTITY_SECTION_MANAGER_CLASS, PAPER_ENTITY_LOOKUP_CLASS;

    //Methods
    public static Method GET_BUKKIT_ENTITY_METHOD, GET_CRAFT_PLAYER_HANDLE_METHOD, GET_CRAFT_WORLD_HANDLE_METHOD,
            GET_ENTITY_BY_ID_LEVEL_ENTITY_GETTER_METHOD, GET_ENTITY_BY_ID_METHOD, HANDLE_METHOD;

    private static boolean PAPER_ENTITY_LOOKUP_EXISTS = false;

    //Cache entities right after we request/find them for faster search.
    public static Map<Integer, Entity> ENTITY_ID_CACHE = new MapMaker().weakValues().makeMap();

    public static void init() {
        initClasses();
        initFields();
        initMethods();
    }
    private static void initMethods() {
        GET_CRAFT_PLAYER_HANDLE_METHOD = getMethod(CRAFT_PLAYER_CLASS, "getHandle", 0);
        GET_CRAFT_WORLD_HANDLE_METHOD = getMethod(CRAFT_WORLD_CLASS, "getHandle", 0);
        GET_ENTITY_BY_ID_LEVEL_ENTITY_GETTER_METHOD = getMethod(LEVEL_ENTITY_GETTER_CLASS, "a", int.class);
        GET_BUKKIT_ENTITY_METHOD = getMethod(NMS_ENTITY_CLASS, CRAFT_ENTITY_CLASS, 0);

        GET_ENTITY_BY_ID_METHOD = getMethodExact(WORLD_SERVER_CLASS, "b", NMS_ENTITY_CLASS, int.class);
        if(GET_ENTITY_BY_ID_METHOD == null) GET_ENTITY_BY_ID_METHOD = getMethodExact(WORLD_SERVER_CLASS, "getEntity", NMS_ENTITY_CLASS, int.class);

        Method method = null;
        for(Method m : ReflectionUtils.getCraftClass("block.CraftBlockState").getDeclaredMethods()) {
            if(!m.getName().equalsIgnoreCase("getHandle")) continue;
            method = m;
            break;
        }
        HANDLE_METHOD = method;
    }
    private static void initClasses() {
        NMS_ENTITY_CLASS = getServerClass("world.entity.Entity");
        ENTITY_PLAYER_CLASS = getServerClass("server.level.EntityPlayer");
        PLAYER_CONNECTION_CLASS = getServerClass("server.network.PlayerConnection");

        WORLD_SERVER_CLASS = getServerClass("server.level.WorldServer");

        LEVEL_ENTITY_GETTER_CLASS = getServerClass("world.level.entity.LevelEntityGetter");
        PERSISTENT_ENTITY_SECTION_MANAGER_CLASS = getServerClass("world.level.entity.PersistentEntitySectionManager");
        PAPER_ENTITY_LOOKUP_CLASS = getClassByNameWithoutException("io.papermc.paper.chunk.system.entity.EntityLookup");

        CRAFT_WORLD_CLASS = getOBCClass("CraftWorld");
        CRAFT_PLAYER_CLASS = getOBCClass("entity.CraftPlayer");
        CRAFT_ENTITY_CLASS = getOBCClass("entity.CraftEntity");
    }
    private static void initFields() {
        PAPER_ENTITY_LOOKUP_EXISTS = getField(WORLD_SERVER_CLASS, PAPER_ENTITY_LOOKUP_CLASS, 0) != null;
    }

    @Nullable
    public static Class<?> getServerClass(String modern) {
        return getClassByNameWithoutException("net.minecraft." + modern);
    }
    public static Class<?> getNMSClass(String name) throws ClassNotFoundException {
        return Class.forName(LEGACY_NMS_PACKAGE + name);
    }
    public static Class<?> getOBCClass(String name) {
        return getClassByNameWithoutException(OBC_PACKAGE + name);
    }

    public static Object getCraftPlayer(Player player) {
        return CRAFT_PLAYER_CLASS.cast(player);
    }
    public static Object getEntityPlayer(Player player) {
        Object craftPlayer = getCraftPlayer(player);
        try {
            return GET_CRAFT_PLAYER_HANDLE_METHOD.invoke(craftPlayer);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static int generateEntityId() {
        Field field = getField(NMS_ENTITY_CLASS, "entityCount");
        if(field == null) field = getField(NMS_ENTITY_CLASS, AtomicInteger.class, 0);

        try {
            if(field.getType().equals(AtomicInteger.class)) {
                AtomicInteger atomicInteger = (AtomicInteger) field.get(null);
                return atomicInteger.incrementAndGet();
            } else {
                int id = field.getInt(null) + 1;
                field.set(null, id);
                return id;
            }
        } catch(IllegalAccessException ex) {ex.printStackTrace();}
        throw new IllegalStateException("Failed to generate a new unique entity ID!");
    }

    public static Field[] getFields(final Class<?> cls) {
        final Field[] declaredFields2;
        final Field[] declaredFields = declaredFields2 = cls.getDeclaredFields();
        for(final Field f : declaredFields2) f.setAccessible(true);
        return declaredFields;
    }
    public static Field getField(final Class<?> cls, final String name) {
        for(final Field f : getFields(cls)) {if(f.getName().equals(name)) return f;}

        if(cls.getSuperclass() != null) return getField(cls.getSuperclass(), name);
        return null;
    }
    public static Field getField(final Class<?> cls, final Class<?> dataType, final int index) {
        if(dataType == null || cls == null) return null;

        int currentIndex = 0;
        for(final Field f : getFields(cls)) {if(dataType.isAssignableFrom(f.getType()) && currentIndex++ == index) return f;}

        if(cls.getSuperclass() != null) return getField(cls.getSuperclass(), dataType, index);
        return null;
    }
    public static Class<?> getClassByNameWithoutException(final String name) {
        try {return Class.forName(name);
        } catch(ClassNotFoundException e) {return null;}
    }

    public static Method getMethod(final Class<?> cls, final String name, final Class<?>... params) {
        if (cls == null) {
            return null;
        }
        try {
            final Method m = cls.getDeclaredMethod(name, params);
            m.setAccessible(true);
            return m;
        } catch (NoSuchMethodException e) {
            try {
                final Method m = cls.getMethod(name, params);
                m.setAccessible(true);
                return m;
            } catch (NoSuchMethodException e1) {
                if (cls.getSuperclass() != null) {
                    return getMethod(cls.getSuperclass(), name, params);
                }
            }
        }
        return null;
    }
    public static Method getMethodExact(final Class<?> cls, final String name, Class<?> returning, Class<?>... params) {
        if (cls == null) {
            return null;
        }
        for (final Method m : cls.getDeclaredMethods()) {
            if (m.getName().equals(name)
                    && Arrays.equals(m.getParameterTypes(), params) &&
                    (returning == null || m.getReturnType().equals(returning))) {
                m.setAccessible(true);
                return m;
            }
        }
        if (cls.getSuperclass() != null) {
            return getMethodExact(cls.getSuperclass(), name, null, params);
        }
        return null;
    }
    public static Method getMethod(final Class<?> cls, final String name, final int index) {
        if (cls == null) {
            return null;
        }
        int currentIndex = 0;
        for (final Method m : cls.getDeclaredMethods()) {
            if (m.getName().equals(name) && index == currentIndex++) {
                m.setAccessible(true);
                return m;
            }
        }
        if (cls.getSuperclass() != null) {
            return getMethod(cls.getSuperclass(), name, index);
        }
        return null;
    }
    public static Method getMethod(final Class<?> cls, final Class<?> returning, final int index) {
        if (cls == null) {
            return null;
        }
        int currentIndex = 0;
        for (final Method m : cls.getDeclaredMethods()) {
            if ((returning == null || m.getReturnType().equals(returning)) && index == currentIndex++) {
                m.setAccessible(true);
                return m;
            }
        }
        if (cls.getSuperclass() != null) {
            return getMethod(cls.getSuperclass(), returning, index);
        }
        return null;
    }

    public static Entity getBukkitEntity(Object nmsEntity) {
        Object craftEntity = null;
        try {
            craftEntity = GET_BUKKIT_ENTITY_METHOD.invoke(nmsEntity);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return (Entity) craftEntity;
    }


    private static Entity getEntityByIdWithWorldUnsafe(World world, int id) {
        if(world == null) return null;

        Entity e = ENTITY_ID_CACHE.getOrDefault(id, null);
        if(e != null) return e;

        try {
            Object worldServer = GET_CRAFT_WORLD_HANDLE_METHOD.invoke(world);
            Object nmsEntity;

            ReflectionObject reflectWorldServer = new ReflectionObject(worldServer);
            Object levelEntityGetter;
            if (PAPER_ENTITY_LOOKUP_EXISTS) {
                levelEntityGetter = reflectWorldServer.readObject(0, PAPER_ENTITY_LOOKUP_CLASS);
            } else {
                Object entitySectionManager = reflectWorldServer.readObject(0, PERSISTENT_ENTITY_SECTION_MANAGER_CLASS);
                ReflectionObject reflectEntitySectionManager = new ReflectionObject(entitySectionManager);
                levelEntityGetter = reflectEntitySectionManager.readObject(0, LEVEL_ENTITY_GETTER_CLASS);
            }
            nmsEntity = GET_ENTITY_BY_ID_LEVEL_ENTITY_GETTER_METHOD.invoke(levelEntityGetter, id);

            if(nmsEntity == null) return null;

            e = getBukkitEntity(nmsEntity);
            ENTITY_ID_CACHE.put(id, e);
            return e;
        } catch (IllegalAccessException | InvocationTargetException ex) {
            ex.printStackTrace();
        }
        return null;
    }
    @Nullable
    public static Entity getEntityById(@Nullable World origin, int id) {
        if (origin != null) {
            Entity e = getEntityByIdWithWorldUnsafe(origin, id);
            if (e != null) {
                return e;
            }
        }
        //They specified the wrong world
        for (World world : Bukkit.getWorlds()) {
            Entity entity = getEntityByIdWithWorldUnsafe(world, id);
            if (entity != null) {
                return entity;
            }
        }
        return null;
    }
    @Nullable
    public static Entity getEntityById(int entityID) {
        return getEntityById(null, entityID);
    }


    public static class ReflectionObject {
        private static final Map<Class<?>, Map<Class<?>, Field[]>> FIELD_CACHE = new ConcurrentHashMap<>();
        private static final Field[] EMPTY_FIELD_ARRAY = new Field[0];
        protected final Object object;
        private final Class<?> clazz;

        public ReflectionObject(Object object) {
            this.object = object;
            this.clazz = object.getClass();
        }

        public <T> T readObject(int index, Class<? extends T> type) {
            try {
                Field field = getField(type, index);
                return (T) field.get(object);
            } catch (IllegalAccessException | NullPointerException | ArrayIndexOutOfBoundsException e) {
                throw new IllegalStateException("PacketEvents failed to find a " + type.getSimpleName() + " indexed " + index + " by its type in the " + clazz.getName() + " class!");
            }
        }

        private Field getField(Class<?> type, int index) {
            Map<Class<?>, Field[]> cached = FIELD_CACHE.computeIfAbsent(clazz, k -> new ConcurrentHashMap<>());
            Field[] fields = cached.computeIfAbsent(type, typeClass -> getFields(typeClass, clazz.getDeclaredFields()));
            if (fields.length >= index + 1) {
                return fields[index];
            } else {
                throw new IllegalStateException("PacketEvents failed to find a " + type.getSimpleName() + " indexed " + index + " by its type in the " + clazz.getName() + " class!");
            }
        }
        private Field[] getFields(Class<?> type, Field[] fields) {
            List<Field> ret = new ArrayList<>();
            for(Field field : fields) {
                if(!field.getType().equals(type)) continue;
                field.setAccessible(true);
                ret.add(field);
            }
            return ret.toArray(EMPTY_FIELD_ARRAY);
        }
    }

}
