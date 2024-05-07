package org.macausmp.sportsday.customize;

import org.bukkit.*;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ArmorMeta;
import org.bukkit.inventory.meta.ColorableArmorMeta;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.macausmp.sportsday.SportsDay;
import org.macausmp.sportsday.util.ItemUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class PlayerCustomize {
    private static final SportsDay PLUGIN = SportsDay.getInstance();
    private static final Map<Material, TrimMaterial> TRIM_MATERIAL = new HashMap<>();
    private static final Map<String, TrimPattern> TRIM_PATTERN = new HashMap<>();
    private static final NamespacedKey CLOTHING = new NamespacedKey(PLUGIN, "clothing");
    private static final NamespacedKey BOAT_TYPE = new NamespacedKey(PLUGIN, "boat_type");
    private static final NamespacedKey WEAPON_SKIN = new NamespacedKey(PLUGIN, "weapon_skin");
    private static final NamespacedKey PROJECTILE_TRAIL = new NamespacedKey(PLUGIN, "projectile_trail");
    private static final NamespacedKey WALKING_EFFECT = new NamespacedKey(PLUGIN, "walking_effect");
    private static final NamespacedKey GRAFFITI_SPRAY = new NamespacedKey(PLUGIN, "graffiti_spray");
    private static final NamespacedKey MUSICKIT = new NamespacedKey(PLUGIN, "musickit");
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
        Registry.TRIM_PATTERN.forEach(k -> TRIM_PATTERN.put(k.key().value().toUpperCase(), k));
    }

    public static void suitUp(@NotNull Player player) {
        player.getInventory().setHelmet(cloth(player, EquipmentSlot.HEAD));
        player.getInventory().setChestplate(cloth(player, EquipmentSlot.CHEST));
        player.getInventory().setLeggings(cloth(player, EquipmentSlot.LEGS));
        player.getInventory().setBoots(cloth(player, EquipmentSlot.FEET));
    }

    private static @Nullable ItemStack cloth(@NotNull Player player, @NotNull EquipmentSlot slot) {
        Cloth cloth = getCloth(player, slot);
        if (cloth == null) return null;
        ItemStack item = getClothItemStack(cloth);
        item.editMeta(meta -> {
            meta.setUnbreakable(true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        });
        return ItemUtil.setBind(item);
    }

    public static @Nullable Cloth getCloth(@NotNull Player player, @NotNull EquipmentSlot slot) {
        if (!player.getPersistentDataContainer().has(CLOTHING, PersistentDataType.TAG_CONTAINER)) return null;
        PersistentDataContainer container = Objects.requireNonNull(player.getPersistentDataContainer().get(CLOTHING, PersistentDataType.TAG_CONTAINER));
        return container.get(new NamespacedKey(PLUGIN, slot.name().toLowerCase()), ClothDataType.INSTANCE);
    }

    public static @NotNull ItemStack getClothItemStack(@NotNull Cloth cloth) {
        ItemStack item = new ItemStack(cloth.material);
        if (cloth.colorable()) item.editMeta(ColorableArmorMeta.class, meta -> meta.setColor(cloth.color));
        if (cloth.trimMaterial.equals(Cloth.NONE)) return item;
        TrimMaterial tm = TRIM_MATERIAL.get(Material.valueOf(cloth.trimMaterial));
        TrimPattern tp = TRIM_PATTERN.get(cloth.trimPattern);
        if (tm != null && tp != null) {
            item.editMeta(ArmorMeta.class, meta -> meta.setTrim(new ArmorTrim(tm, tp)));
        }
        return item;
    }

    public static void setClothMaterial(@NotNull Player player, @NotNull Material type) {
        if (!player.getPersistentDataContainer().has(CLOTHING, PersistentDataType.TAG_CONTAINER)) {
            PersistentDataContainer container = player.getPersistentDataContainer().getAdapterContext().newPersistentDataContainer();
            player.getPersistentDataContainer().set(CLOTHING, PersistentDataType.TAG_CONTAINER, container);
        }
        PersistentDataContainer container = Objects.requireNonNull(player.getPersistentDataContainer().get(CLOTHING, PersistentDataType.TAG_CONTAINER));
        Cloth original = getCloth(player, type.getEquipmentSlot());
        if (original != null) {
            original.material = type;
        } else {
            original = new Cloth(type);
        }
        container.set(new NamespacedKey(PLUGIN, type.getEquipmentSlot().name().toLowerCase()), ClothDataType.INSTANCE, original);
        player.getPersistentDataContainer().set(CLOTHING, PersistentDataType.TAG_CONTAINER, container);
    }

    public static void resetCloth(@NotNull Player player, @NotNull EquipmentSlot slot) {
        PersistentDataContainer container = Objects.requireNonNull(player.getPersistentDataContainer().get(CLOTHING, PersistentDataType.TAG_CONTAINER));
        container.remove(new NamespacedKey(PLUGIN, slot.name().toLowerCase()));
        player.getPersistentDataContainer().set(CLOTHING, PersistentDataType.TAG_CONTAINER, container);
    }

    public static void setClothColor(@NotNull Player player, @NotNull EquipmentSlot slot, @Nullable Color color) {
        PersistentDataContainer container = Objects.requireNonNull(player.getPersistentDataContainer().get(CLOTHING, PersistentDataType.TAG_CONTAINER));
        Cloth cloth = Objects.requireNonNull(getCloth(player, slot));
        cloth.color = color != null ? color : Bukkit.getItemFactory().getDefaultLeatherColor();
        container.set(new NamespacedKey(PLUGIN, slot.name().toLowerCase()), ClothDataType.INSTANCE, cloth);
        player.getPersistentDataContainer().set(CLOTHING, PersistentDataType.TAG_CONTAINER, container);
    }

    public static void setClothTrimMaterial(@NotNull Player player, @NotNull EquipmentSlot slot, @NotNull Material type) {
        PersistentDataContainer container = Objects.requireNonNull(player.getPersistentDataContainer().get(CLOTHING, PersistentDataType.TAG_CONTAINER));
        Cloth cloth = Objects.requireNonNull(getCloth(player, slot));
        cloth.trimMaterial = type.name();
        container.set(new NamespacedKey(PLUGIN, slot.name().toLowerCase()), ClothDataType.INSTANCE, cloth);
        player.getPersistentDataContainer().set(CLOTHING, PersistentDataType.TAG_CONTAINER, container);
    }

    public static void setClothTrimPattern(@NotNull Player player, @NotNull EquipmentSlot slot, @NotNull Material type) {
        PersistentDataContainer container = Objects.requireNonNull(player.getPersistentDataContainer().get(CLOTHING, PersistentDataType.TAG_CONTAINER));
        Cloth cloth = Objects.requireNonNull(getCloth(player, slot));
        cloth.trimPattern = type.name().substring(0, type.name().length() - 29);
        container.set(new NamespacedKey(PLUGIN, slot.name().toLowerCase()), ClothDataType.INSTANCE, cloth);
        player.getPersistentDataContainer().set(CLOTHING, PersistentDataType.TAG_CONTAINER, container);
    }

    public static void resetClothTrim(@NotNull Player player, @NotNull EquipmentSlot slot) {
        PersistentDataContainer container = Objects.requireNonNull(player.getPersistentDataContainer().get(CLOTHING, PersistentDataType.TAG_CONTAINER));
        Cloth cloth = Objects.requireNonNull(getCloth(player, slot));
        cloth.trimMaterial = PlayerCustomize.Cloth.NONE;
        cloth.trimPattern = PlayerCustomize.Cloth.NONE;
        container.set(new NamespacedKey(PLUGIN, slot.name().toLowerCase()), ClothDataType.INSTANCE, cloth);
        player.getPersistentDataContainer().set(CLOTHING, PersistentDataType.TAG_CONTAINER, container);
    }

    public static Boat.@Nullable Type getBoatType(@NotNull Player player) {
        String type = player.getPersistentDataContainer().get(BOAT_TYPE, PersistentDataType.STRING);
        return type != null ? Boat.Type.valueOf(type) : null;
    }

    public static void setBoatType(@NotNull Player player, @NotNull Material type) {
        player.getPersistentDataContainer().set(BOAT_TYPE, PersistentDataType.STRING, type.name().substring(0, type.name().length() - 5));
    }

    public static @Nullable Material getWeaponSkin(@NotNull Player player) {
        String type = player.getPersistentDataContainer().get(WEAPON_SKIN, PersistentDataType.STRING);
        return type != null ? Material.getMaterial(type) : null;
    }

    public static void setWeaponSkin(@NotNull Player player, @NotNull Material type) {
        player.getPersistentDataContainer().set(WEAPON_SKIN, PersistentDataType.STRING, type.name());
    }

    public static @Nullable CustomizeParticleEffect getProjectileTrail(@NotNull Player player) {
        String effect = player.getPersistentDataContainer().get(PROJECTILE_TRAIL, PersistentDataType.STRING);
        return effect != null ? CustomizeParticleEffect.valueOf(effect) : null;
    }

    public static void setProjectileTrail(@NotNull Player player, CustomizeParticleEffect effect) {
        if (effect != null) {
            player.getPersistentDataContainer().set(PROJECTILE_TRAIL, PersistentDataType.STRING, effect.name());
        } else {
            player.getPersistentDataContainer().remove(PROJECTILE_TRAIL);
        }
    }

    public static @Nullable CustomizeParticleEffect getWalkingEffect(@NotNull Player player) {
        String effect = player.getPersistentDataContainer().get(WALKING_EFFECT, PersistentDataType.STRING);
        return effect != null ? CustomizeParticleEffect.valueOf(effect) : null;
    }

    public static void setWalkingEffect(@NotNull Player player, CustomizeParticleEffect effect) {
        if (effect != null) {
            player.getPersistentDataContainer().set(WALKING_EFFECT, PersistentDataType.STRING, effect.name());
        } else {
            player.getPersistentDataContainer().remove(WALKING_EFFECT);
        }
    }

    public static @Nullable CustomizeGraffitiSpray getGraffitiSpray(@NotNull Player player) {
        String graffiti = player.getPersistentDataContainer().get(GRAFFITI_SPRAY, PersistentDataType.STRING);
        return graffiti != null ? CustomizeGraffitiSpray.valueOf(graffiti) : null;
    }

    public static void setGraffitiSpray(@NotNull Player player, CustomizeGraffitiSpray graffiti) {
        if (graffiti != null) {
            player.getPersistentDataContainer().set(GRAFFITI_SPRAY, PersistentDataType.STRING, graffiti.name());
        } else {
            player.getPersistentDataContainer().remove(GRAFFITI_SPRAY);
        }
    }

    public static @Nullable CustomizeMusickit getMusickit(@NotNull Player player) {
        String musickit = player.getPersistentDataContainer().get(MUSICKIT, PersistentDataType.STRING);
        return musickit != null ? CustomizeMusickit.valueOf(musickit) : null;
    }

    public static void setMusickit(@NotNull Player player, CustomizeMusickit musickit) {
        if (musickit != null) {
            player.getPersistentDataContainer().set(MUSICKIT, PersistentDataType.STRING, musickit.name());
        } else {
            player.getPersistentDataContainer().remove(MUSICKIT);
        }
    }

    public static class Cloth {
        public static final String NONE = "none";
        private Material material;
        private String trimMaterial = NONE;
        private String trimPattern = NONE;
        private Color color = Bukkit.getItemFactory().getDefaultLeatherColor();

        private Cloth(Material material) {
            this.material = material;
        }

        public Material getMaterial() {
            return material;
        }

        public String getTrimMaterial() {
            return trimMaterial;
        }

        public String getTrimPattern() {
            return trimPattern;
        }

        public Color getColor() {
            return color;
        }

        public boolean colorable() {
            return material != null && Bukkit.getItemFactory().getItemMeta(material) instanceof ColorableArmorMeta;
        }
    }

    private static class ClothDataType implements PersistentDataType<PersistentDataContainer, Cloth> {
        public static final ClothDataType INSTANCE = new ClothDataType();

        @Override
        public @NotNull Class<PersistentDataContainer> getPrimitiveType() {
            return PersistentDataContainer.class;
        }

        @Override
        public @NotNull Class<Cloth> getComplexType() {
            return Cloth.class;
        }

        @Override
        public @NotNull PersistentDataContainer toPrimitive(@NotNull Cloth complex, @NotNull PersistentDataAdapterContext context) {
            PersistentDataContainer container = context.newPersistentDataContainer();
            container.set(new NamespacedKey(PLUGIN, "material"), PersistentDataType.STRING, complex.material.name());
            container.set(new NamespacedKey(PLUGIN, "trim-material"), PersistentDataType.STRING, complex.trimMaterial);
            container.set(new NamespacedKey(PLUGIN, "trim-pattern"), PersistentDataType.STRING, complex.trimPattern);
            if (complex.colorable()) container.set(new NamespacedKey(PLUGIN, "color"), PersistentDataType.INTEGER, complex.color.asARGB());
            return container;
        }

        @Override
        public @NotNull Cloth fromPrimitive(@NotNull PersistentDataContainer primitive, @NotNull PersistentDataAdapterContext context) {
            Cloth cloth = new Cloth(Material.valueOf(primitive.get(new NamespacedKey(PLUGIN, "material"), PersistentDataType.STRING)));
            cloth.trimMaterial = primitive.get(new NamespacedKey(PLUGIN, "trim-material"), PersistentDataType.STRING);
            cloth.trimPattern = primitive.get(new NamespacedKey(PLUGIN, "trim-pattern"), PersistentDataType.STRING);
            Integer color = primitive.get(new NamespacedKey(PLUGIN, "color"), PersistentDataType.INTEGER);
            if (color != null) cloth.color = Color.fromARGB(color);
            return cloth;
        }
    }
}
