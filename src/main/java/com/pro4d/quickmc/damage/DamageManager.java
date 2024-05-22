package com.pro4d.quickmc.damage;

import com.google.common.util.concurrent.AtomicDouble;
import com.pro4d.quickmc.QuickMC;
import com.pro4d.quickmc.QuickUtils;
import com.pro4d.quickmc.attributes.AttributeManager;
import com.pro4d.quickmc.events.CustomDamageEvent;
import lombok.Getter;
import org.bukkit.GameMode;
import org.bukkit.attribute.Attribute;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class DamageManager implements Listener {

    public static ExpressionTransformer PROTECTION, PROJECTILE_PROTECTION, BLAST_PROTECTION, FIRE_PROTECTION;

    public static ArmorTypes HELMET, CHESTPLATE, LEGGINGS, BOOTS;
    public static int HELMET_PROT, CHESTPLATE_PROT, LEGGINGS_PROT, BOOTS_PROT;

//    @Getter private static Map<UUID, Double> customDamage;

    @Getter private static Set<UUID> hasCustomDamage;
    public DamageManager() {
//        customDamage = new HashMap<>();
        hasCustomDamage = new HashSet<>();

        HELMET = ArmorTypes.NAKED;
        HELMET_PROT = 0;

        CHESTPLATE = ArmorTypes.NAKED;
        CHESTPLATE_PROT = 0;

        LEGGINGS = ArmorTypes.NAKED;
        LEGGINGS_PROT = 0;

        BOOTS = ArmorTypes.NAKED;
        BOOTS_PROT = 0;

        createDamageReductionTransformers();
    }

    public static int getDefensePoints(EnchantmentTarget armor, ArmorTypes type) {
        if(type == ArmorTypes.NAKED) return 0;

        return switch(armor) {
            case ARMOR_HEAD -> switch (type) {
                case TURTLE, CHAINMAIL, GOLD, IRON -> 2;
                case LEATHER -> 1;
                case DIAMOND, NETHERITE -> 3;
                default -> 0;
            };
            case ARMOR_TORSO -> switch (type) {
                case LEATHER -> 3;
                case GOLD, CHAINMAIL -> 5;
                case IRON -> 6;
                case DIAMOND, NETHERITE -> 8;
                default -> 0;
            };
            case ARMOR_LEGS -> switch (type) {
                case LEATHER -> 2;
                case GOLD -> 3;
                case CHAINMAIL -> 4;
                case IRON -> 5;
                case DIAMOND, NETHERITE -> 6;
                default -> 0;
            };
            case ARMOR_FEET -> switch (type) {
                case LEATHER, CHAINMAIL, GOLD -> 1;
                case IRON -> 2;
                case DIAMOND, NETHERITE -> 3;
                default -> 0;
            };
            default -> 0;
        };

    }

    private static void createDamageReductionTransformers() {
        PROTECTION = new ExpressionTransformer(0, (target, source, damage) -> {
            double scalingFactor = 1.0 + protectionDiffFromMeta(target);
            double scaled = damage * scalingFactor;
            return damage + scaled;
        });

        PROJECTILE_PROTECTION = new ExpressionTransformer(0, ((target, source, damage) -> {
            EntityEquipment equipment = target.getEquipment();
            double protLvl = 0;
            if(equipment != null) {
                ItemStack helmet = equipment.getHelmet();
                if(helmet != null) protLvl += helmet.getEnchantmentLevel(Enchantment.PROTECTION_PROJECTILE) * .08;

                ItemStack chestplate = equipment.getChestplate();
                if(chestplate != null) protLvl += chestplate.getEnchantmentLevel(Enchantment.PROTECTION_PROJECTILE) * .08;

                ItemStack leggings = equipment.getLeggings();
                if(leggings != null) protLvl += leggings.getEnchantmentLevel(Enchantment.PROTECTION_PROJECTILE) * .08;

                ItemStack boots = equipment.getBoots();
                if(boots != null) protLvl += boots.getEnchantmentLevel(Enchantment.PROTECTION_PROJECTILE) * .08;
            }

            double scalingFactor = Math.min(protLvl, .8);
            return damage - (damage * scalingFactor);
        }));

        BLAST_PROTECTION = new ExpressionTransformer(0, ((target, source, damage) -> {
            EntityEquipment equipment = target.getEquipment();
            double protLvl = 0;
            if(equipment != null) {
                ItemStack helmet = equipment.getHelmet();
                if(helmet != null) protLvl += helmet.getEnchantmentLevel(Enchantment.PROTECTION_EXPLOSIONS) * .08;

                ItemStack chestplate = equipment.getChestplate();
                if(chestplate != null) protLvl += chestplate.getEnchantmentLevel(Enchantment.PROTECTION_EXPLOSIONS) * .08;

                ItemStack leggings = equipment.getLeggings();
                if(leggings != null) protLvl += leggings.getEnchantmentLevel(Enchantment.PROTECTION_EXPLOSIONS) * .08;

                ItemStack boots = equipment.getBoots();
                if(boots != null) protLvl += boots.getEnchantmentLevel(Enchantment.PROTECTION_EXPLOSIONS) * .08;
            }

            double scalingFactor = Math.min(protLvl, .8);
            return damage - (damage * scalingFactor);
        }));

        FIRE_PROTECTION = new ExpressionTransformer(0, ((target, source, damage) -> {
            EntityEquipment equipment = target.getEquipment();
            double protLvl = 0;
            if(equipment != null) {
                ItemStack helmet = equipment.getHelmet();
                if(helmet != null) protLvl += helmet.getEnchantmentLevel(Enchantment.PROTECTION_FIRE) * .08;

                ItemStack chestplate = equipment.getChestplate();
                if(chestplate != null) protLvl += chestplate.getEnchantmentLevel(Enchantment.PROTECTION_FIRE) * .08;

                ItemStack leggings = equipment.getLeggings();
                if(leggings != null) protLvl += leggings.getEnchantmentLevel(Enchantment.PROTECTION_FIRE) * .08;

                ItemStack boots = equipment.getBoots();
                if(boots != null) protLvl += boots.getEnchantmentLevel(Enchantment.PROTECTION_FIRE) * .08;
            }

            double scalingFactor = Math.min(protLvl, .8);
            return damage - (damage * scalingFactor);
        }));
    }

    public static double totalMetaDefensePoints() {
        return getDefensePoints(EnchantmentTarget.ARMOR_HEAD, HELMET) + getDefensePoints(EnchantmentTarget.ARMOR_TORSO, CHESTPLATE)
                + getDefensePoints(EnchantmentTarget.ARMOR_LEGS, LEGGINGS) + getDefensePoints(EnchantmentTarget.ARMOR_FEET, BOOTS);
    }
    public static double totalMetaProtReduction() {
        double totalProtLvl = HELMET_PROT + CHESTPLATE_PROT + LEGGINGS_PROT + BOOTS_PROT;
        return (totalProtLvl * 4) / 100;
    }

    /**
     * Deal the inputted number of hearts of damage to an entity,
     * while factoring in armor.
     * Provide a source of the damage is optional.
    **/
    public static boolean damageEntity(LivingEntity target, Entity source, double hearts, String causeName, List<ExpressionTransformer> transformers) {
        if(target.isDead() || target.getNoDamageTicks() != 0) return false;
        if(target instanceof Player player && player.getGameMode() == GameMode.CREATIVE) return false;

        CustomDamageEvent event = new CustomDamageEvent(target, source, hearts, causeName, transformers);
        QuickMC.getSourcePlugin().getServer().getPluginManager().callEvent(event);
        if(event.isCancelled()) return false;

        target = event.getTarget();
        source = event.getSource();
        hearts = event.getDamage();
        transformers = event.getTransformersList();

        AtomicDouble wrapped = new AtomicDouble(getRelativeToMeta(target, hearts));
        new DamageTransformer(wrapped, PROTECTION).apply(target, source);

        transformers = new ArrayList<>(transformers);
        if(!transformers.isEmpty()) transformers.sort(Comparator.comparingDouble(ExpressionTransformer::getPriority));

        for(ExpressionTransformer expression : transformers) {
            new DamageTransformer(wrapped, expression).apply(target, source);
        }

        double finalDmg = wrapped.get();
        event.setFinalDmg(finalDmg);

        getHasCustomDamage().add(target.getUniqueId());
        target.damage(finalDmg, source);

        LivingEntity finalTarget = target;
        QuickUtils.sync(() -> finalTarget.setLastDamageCause(event));
        return true;
    }

    public static boolean damageEntity(LivingEntity target, Entity source, double hearts, String causeName) {
        return damageEntity(target, source, hearts, causeName, new ArrayList<>());
    }


    /**
     * Set the final damage of a damage event. Value in hearts
     **/
    public static void modifyDamage(LivingEntity target, Entity source, double hearts, EntityDamageEvent event, String causeName, List<ExpressionTransformer> transformers) {
        CustomDamageEvent dmgEvent = new CustomDamageEvent(target, source, hearts, causeName, transformers);
        QuickMC.getSourcePlugin().getServer().getPluginManager().callEvent(dmgEvent);
        if(dmgEvent.isCancelled()) return;

        target = dmgEvent.getTarget();
        source = dmgEvent.getSource();
        hearts = dmgEvent.getDamage();
        transformers = dmgEvent.getTransformersList();

//        double dmg = getRelativeToMeta(target, hearts);
//        double scalingFactor = 1.0 + protectionDiffFromMeta(target);
//        dmg = dmg + (dmg * scalingFactor);

        AtomicDouble wrapped = new AtomicDouble(getRelativeToMeta(target, hearts));
        new DamageTransformer(wrapped, PROTECTION).apply(target, source);

        transformers = new ArrayList<>(transformers);
        if(!transformers.isEmpty()) transformers.sort(Comparator.comparingDouble(ExpressionTransformer::getPriority));

        for(ExpressionTransformer expression : transformers) {
            new DamageTransformer(wrapped, expression).apply(target, source);
        }

        // TODO make this more open-ended (aka return value instead of hard modifying event) ?

//        event.setDamage(EntityDamageEvent.DamageModifier.BASE, dmg);
        event.setDamage(EntityDamageEvent.DamageModifier.BASE, wrapped.get());
        event.setDamage(EntityDamageEvent.DamageModifier.ARMOR, 0);
        event.setDamage(EntityDamageEvent.DamageModifier.MAGIC, 0);
    }


    /**
     * Retrieve the difference between total damage reduction
     * of the player's armor compared to the 'META' armor.
    **/
    public static double getRelativeToMeta(LivingEntity entity, double hp) {
        double defensePointsDiff = defensePointsDiffFromMeta(entity);
        double scalingFactor = 1.0 + (defensePointsDiff / totalMetaDefensePoints());
        return hp * scalingFactor;
//        double passing = (difference(entity) / 25);
//        double reduced = passing * totalMetaProtReduction();
//        return (reduced + passing) + (hp);
    }


    private static double defensePointsDiffFromMeta(LivingEntity entity) {
        return totalMetaDefensePoints() - AttributeManager.getAttributeValue(entity, Attribute.GENERIC_ARMOR);
    }
    private static double protectionDiffFromMeta(LivingEntity entity) {
        EntityEquipment equipment = entity.getEquipment();
        double protLvl = 0;
        if(equipment != null) {
            ItemStack helmet = equipment.getHelmet();
            if(helmet != null) protLvl += helmet.getEnchantmentLevel(Enchantment.PROTECTION_ENVIRONMENTAL) * .04;

            ItemStack chestplate = equipment.getChestplate();
            if(chestplate != null) protLvl += chestplate.getEnchantmentLevel(Enchantment.PROTECTION_ENVIRONMENTAL) * .04;

            ItemStack leggings = equipment.getLeggings();
            if(leggings != null) protLvl += leggings.getEnchantmentLevel(Enchantment.PROTECTION_ENVIRONMENTAL) * .04;

            ItemStack boots = equipment.getBoots();
            if(boots != null) protLvl += boots.getEnchantmentLevel(Enchantment.PROTECTION_ENVIRONMENTAL) * .04;
        }
        return totalMetaProtReduction() - protLvl;
    }

    /**
     * Retrieve the amount of raw damage needed to deal the requested number of hearts
     **/
    private static double trueDamageFromSource(double hearts) {
        // TODO update
        double passing = 1 - (totalMetaDefensePoints() / 25);
        double reduced = passing * totalMetaProtReduction();

        return (reduced + (totalMetaDefensePoints() / 25)) + (hearts * 2);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private void reduce(EntityDamageEvent event) {
        if(!(event.getEntity() instanceof Player player)) return;
        UUID uuid = player.getUniqueId();
        if(!getHasCustomDamage().contains(uuid)) return;

        event.setDamage(EntityDamageEvent.DamageModifier.ARMOR, 0);
        event.setDamage(EntityDamageEvent.DamageModifier.MAGIC, 0);

        getHasCustomDamage().remove(uuid);
    }


    static class DamageTransformer {

        private final AtomicDouble input;
        @Getter private final ExpressionTransformer transformer;
        public DamageTransformer(AtomicDouble input, ExpressionTransformer transformer) {
            this.input = input;
            this.transformer = transformer;
        }

        public void apply(LivingEntity target, Entity source) {
            double dmg = input.get();
            dmg = transformer.getHandler().accept(target, source, dmg);
            input.set(dmg);
        }

        @FunctionalInterface
        public interface ExpressionHandler {
            double accept(LivingEntity target, Entity source, double damage);
        }
    }

}
