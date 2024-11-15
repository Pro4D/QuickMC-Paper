package com.pro4d.quickmc.damage;

import com.destroystokyo.paper.event.entity.EntityKnockbackByEntityEvent;
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
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

@SuppressWarnings("removal")
public class DamageManager implements Listener {

    // TODO gamemode check for spectator too

    public static ExpressionTransformer PROTECTION, PROJECTILE_PROTECTION, BLAST_PROTECTION, FIRE_PROTECTION,
            RESISTANCE_EFFECT,
            SHIELD_BLOCK, RESPECT_I_FRAMES;

    public static ArmorTypes HELMET, CHESTPLATE, LEGGINGS, BOOTS;
    public static int HELMET_PROT, CHESTPLATE_PROT, LEGGINGS_PROT, BOOTS_PROT;

//    @Getter private static Map<UUID, Double> customDamage;

    @Getter private static Set<CustomDamageEvent> customDamage;
    public DamageManager() {
//        customDamage = new HashMap<>();
        customDamage = new HashSet<>();

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

        RESISTANCE_EFFECT = new ExpressionTransformer(1, (((target, source, damage) -> {
            PotionEffect resistance = target.getPotionEffect(PotionEffectType.DAMAGE_RESISTANCE);
            if(resistance != null) {
                int level = resistance.getAmplifier() + 1;
                double partial = damage * (.2 * level);
                damage = Math.max(damage - partial, 0);
            }

            return damage;
        })));

        // TODO change how shield blocking is implemented. maybe in the DamageEvent? maybe call "return" on method entirely?
        SHIELD_BLOCK = new ExpressionTransformer(2, (((target, source, damage) -> {
            if(target instanceof Player player && player.isBlocking()) damage = 0;
            return damage;
        })));

        RESPECT_I_FRAMES = new ExpressionTransformer(3, (((target, source, damage) -> {
            if(target.getNoDamageTicks() != 0) damage = 0;
            return damage;
        })));
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
    public static boolean damageEntity(LivingEntity target, Entity source, double hearts, String causeName, List<ExpressionTransformer> transformers, int iFrames, boolean applyKnockback) {
        if(target.isDead()) return false;
        if(target instanceof Player player && player.getGameMode() == GameMode.CREATIVE) return false;

        CustomDamageEvent event = new CustomDamageEvent(target, source, hearts, causeName, transformers, iFrames, applyKnockback, true);
        QuickMC.getSourcePlugin().getServer().getPluginManager().callEvent(event);
        if(event.isCancelled()) return false;

        target = event.getTarget();
        source = event.getSource();
        hearts = event.getDamage();
        transformers = event.getTransformersList();
        iFrames = event.getIFrames();

        AtomicDouble wrapped = new AtomicDouble(getRelativeToMeta(target, hearts));
        new DamageTransformer(wrapped, PROTECTION).apply(target, source);

        transformers = new ArrayList<>(transformers);
        if(!transformers.isEmpty()) transformers.sort(Comparator.comparingDouble(ExpressionTransformer::getPriority));

        if(transformers.contains(RESPECT_I_FRAMES) && target.getNoDamageTicks() > 0) return false;

        for(ExpressionTransformer expression : transformers) {
            new DamageTransformer(wrapped, expression).apply(target, source);
        }

        double finalDmg = wrapped.get();
        event.setFinalDmg(finalDmg);

        getCustomDamage().add(event);
        target.setNoDamageTicks(0);
        target.damage(Math.min(finalDmg, target.getHealth()), source);
        target.setNoDamageTicks(iFrames);

        LivingEntity finalTarget = target;
        QuickUtils.sync(() -> finalTarget.setLastDamageCause(event));
        return true;
    }
    public static boolean damageEntity(LivingEntity target, Entity source, double hearts, String causeName, List<ExpressionTransformer> transformers)  {
        return damageEntity(target, source, hearts, causeName, transformers, 20, true);
    }
    public static boolean damageEntity(LivingEntity target, Entity source, double hearts, String causeName) {
        return damageEntity(target, source, hearts, causeName, new ArrayList<>(), 20, true);
    }


    /**
     * Deal the inputted number of damage points of damage to an entity,
     * while factoring in armor.
     * Provide a source of the damage is optional.
     **/
    public static boolean applyDamagePoints(LivingEntity target, Entity source, double dmg, String causeName, List<ExpressionTransformer> transformers, int iFrames, boolean applyKnockback) {
        if(target.isDead()) return false;
        if(target instanceof Player player && player.getGameMode() == GameMode.CREATIVE) return false;

        // TODO isHearts ?
        CustomDamageEvent event = new CustomDamageEvent(target, source, dmg / 2, causeName, transformers, iFrames, applyKnockback, true);
        QuickMC.getSourcePlugin().getServer().getPluginManager().callEvent(event);
        if(event.isCancelled()) return false;

        target = event.getTarget();
        source = event.getSource();
        dmg = event.getDamage();
        transformers = event.getTransformersList();
        iFrames = event.getIFrames();

        AtomicDouble wrapped = new AtomicDouble(getRelativeToMeta(target, dmg));
        new DamageTransformer(wrapped, PROTECTION).apply(target, source);

        transformers = new ArrayList<>(transformers);
        if(!transformers.isEmpty()) transformers.sort(Comparator.comparingDouble(ExpressionTransformer::getPriority));

        if(transformers.contains(RESPECT_I_FRAMES) && target.getNoDamageTicks() > 0) return false;

        for(ExpressionTransformer expression : transformers) {
            new DamageTransformer(wrapped, expression).apply(target, source);
        }

        double finalDmg = wrapped.get();
        event.setFinalDmg(finalDmg);

        getCustomDamage().add(event);
        target.setNoDamageTicks(0);
        target.damage(finalDmg, source);
        target.setNoDamageTicks(iFrames);

        LivingEntity finalTarget = target;
        QuickUtils.sync(() -> finalTarget.setLastDamageCause(event));
        return true;
    }
    public static boolean applyDamagePoints(LivingEntity target, Entity source, double dmg, String causeName, List<ExpressionTransformer> transformers) {
        return applyDamagePoints(target, source, dmg, causeName, transformers, 20, true);
    }
    public static boolean applyDamagePoints(LivingEntity target, Entity source, double dmg, String causeName) {
        return applyDamagePoints(target, source, dmg, causeName, new ArrayList<>(), 20, true);
    }

    /**
     * Deal the inputted number of damage points of damage to an entity,
     * while factoring in armor.
     * Provide a source of the damage is optional.
     **/
    public static boolean dealDamagePoints(LivingEntity target, Entity source, double dmg, String causeName, List<ExpressionTransformer> transformers, int iFrames, boolean applyKnockback) {
        if(target.isDead()) return false;
        if(target instanceof Player player && player.getGameMode() == GameMode.CREATIVE) return false;

        CustomDamageEvent event = new CustomDamageEvent(target, source, dmg / 2, causeName, transformers, iFrames, applyKnockback, false);
        QuickMC.getSourcePlugin().getServer().getPluginManager().callEvent(event);
        if(event.isCancelled()) return false;

        target = event.getTarget();
        source = event.getSource();
        dmg = event.getDamage();
        transformers = event.getTransformersList();
        iFrames = event.getIFrames();

        AtomicDouble wrapped = new AtomicDouble(getRelativeToMeta(target, dmg));
        new DamageTransformer(wrapped, PROTECTION).apply(target, source);

        transformers = new ArrayList<>(transformers);
        if(!transformers.isEmpty()) transformers.sort(Comparator.comparingDouble(ExpressionTransformer::getPriority));

        if(transformers.contains(RESPECT_I_FRAMES) && target.getNoDamageTicks() > 0) return false;

        for(ExpressionTransformer expression : transformers) {
            new DamageTransformer(wrapped, expression).apply(target, source);
        }

        double finalDmg = wrapped.get();
        event.setFinalDmg(finalDmg);

        getCustomDamage().add(event);
        target.setNoDamageTicks(0);
        target.damage(finalDmg, source);
        target.setNoDamageTicks(iFrames);

        LivingEntity finalTarget = target;
        QuickUtils.sync(() -> finalTarget.setLastDamageCause(event));
        return true;
    }
    public static boolean dealDamagePoints(LivingEntity target, Entity source, double dmg, String causeName, List<ExpressionTransformer> transformers) {
        return dealDamagePoints(target, source, dmg, causeName, transformers, 20, true);
    }
    public static boolean dealDamagePoints(LivingEntity target, Entity source, double dmg, String causeName) {
        return dealDamagePoints(target, source, dmg, causeName, new ArrayList<>(), 20, true);
    }


    /**
     * Set the final damage of a damage event. Value in hearts
     **/
    public static double modifyDamage(LivingEntity target, Entity source, double hearts, EntityDamageEvent event, String causeName, List<ExpressionTransformer> transformers, int iFrames, boolean applyKnockback) {
        CustomDamageEvent dmgEvent = new CustomDamageEvent(target, source, hearts, causeName, transformers, iFrames, applyKnockback, true);
        QuickMC.getSourcePlugin().getServer().getPluginManager().callEvent(dmgEvent);
        if(dmgEvent.isCancelled()) return -1;

        target = dmgEvent.getTarget();
        source = dmgEvent.getSource();
        hearts = dmgEvent.getDamage();
        transformers = dmgEvent.getTransformersList();

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

        return event.getFinalDamage();
    }
    public static double modifyDamage(LivingEntity target, Entity source, double hearts, EntityDamageEvent event, String causeName, List<ExpressionTransformer> transformers) {
        return modifyDamage(target, source, hearts, event, causeName, transformers, 20, true);
    }
    public static double modifyDamage(LivingEntity target, Entity source, double hearts, EntityDamageEvent event, String causeName) {
        return modifyDamage(target, source, hearts, event, causeName, new ArrayList<>(), 20, true);
    }

    /**
     * Set the final damage of a damage event. Value in damage points
     **/
    public static double modifyDamagePoints(LivingEntity target, Entity source, double dmg, EntityDamageEvent event, String causeName, List<ExpressionTransformer> transformers, int iFrames, boolean applyKnockback) {
        // TODO isHearts ?
        CustomDamageEvent dmgEvent = new CustomDamageEvent(target, source, dmg / 2, causeName, transformers, iFrames, applyKnockback, true);
        QuickMC.getSourcePlugin().getServer().getPluginManager().callEvent(dmgEvent);
        if(dmgEvent.isCancelled()) return -1;

        target = dmgEvent.getTarget();
        source = dmgEvent.getSource();
        dmg = dmgEvent.getDamage();
        transformers = dmgEvent.getTransformersList();

        AtomicDouble wrapped = new AtomicDouble(getRelativeToMeta(target, dmg));
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

        return event.getFinalDamage();
    }
    public static double modifyDamagePoints(LivingEntity target, Entity source, double dmg, EntityDamageEvent event, String causeName, List<ExpressionTransformer> transformers) {
        return modifyDamagePoints(target, source, dmg, event, causeName, transformers, 20, true);
    }
    public static double modifyDamagePoints(LivingEntity target, Entity source, double dmg, EntityDamageEvent event, String causeName) {
        return modifyDamagePoints(target, source, dmg, event, causeName, new ArrayList<>(), 20, true);
    }

    public static void setFinalDamage(EntityDamageEvent event, double dmg) {}

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
        return totalMetaDefensePoints() - AttributeManager.getBukkitAttributeValue(entity, Attribute.GENERIC_ARMOR);
    }
    public static double protectionDiffFromMeta(LivingEntity entity) {
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
    public static double trueDamageFromSource(double hearts) {
        // TODO update
        double passing = 1 - (totalMetaDefensePoints() / 25);
        double reduced = passing * totalMetaProtReduction();

        return (reduced + (totalMetaDefensePoints() / 25)) + (hearts * 2);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    private void reduce(EntityDamageEvent event) {
        if(!(event.getEntity() instanceof LivingEntity entity)) return;
        UUID uuid = entity.getUniqueId();
        CustomDamageEvent customDmg = getTakenCustomDamage(uuid);
        if(customDmg == null) return;

        if(customDmg.isHearts()) {
            int damage = (int) event.getOriginalDamage(EntityDamageEvent.DamageModifier.BASE);
            int absorption = (int) entity.getAbsorptionAmount();

//            event.setDamage(EntityDamageEvent.DamageModifier.BASE, Math.max(0, damage - absorption));

            int damageAbsorbed = -Math.min(damage, absorption);
            event.setDamage(EntityDamageEvent.DamageModifier.ABSORPTION, damageAbsorbed);

            event.setDamage(EntityDamageEvent.DamageModifier.ARMOR, 0);
            event.setDamage(EntityDamageEvent.DamageModifier.MAGIC, 0);
            if(entity instanceof Player) event.setDamage(EntityDamageEvent.DamageModifier.BLOCKING, 0);
        }

        if(customDmg.isApplyKnockback()) {
            getCustomDamage().remove(customDmg);
        } else QuickUtils.sync(() -> getCustomDamage().remove(customDmg));
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    private void noKnockback(EntityKnockbackByEntityEvent event) {
        CustomDamageEvent customDmg = getTakenCustomDamage(event.getEntity().getUniqueId());
        if(customDmg != null && !customDmg.isApplyKnockback()) event.setCancelled(true);
        getCustomDamage().remove(customDmg);
    }

    public static CustomDamageEvent getTakenCustomDamage(UUID uuid) {
        for(CustomDamageEvent event : getCustomDamage()) {
            if(event.getTarget().getUniqueId().equals(uuid)) return event;
        }
        return null;
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
