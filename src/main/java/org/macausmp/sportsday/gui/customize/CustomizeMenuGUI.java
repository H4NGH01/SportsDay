package org.macausmp.sportsday.gui.customize;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.apache.commons.lang3.ArrayUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.macausmp.sportsday.customize.*;
import org.macausmp.sportsday.gui.ButtonHandler;
import org.macausmp.sportsday.gui.PluginGUI;
import org.macausmp.sportsday.util.ItemUtil;
import org.macausmp.sportsday.util.TextUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class CustomizeMenuGUI extends PluginGUI {
    private final Player player;

    public CustomizeMenuGUI(@NotNull Player player) {
        super(45, Component.translatable("gui.customize.title"));
        this.player = player;
        update();
    }

    @Override
    protected void update() {
        getInventory().setItem(10, clothing());
        getInventory().setItem(12, boatType());
        getInventory().setItem(14, weaponSkin());
        getInventory().setItem(16, victoryDance());
        getInventory().setItem(28, projectileTrail());
        getInventory().setItem(30, walkingEffect());
        getInventory().setItem(32, graffitiSpray());
        getInventory().setItem(34, musickit());
    }

    private @NotNull ItemStack clothing() {
        ItemStack stack = customize(Material.LEATHER_CHESTPLATE, "clothing");
        Material[] slots = Arrays.stream(EquipmentSlot.values())
                .filter(EquipmentSlot::isArmor)
                .map(this::getType)
                .filter(Objects::nonNull)
                .toArray(Material[]::new);
        ArrayUtils.reverse(slots);
        if (slots.length > 0) {
            List<Component> lore = new ArrayList<>();
            lore.add(TextUtil.text(Component.translatable("gui.customize.selected").arguments(Component.text())));
            Arrays.stream(slots).forEach(s -> lore.add(getTypeName(s)));
            stack.lore(lore);
        }
        stack.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        return stack;
    }

    private @Nullable Material getType(EquipmentSlot slot) {
        PlayerCustomize.Cloth cloth = PlayerCustomize.getCloth(player, slot);
        return cloth != null ? cloth.getMaterial() : null;
    }

    private @NotNull ItemStack boatType() {
        ItemStack stack = customize(Material.OAK_BOAT, "boat_type");
        List<Component> lore = new ArrayList<>();
        Component c = getTypeName(Objects.requireNonNull(Material.getMaterial(PlayerCustomize.getBoatType(player).name())));
        lore.add(TextUtil.text(Component.translatable("gui.customize.selected").arguments(Component.text())).append(c));
        stack.lore(lore);
        return stack;
    }

    private @NotNull ItemStack weaponSkin() {
        ItemStack stack = customize(Material.BONE, "weapon_skin");
        Material type = PlayerCustomize.getWeaponSkin(player);
        List<Component> lore = new ArrayList<>();
        Component c = getTypeName(type);
        lore.add(TextUtil.text(Component.translatable("gui.customize.selected").arguments(Component.text())).append(c));
        stack.lore(lore);
        return stack;
    }

    private @NotNull Component getTypeName(@NotNull Material type) {
        return Component.translatable(type.translationKey())
                .color(NamedTextColor.GREEN)
                .decoration(TextDecoration.ITALIC, false);
    }

    private @NotNull ItemStack victoryDance() {
        ItemStack stack = customize(Material.PLAYER_HEAD, "victory_dance");
        VictoryDance dance = PlayerCustomize.getVictoryDance(player);
        List<Component> lore = new ArrayList<>();
        ComponentLike c = dance != null ? dance : TextUtil.text(Component.translatable("gui.text.none"));
        lore.add(TextUtil.text(Component.translatable("gui.customize.selected").arguments(c)));
        stack.lore(lore);
        return stack;
    }

    private @NotNull ItemStack projectileTrail() {
        ItemStack stack = customize(Material.ARROW, "projectile_trail");
        ParticleEffect effect = PlayerCustomize.getProjectileTrail(player);
        List<Component> lore = new ArrayList<>();
        ComponentLike c = effect != null ? effect : TextUtil.text(Component.translatable("gui.text.none"));
        lore.add(TextUtil.text(Component.translatable("gui.customize.selected").arguments(c)));
        stack.lore(lore);
        return stack;
    }

    private @NotNull ItemStack walkingEffect() {
        ItemStack stack = customize(Material.NETHER_STAR, "walking_effect");
        ParticleEffect effect = PlayerCustomize.getWalkingEffect(player);
        List<Component> lore = new ArrayList<>();
        ComponentLike c = effect != null ? effect : TextUtil.text(Component.translatable("gui.text.none"));
        lore.add(TextUtil.text(Component.translatable("gui.customize.selected").arguments(c)));
        stack.lore(lore);
        return stack;
    }

    private @NotNull ItemStack graffitiSpray() {
        ItemStack stack = customize(Material.PAINTING, "graffiti_spray");
        GraffitiSpray graffiti = PlayerCustomize.getGraffitiSpray(player);
        List<Component> lore = new ArrayList<>();
        ComponentLike c = graffiti != null ? graffiti : TextUtil.text(Component.translatable("gui.text.none"));
        lore.add(TextUtil.text(Component.translatable("gui.customize.selected").arguments(c)));
        stack.lore(lore);
        return stack;
    }

    private @NotNull ItemStack musickit() {
        ItemStack stack = customize(Material.JUKEBOX, "musickit");
        Musickit musickit = PlayerCustomize.getMusickit(player);
        List<Component> lore = new ArrayList<>();
        ComponentLike c = musickit != null ? musickit : TextUtil.text(Component.translatable("gui.text.none"));
        lore.add(TextUtil.text(Component.translatable("gui.customize.selected").arguments(c)));
        stack.lore(lore);
        return stack;
    }

    private @NotNull ItemStack customize(Material material, String customize) {
        return ItemUtil.item(material, "customize_" + customize, Component.translatable("gui.customize." + customize + ".title")
                .color(NamedTextColor.YELLOW));
    }

    @ButtonHandler
    public void onClick(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        switch (e.getSlot()) {
            case 10 -> new ClothingCustomizeGUI(p).open(p);
            case 12 -> new BoatTypeGUI(p).open(p);
            case 14 -> new WeaponSkinGUI(p).open(p);
            case 16 -> new VictoryDanceGUI(p).open(p);
            case 28 -> new ProjectileTrailGUI(p).open(p);
            case 30 -> new WalkingEffectGUI(p).open(p);
            case 32 -> new GraffitiSprayGUI(p).open(p);
            case 34 -> new MusickitGUI(p).open(p);
        }
    }
}
