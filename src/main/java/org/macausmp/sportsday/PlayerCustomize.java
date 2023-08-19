package org.macausmp.sportsday;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ArmorMeta;
import org.bukkit.inventory.meta.ColorableArmorMeta;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Objects;

public class PlayerCustomize {
    private static final SportsDay PLUGIN = SportsDay.getInstance();
    private static final HashMap<Material, TrimMaterial> TRIM_MATERIAL = new HashMap<>();
    private static final HashMap<String, TrimPattern> TRIM_PATTERN = new HashMap<>();

    static {
        TRIM_MATERIAL.put(Material.QUARTZ, TrimMaterial.QUARTZ);
        TRIM_MATERIAL.put(Material.IRON_INGOT, TrimMaterial.IRON);
        TRIM_MATERIAL.put(Material.NETHERITE_INGOT, TrimMaterial.NETHERITE);
        TRIM_MATERIAL.put(Material.REDSTONE, TrimMaterial.REDSTONE);
        TRIM_MATERIAL.put(Material.COPPER_INGOT, TrimMaterial.COPPER);
        TRIM_MATERIAL.put(Material.GOLD_INGOT, TrimMaterial.GOLD);
        TRIM_MATERIAL.put(Material.EMERALD, TrimMaterial.EMERALD);
        TRIM_MATERIAL.put(Material.DIAMOND, TrimMaterial.DIAMOND);
        TRIM_MATERIAL.put(Material.LAPIS_LAZULI, TrimMaterial.LAPIS);
        TRIM_MATERIAL.put(Material.AMETHYST_SHARD, TrimMaterial.AMETHYST);
        TRIM_PATTERN.put("SENTRY", TrimPattern.SENTRY);
        TRIM_PATTERN.put("DUNE", TrimPattern.DUNE);
        TRIM_PATTERN.put("COAST", TrimPattern.COAST);
        TRIM_PATTERN.put("WILD", TrimPattern.WILD);
        TRIM_PATTERN.put("WARD", TrimPattern.WARD);
        TRIM_PATTERN.put("EYE", TrimPattern.EYE);
        TRIM_PATTERN.put("VEX", TrimPattern.VEX);
        TRIM_PATTERN.put("TIDE", TrimPattern.TIDE);
        TRIM_PATTERN.put("SNOUT", TrimPattern.SNOUT);
        TRIM_PATTERN.put("RIB", TrimPattern.RIB);
        TRIM_PATTERN.put("SPIRE", TrimPattern.SPIRE);
        TRIM_PATTERN.put("WAYFINDER", TrimPattern.WAYFINDER);
        TRIM_PATTERN.put("SHAPER", TrimPattern.SHAPER);
        TRIM_PATTERN.put("SILENCE", TrimPattern.SILENCE);
        TRIM_PATTERN.put("RAISER", TrimPattern.RAISER);
        TRIM_PATTERN.put("HOST", TrimPattern.HOST);
    }

    public static void suitUp(@NotNull Player player) {
        player.getInventory().setHelmet(cloth(player, EquipmentSlot.HEAD));
        player.getInventory().setChestplate(cloth(player, EquipmentSlot.CHEST));
        player.getInventory().setLeggings(cloth(player, EquipmentSlot.LEGS));
        player.getInventory().setBoots(cloth(player, EquipmentSlot.FEET));
    }

    private static @Nullable ItemStack cloth(@NotNull Player player, @NotNull EquipmentSlot slot) {
        String source = player.getUniqueId() + ".clothing." + slot.name().toLowerCase();
        if (PLUGIN.getCustomizeConfig().getString(source + ".item") == null) {
            return null;
        }
        ItemStack cloth = new ItemStack(Objects.requireNonNull(Material.getMaterial(Objects.requireNonNull(PLUGIN.getCustomizeConfig().getString(source + ".item")))));
        if (cloth.getItemMeta() instanceof ColorableArmorMeta) {
            cloth.editMeta(ColorableArmorMeta.class, meta -> meta.setColor(PlayerCustomize.getClothColor(player, slot)));
        }
        cloth.editMeta(ArmorMeta.class, meta -> {
            meta.addEnchant(Enchantment.BINDING_CURSE, 1, true);
            meta.setUnbreakable(true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
            if (hasClothTrim(player, slot)) {
                meta.setTrim(new ArmorTrim(getClothTrimMaterial(player, slot), getClothTrimPattern(player, slot)));
            }
        });
        return cloth;
    }

    public static @Nullable ItemStack getClothItem(@NotNull Player player, @NotNull EquipmentSlot slot) {
        String item = PLUGIN.getCustomizeConfig().getString(player.getUniqueId() + ".clothing." + slot.name().toLowerCase() + ".item");
        if (item != null) {
            return new ItemStack(Objects.requireNonNull(Material.getMaterial(item)));
        }
        return null;
    }

    public static void setClothItem(@NotNull Player player, @NotNull Material type) {
        PLUGIN.getCustomizeConfig().set(player.getUniqueId() + ".clothing." + type.getEquipmentSlot().name().toLowerCase() + ".item", type.name());
    }

    public static void resetCloth(@NotNull Player player, @NotNull EquipmentSlot slot) {
        PLUGIN.getCustomizeConfig().set(player.getUniqueId() + ".clothing." + slot.name().toLowerCase(), null);
    }

    public static Color getClothColor(@NotNull Player player, @NotNull EquipmentSlot slot) {
        return PLUGIN.getCustomizeConfig().getColor(player.getUniqueId() + ".clothing." + slot.name().toLowerCase() + ".color");
    }

    public static void setClothColor(@NotNull Player player, @NotNull EquipmentSlot slot, Color color) {
        PLUGIN.getCustomizeConfig().set(player.getUniqueId() + ".clothing." + slot.name().toLowerCase() + ".color", color);
    }

    public static boolean hasClothTrim(@NotNull Player player, @NotNull EquipmentSlot slot) {
        return PLUGIN.getCustomizeConfig().getString(player.getUniqueId() + ".clothing." + slot.name().toLowerCase() + ".trim") != null && PLUGIN.getCustomizeConfig().getString(player.getUniqueId() + ".clothing." + slot.name().toLowerCase() + ".trim.material") != null && PLUGIN.getCustomizeConfig().getString(player.getUniqueId() + ".clothing." + slot.name().toLowerCase() + ".trim.pattern") != null;
    }

    public static TrimMaterial getClothTrimMaterial(@NotNull Player player, @NotNull EquipmentSlot slot) {
        return TRIM_MATERIAL.get(Material.getMaterial(Objects.requireNonNull(PLUGIN.getCustomizeConfig().getString(player.getUniqueId() + ".clothing." + slot.name().toLowerCase() + ".trim.material"))));
    }

    public static void setClothTrimMaterial(@NotNull Player player, @NotNull EquipmentSlot slot, @NotNull Material type) {
        PLUGIN.getCustomizeConfig().set(player.getUniqueId() + ".clothing." + slot.name().toLowerCase() + ".trim.material", type.name());
    }

    public static TrimPattern getClothTrimPattern(@NotNull Player player, @NotNull EquipmentSlot slot) {
        return TRIM_PATTERN.get(Objects.requireNonNull(PLUGIN.getCustomizeConfig().getString(player.getUniqueId() + ".clothing." + slot.name().toLowerCase() + ".trim.pattern")));
    }

    public static void setClothTrimPattern(@NotNull Player player, @NotNull EquipmentSlot slot, @NotNull Material type) {
        PLUGIN.getCustomizeConfig().set(player.getUniqueId() + ".clothing." + slot.name().toLowerCase() + ".trim.pattern", type.name().substring(0, type.name().length() - 29));
    }

    public static void resetClothTrim(@NotNull Player player, @NotNull EquipmentSlot slot) {
        PLUGIN.getCustomizeConfig().set(player.getUniqueId() + ".clothing." + slot.name().toLowerCase() + ".trim", null);
    }

    public static String getBoat(@NotNull Player player) {
        return PLUGIN.getCustomizeConfig().getString(player.getUniqueId() + ".boat");
    }

    public static void setBoat(@NotNull Player player, @NotNull Material type) {
        PLUGIN.getCustomizeConfig().set(player.getUniqueId() + ".boat", type.name());
    }

    public static String getWeapon(@NotNull Player player) {
        return PLUGIN.getCustomizeConfig().getString(player.getUniqueId() + ".weapon");
    }

    public static void setWeapon(@NotNull Player player, @NotNull Material type) {
        PLUGIN.getCustomizeConfig().set(player.getUniqueId() + ".weapon", type.name());
    }
}
