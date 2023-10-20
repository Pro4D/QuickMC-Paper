package com.pro4d.quickmc.damage;

import lombok.Getter;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.EntityEquipment;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DamageManager implements Listener {

    public static ArmorTypes HELMET, CHESTPLATE, LEGGINGS, BOOTS;
    public static int HELMET_PROT, CHESTPLATE_PROT, LEGGINGS_PROT, BOOTS_PROT;

    @Getter private static Map<UUID, Double> customDamage;
    public DamageManager() {
        customDamage = new HashMap<>();
    }

    public static double totalMetaDefensePoints() {
        return HELMET.getDefensePoints() + CHESTPLATE.getDefensePoints() + LEGGINGS.getDefensePoints() + BOOTS.getDefensePoints();
    }
    public static double totalMetaProtReduction() {
        double totalProtLvl = HELMET_PROT + CHESTPLATE_PROT + LEGGINGS_PROT + BOOTS_PROT;
        return (totalProtLvl * 4) / 100;
    }

    public static void damageEntity(LivingEntity target, Entity source, double hearts) {
        getCustomDamage().put(target.getUniqueId(), hearts * 2);
        if(source == null) {
            target.damage(hearts * 2);

        } else target.damage(getRelativeToMeta(target, hearts), source);
    }

    private static double trueDamageFromSource(double hearts) {
        double passing = 1 - (totalMetaDefensePoints() / 25);
        double reduced = passing * totalMetaProtReduction();

        return (reduced + (totalMetaDefensePoints() / 25)) + (hearts * 2);
    }

    private static double difference(LivingEntity entity) {
        EntityEquipment equipment = entity.getEquipment();
        double dp = 0;
        if(equipment != null) {
            dp += ArmorTypes.getByItem(equipment.getHelmet()).getDefensePoints();
            dp += ArmorTypes.getByItem(equipment.getChestplate()).getDefensePoints();
            dp += ArmorTypes.getByItem(equipment.getLeggings()).getDefensePoints();
            dp += ArmorTypes.getByItem(equipment.getBoots()).getDefensePoints();
        }

        return totalMetaDefensePoints() - dp;
    }


    public static void modifyDamage(Player player, double target, EntityDamageEvent event) {
        event.setDamage(EntityDamageEvent.DamageModifier.BASE, getRelativeToMeta(player, target));
        event.setDamage(EntityDamageEvent.DamageModifier.ARMOR, 0);
        event.setDamage(EntityDamageEvent.DamageModifier.MAGIC, 0);
    }

    private static double getRelativeToMeta(LivingEntity entity, double hearts) {
        double passing = (difference(entity) / 25);
        double reduced = passing * totalMetaProtReduction();

        return (reduced + passing) + (hearts * 2);
    }

    @EventHandler
    private void reduce(EntityDamageEvent event) {
        if(!(event.getEntity() instanceof Player player)) return;
        UUID uuid = player.getUniqueId();
        if(!getCustomDamage().containsKey(uuid)) return;
//        event.setDamage(EntityDamageEvent.DamageModifier.BASE, getRelativeToMeta(player, getCustomDamage().get(uuid)));

        event.setDamage(EntityDamageEvent.DamageModifier.ARMOR, 0);
        event.setDamage(EntityDamageEvent.DamageModifier.MAGIC, 0);

        getCustomDamage().remove(uuid);
    }

}
