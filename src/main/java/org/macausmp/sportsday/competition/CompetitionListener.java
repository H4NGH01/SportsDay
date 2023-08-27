package org.macausmp.sportsday.competition;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.SportsDay;
import org.macausmp.sportsday.command.CompetitionGUICommand;
import org.macausmp.sportsday.competition.sumo.Sumo;
import org.macausmp.sportsday.competition.sumo.SumoRound;
import org.macausmp.sportsday.gui.GUIButton;
import org.macausmp.sportsday.gui.GUIManager;
import org.macausmp.sportsday.gui.competition.PlayerListGUI;
import org.macausmp.sportsday.gui.customize.CustomizeMenuGUI;
import org.macausmp.sportsday.gui.customize.GraffitiSprayGUI;
import org.macausmp.sportsday.gui.menu.MenuGUI;
import org.macausmp.sportsday.util.CustomizeGraffitiSpray;
import org.macausmp.sportsday.util.CustomizeParticleEffect;
import org.macausmp.sportsday.util.PlayerCustomize;
import org.macausmp.sportsday.util.TextUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class CompetitionListener implements Listener {
    private static final SportsDay PLUGIN = SportsDay.getInstance();
    private static final List<UUID> SPAWNPOINT_LIST = new ArrayList<>();
    public static final Material CHECKPOINT = Material.getMaterial(Objects.requireNonNull(PLUGIN.getConfig().getString("checkpoint_block")));
    public static final Material DEATH = Material.getMaterial(Objects.requireNonNull(PLUGIN.getConfig().getString("death_block")));
    public static final NamespacedKey GRAFFITI = Objects.requireNonNull(NamespacedKey.fromString("graffiti_frame", PLUGIN));
    public static final ItemStack MENU = menu();
    public static final ItemStack CUSTOMIZE = customize();
    public static final ItemStack SPRAY = spray();

    @EventHandler
    public void onJoin(@NotNull PlayerJoinEvent e) {
        Player p = e.getPlayer();
        PlayerCustomize.suitUp(p);
        p.getInventory().setItem(3, MENU);
        p.getInventory().setItem(4, CUSTOMIZE);
        IEvent current = Competitions.getCurrentlyEvent();
        if (current == null || current.getStage() != Stage.STARTED) return;
        if (!Competitions.containPlayer(p)) return;
        GUIManager.COMPETITION_INFO_GUI.update();
        PlayerListGUI.updateGUI();
        current.onEvent(e);
    }

    @EventHandler
    public void onQuit(@NotNull PlayerQuitEvent e) {
        Player p = e.getPlayer();
        if (!Competitions.containPlayer(p)) return;
        GUIManager.COMPETITION_INFO_GUI.update();
        PlayerListGUI.updateGUI();
        IEvent current = Competitions.getCurrentlyEvent();
        if (current == null || current.getStage() != Stage.STARTED) return;
        current.onEvent(e);
    }

    @EventHandler
    public void onMove(@NotNull PlayerMoveEvent e) {
        Player p = e.getPlayer();
        CustomizeParticleEffect effect = PlayerCustomize.getWalkingEffect(p);
        if (effect != null) {
            Location loc = p.getLocation().clone();
            loc.setY(loc.y() + 0.3);
            p.spawnParticle(effect.getParticle(), loc, 1, 0.3f, 0.3f, 0.3f, effect.getData());
        }
        IEvent current = Competitions.getCurrentlyEvent();
        if (current == null || current.getStage() != Stage.STARTED) return;
        if (!Competitions.containPlayer(p) || !p.getGameMode().equals(GameMode.ADVENTURE)) return;
        current.onEvent(e);
        Location loc = e.getTo().clone();
        loc.setY(loc.getY() - 0.5f);
        if (SPAWNPOINT_LIST.contains(p.getUniqueId()) && loc.getWorld().getBlockAt(loc).getType() != CHECKPOINT) SPAWNPOINT_LIST.remove(p.getUniqueId());
        if (loc.getWorld().getBlockAt(loc).getType() == DEATH) p.setHealth(0);
    }

    public static void spawnpoint(@NotNull Player player, @NotNull Location loc) {
        if (!SPAWNPOINT_LIST.contains(player.getUniqueId()) && loc.getWorld().getBlockAt(loc).getType() == CHECKPOINT) {
            SPAWNPOINT_LIST.add(player.getUniqueId());
            player.setBedSpawnLocation(player.getLocation(), true);
            player.playSound(player, Sound.ENTITY_ARROW_HIT_PLAYER, 1f, 1f);
            player.sendActionBar(Component.text("Checkpoint").color(NamedTextColor.GREEN).decoration(TextDecoration.BOLD, true));
        }
    }

    @EventHandler
    public void onHit(@NotNull EntityDamageByEntityEvent e) {
        if (e.getEntity() instanceof Player player && e.getDamager() instanceof Player damager) {
            IEvent current = Competitions.getCurrentlyEvent();
            if (current != null && current == Competitions.SUMO) {
                SumoRound sumo = ((Sumo) current).getSumoStage().getCurrentRound();
                if (sumo != null && sumo.getStatus() == SumoRound.RoundStatus.STARTED && sumo.containPlayer(player) && sumo.containPlayer(damager)) {
                    e.setDamage(0);
                } else {
                    e.setCancelled(true);
                }
                return;
            }
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onDamage(@NotNull EntityDamageEvent e) {
        if (e.getEntity() instanceof Player player) {
            IEvent current = Competitions.getCurrentlyEvent();
            if (current == null || !Competitions.containPlayer(player) || current.getStage() == Stage.STARTED) return;
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlace(@NotNull BlockPlaceEvent e) {
        if (e.getPlayer().getGameMode() == GameMode.CREATIVE && !SportsDay.AUDIENCE.hasPlayer(e.getPlayer())) return;
        e.setCancelled(true);
    }

    @EventHandler
    public void onBreak(@NotNull BlockBreakEvent e) {
        if (e.getPlayer().getGameMode() == GameMode.CREATIVE && !SportsDay.AUDIENCE.hasPlayer(e.getPlayer())) return;
        e.setCancelled(true);
    }

    @EventHandler
    public void onSpray(@NotNull PlayerInteractEvent e) {
        if (e.getItem() != null && GUIButton.isSameButton(e.getItem(), SPRAY)) {
            e.setCancelled(true);
            Player p = e.getPlayer();
            if (e.getAction().equals(Action.RIGHT_CLICK_AIR)) {
                p.openInventory(new GraffitiSprayGUI(p).getInventory());
            } else if (e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
                if (p.getCooldown(SPRAY.getType()) > 0 && !p.isOp()) {
                    p.sendActionBar(Component.translatable("item.spray.cooldown").args(Component.text(p.getCooldown(SPRAY.getType()) / 20f)).color(NamedTextColor.YELLOW));
                    return;
                }
                CustomizeGraffitiSpray graffiti = PlayerCustomize.getGraffitiSpray(p);
                if (graffiti == null || Bukkit.getMap(graffiti.getId()) == null) return;
                Block b = p.getTargetBlockExact(4);
                BlockFace f = p.getTargetBlockFace(4);
                if (b != null && b.getType().isOccluding() && f != null) {
                    Location loc = b.getLocation().add(f.getDirection());
                    ItemStack map = new ItemStack(Material.FILLED_MAP);
                    map.editMeta(MapMeta.class, meta -> meta.setMapView(Bukkit.getMap(graffiti.getId())));
                    Bukkit.getServer().playSound(net.kyori.adventure.sound.Sound.sound(Key.key("minecraft:use_spray"), net.kyori.adventure.sound.Sound.Source.MASTER, 1f, 1f), p);
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            if (!p.isOp()) p.setCooldown(SPRAY.getType(), 600);
                            ItemFrame frame = loc.getWorld().spawn(loc, ItemFrame.class);
                            // Remove item frame that should not appear in an illegal position
                            if (!frame.getLocation().add(frame.getFacing().getOppositeFace().getDirection()).getBlock().getType().isOccluding()) {
                                frame.remove();
                                return;
                            }
                            frame.setVisible(false);
                            frame.setInvulnerable(true);
                            frame.setFixed(true);
                            frame.setItem(map, false);
                            frame.getPersistentDataContainer().set(GRAFFITI, PersistentDataType.BOOLEAN, true);
                        }
                    }.runTaskLater(PLUGIN, 30L);
                }
            }
        }
    }

    @EventHandler
    public void onChangeSpray(@NotNull PlayerInteractEntityEvent e) {
        ItemStack item = e.getPlayer().getInventory().getItemInMainHand();
        if (GUIButton.isSameButton(item, SPRAY) && e.getRightClicked() instanceof ItemFrame frame) {
            if (!frame.getPersistentDataContainer().has(GRAFFITI)) return;
            e.setCancelled(true);
            Player p = e.getPlayer();
            if (p.getCooldown(SPRAY.getType()) > 0 && !p.isOp()) {
                p.sendActionBar(Component.translatable("item.spray.cooldown").args(Component.text(p.getCooldown(SPRAY.getType()) / 20f)).color(NamedTextColor.YELLOW));
                return;
            }
            CustomizeGraffitiSpray graffiti = PlayerCustomize.getGraffitiSpray(p);
            if (graffiti == null || Bukkit.getMap(graffiti.getId()) == null) return;
            ItemStack map = new ItemStack(Material.FILLED_MAP);
            map.editMeta(MapMeta.class, meta -> meta.setMapView(Bukkit.getMap(graffiti.getId())));
            Bukkit.getServer().playSound(net.kyori.adventure.sound.Sound.sound(Key.key("minecraft:use_spray"), net.kyori.adventure.sound.Sound.Source.MASTER, 1f, 1f), p);
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (!p.isOp()) p.setCooldown(SPRAY.getType(), 600);
                    frame.setItem(map, false);
                }
            }.runTaskLater(PLUGIN, 30L);
        }
    }

    private static final List<UUID> EASTER_TRIGGER = new ArrayList<>();

    @EventHandler
    public void onOpenGUI(@NotNull PlayerInteractEvent e) {
        if (e.getItem() == null) return;
        if (GUIButton.isSameButton(e.getItem(), MENU)) {
            e.setCancelled(true);
            e.getPlayer().openInventory(new MenuGUI().getInventory());
            return;
        }
        if (GUIButton.isSameButton(e.getItem(), CUSTOMIZE)) {
            e.setCancelled(true);
            e.getPlayer().openInventory(new CustomizeMenuGUI().getInventory());
            return;
        }
        if (GUIButton.isSameButton(e.getItem(), CompetitionGUICommand.OP_BOOK)) {
            e.setCancelled(true);
            Player p = e.getPlayer();
            if (p.isOp()) {
                p.openInventory(GUIManager.MENU_GUI.getInventory());
                return;
            }
            // Easter egg, happen if player use the book without op permission
            if (EASTER_TRIGGER.contains(p.getUniqueId())) return;
            EASTER_TRIGGER.add(p.getUniqueId());
            new BukkitRunnable() {
                int i = 0;
                @Override
                public void run() {
                    if (!p.isOnline()) {
                        cancel();
                        return;
                    }
                    if (i >= 3 && i < 10) {
                        p.spawnParticle(Particle.BLOCK_CRACK, p.getLocation(), 20, 0.2, 0.5, 0.2, Material.REDSTONE_BLOCK.createBlockData());
                    }
                    if (i == 0) {
                        p.damage(5);
                        p.sendMessage(Component.translatable("item.op_book_easter_egg1").args(e.getItem().displayName()));
                    } else if (i == 6) {
                        p.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 40, 2, false, false));
                    } else if (i == 8) {
                        p.sendMessage(Component.translatable("item.op_book_easter_egg2"));
                    } if (i == 10) {
                        p.getWorld().setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, false);
                        p.setHealth(0);
                        p.getWorld().setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, true);
                        EASTER_TRIGGER.remove(p.getUniqueId());
                        cancel();
                    }
                    i++;
                }
            }.runTaskTimer(PLUGIN, 0L, 10L);
        }
    }

    private static @NotNull ItemStack menu() {
        ItemStack menu = new ItemStack(Material.COMPASS);
        menu.editMeta(meta -> {
            meta.displayName(TextUtil.text(Component.translatable("item.menu")));
            List<Component> lore = new ArrayList<>();
            lore.add(TextUtil.text(Component.translatable("item.menu_lore")));
            meta.lore(lore);
            meta.getPersistentDataContainer().set(SportsDay.ITEM_ID, PersistentDataType.STRING, "menu");
        });
        return menu;
    }

    private static @NotNull ItemStack customize() {
        ItemStack customize = new ItemStack(Material.CHEST);
        customize.editMeta(meta -> {
            meta.displayName(TextUtil.text(Component.translatable("item.customize")));
            List<Component> lore = new ArrayList<>();
            lore.add(TextUtil.text(Component.translatable("item.customize_lore1")));
            lore.add(TextUtil.text(Component.translatable("item.customize_lore2")));
            meta.lore(lore);
            meta.getPersistentDataContainer().set(SportsDay.ITEM_ID, PersistentDataType.STRING, "customize");
        });
        return customize;
    }

    private static @NotNull ItemStack spray() {
        ItemStack spray = new ItemStack(Material.DRAGON_BREATH);
        spray.editMeta(meta -> {
            meta.displayName(TextUtil.text(Component.translatable("item.spray")));
            List<Component> lore = new ArrayList<>();
            lore.add(TextUtil.text(Component.translatable("item.spray_lore1")));
            lore.add(TextUtil.text(Component.translatable("item.spray_lore2")));
            lore.add(TextUtil.text(Component.translatable("item.spray_lore3")));
            meta.lore(lore);
            meta.getPersistentDataContainer().set(SportsDay.ITEM_ID, PersistentDataType.STRING, "graffiti_spray");
        });
        return spray;
    }
}
