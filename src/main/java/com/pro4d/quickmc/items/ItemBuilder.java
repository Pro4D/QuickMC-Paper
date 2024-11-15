package com.pro4d.quickmc.items;

//import com.cryptomorin.xseries.SkullUtils;
import com.cryptomorin.xseries.profiles.builder.XSkull;
import com.cryptomorin.xseries.profiles.objects.ProfileInputType;
import com.cryptomorin.xseries.profiles.objects.Profileable;
import com.pro4d.quickmc.QuickUtils;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;
import java.util.function.Consumer;

/**
 * Simple {@link ItemStack} builder
 *
 * Original Author - Aglerr & MrMicky
 * Modified By - Pro4D
 */
public class ItemBuilder {

    private final ItemStack item;
    private final ItemMeta meta;

    public ItemBuilder(Material material) {
        this(new ItemStack(material));
    }

    public ItemBuilder(ItemStack item) {
        this.item = Objects.requireNonNull(item, "item");
        this.meta = item.getItemMeta();

        if(this.meta == null) throw new IllegalArgumentException("The type " + item.getType() + " doesn't support item meta");
    }

    public ItemBuilder type(Material material) {
        this.item.setType(material);
        return this;
    }

    public ItemBuilder data(int data) {
        return durability((short) data);
    }

    public ItemBuilder durability(short durability) {
        if(!(this.meta instanceof Damageable damageable)) return this;
        int current = item.getType().getMaxDurability() - damageable.getDamage();
        damageable.setDamage(current - durability);
        return this;
    }

    public ItemBuilder amount(int amount) {
        this.item.setAmount(amount);
        return this;
    }

    public ItemBuilder enchant(Enchantment enchantment) {
        return enchant(enchantment, 1);
    }

    public ItemBuilder enchant(Enchantment enchantment, int level) {
        this.meta.addEnchant(enchantment, level, true);
        return this;
    }

    public ItemBuilder removeEnchant(Enchantment enchantment) {
        this.meta.removeEnchant(enchantment);
        return this;
    }

    public ItemBuilder removeEnchants() {
        this.meta.getEnchants().keySet().forEach(this.meta::removeEnchant);
        return this;
    }

    public ItemBuilder meta(Consumer<ItemMeta> metaConsumer) {
        metaConsumer.accept(this.meta);
        return this;
    }

    public <T extends ItemMeta> ItemBuilder meta(Class<T> metaClass, Consumer<T> metaConsumer) {
        if (metaClass.isInstance(this.meta)) {
            metaConsumer.accept(metaClass.cast(this.meta));
        }
        return this;
    }

    public ItemBuilder name(String name) {
        this.meta.setDisplayName(QuickUtils.color(name));
        return this;
    }

    public ItemBuilder lore(String lore) {
        return lore(Collections.singletonList(lore));
    }

    public ItemBuilder lore(String... lore) {
        return lore(Arrays.asList(lore));
    }

    public ItemBuilder lore(List<String> lore) {
        this.meta.setLore(QuickUtils.colorList(lore));
        return this;
    }

    public ItemBuilder addLore(String line) {
        List<String> lore = this.meta.getLore();

        if (lore == null) {
            return lore(line);
        }

        lore.add(line);
        return lore(lore);
    }

    public ItemBuilder addLore(String... lines) {
        return addLore(Arrays.asList(lines));
    }

    public ItemBuilder addLore(List<String> lines) {
        List<String> lore = this.meta.getLore();
        if(lore == null) return lore(lines);

        lore.addAll(lines);
        return lore(lore);
    }

    public ItemBuilder flags(ItemFlag... flags) {
        this.meta.addItemFlags(flags);
        return this;
    }

    public ItemBuilder flags() {
        return flags(ItemFlag.values());
    }

    public ItemBuilder removeFlags(ItemFlag... flags) {
        this.meta.removeItemFlags(flags);
        return this;
    }

    public ItemBuilder removeFlags() {
        return removeFlags(ItemFlag.values());
    }

    public ItemBuilder armorColor(Color color) {
        return meta(LeatherArmorMeta.class, m -> m.setColor(color));
    }

    public ItemBuilder customModelData(int data){
        this.meta.setCustomModelData(data);
        return this;
    }

    public ItemBuilder pdc(NamespacedKey key, String s){
        this.meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, s);
        return this;
    }

    public ItemBuilder pdc(NamespacedKey key, Double d){
        this.meta.getPersistentDataContainer().set(key, PersistentDataType.DOUBLE, d);
        return this;
    }

    public ItemBuilder pdc(NamespacedKey key, Float f){
        this.meta.getPersistentDataContainer().set(key, PersistentDataType.FLOAT, f);
        return this;
    }

    public ItemBuilder pdc(NamespacedKey key, Integer i){
        this.meta.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, i);
        return this;
    }

    public ItemBuilder pdc(NamespacedKey key, Long l){
        this.meta.getPersistentDataContainer().set(key, PersistentDataType.LONG, l);
        return this;
    }

    public ItemBuilder pdc(NamespacedKey key, Byte b){
        this.meta.getPersistentDataContainer().set(key, PersistentDataType.BYTE, b);
        return this;
    }

    // TODO replace below 3 skull methods with PlayerProfile API
    public ItemBuilder skull(String identifier){
        XSkull.of(this.meta).profile(Profileable.of(ProfileInputType.BASE64, identifier)).apply();
        //XSkull.applySkin(this.meta, identifier);
        return this;
    }

    public ItemBuilder skull(OfflinePlayer identifier){
        XSkull.of(this.meta).profile(Profileable.of(ProfileInputType.UUID, identifier.getUniqueId().toString())).apply();
        //XSkull.applySkin(this.meta, identifier);
        return this;
    }

    public ItemBuilder skull(UUID identifier){
        XSkull.of(this.meta).profile(Profileable.of(ProfileInputType.UUID, identifier.toString())).apply();
        return this;
    }

    public ItemBuilder replace(String placeholder, String value) {
        this.meta.setDisplayName(this.meta.getDisplayName().replace(placeholder, value));
        if (this.meta.getLore() != null) {
            List<String> lore = new ArrayList<>();
            for (String line : this.meta.getLore()) {
                lore.add(line.replace(placeholder, value));
            }
            this.meta.setLore(lore);
        }
        return this;
    }

    public ItemStack build() {
        this.item.setItemMeta(this.meta);
        return this.item;
    }
}
