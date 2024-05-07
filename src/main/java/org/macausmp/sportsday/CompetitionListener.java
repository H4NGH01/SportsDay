package org.macausmp.sportsday;

import com.destroystokyo.paper.ParticleBuilder;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.competition.*;
import org.macausmp.sportsday.competition.sumo.Sumo;
import org.macausmp.sportsday.competition.sumo.SumoMatch;
import org.macausmp.sportsday.customize.CustomizeGraffitiSpray;
import org.macausmp.sportsday.customize.CustomizeParticleEffect;
import org.macausmp.sportsday.customize.PlayerCustomize;
import org.macausmp.sportsday.gui.PluginGUI;
import org.macausmp.sportsday.gui.competition.CompetitionMenuGUI;
import org.macausmp.sportsday.gui.customize.CustomizeMenuGUI;
import org.macausmp.sportsday.gui.customize.GraffitiSprayGUI;
import org.macausmp.sportsday.gui.menu.MenuGUI;
import org.macausmp.sportsday.util.ItemUtil;

import java.util.*;

public final class CompetitionListener implements Listener {
    private static final SportsDay PLUGIN = SportsDay.getInstance();
    private static final Set<UUID> SPAWNPOINT_SET = new HashSet<>();
    public static final Material CHECKPOINT = Material.getMaterial(Objects.requireNonNull(PLUGIN.getConfig().getString("checkpoint_block")));
    public static final Material DEATH = Material.getMaterial(Objects.requireNonNull(PLUGIN.getConfig().getString("death_block")));
    public static final NamespacedKey GRAFFITI = new NamespacedKey(PLUGIN, "graffiti_frame");
    private static final Set<UUID> EASTER_EGG = new HashSet<>();

    @EventHandler
    public void onMove(@NotNull PlayerMoveEvent e) {
        Player p = e.getPlayer();
        CustomizeParticleEffect effect = PlayerCustomize.getWalkingEffect(p);
        if (effect != null) {
            Location loc = p.getLocation().clone();
            loc.setY(loc.y() + 0.3);
            p.spawnParticle(effect.getParticle(), loc, 1, 0.3f, 0.3f, 0.3f, effect.getData());
        }
        IEvent current = Competitions.getCurrentEvent();
        if (current instanceof ITrackEvent && current.getStatus() == Status.STARTED && Competitions.isContestant(p) || AbstractEvent.getPracticeEvent(p) instanceof ITrackEvent) {
            Location loc = e.getTo().clone();
            loc.setY(loc.getY() - 0.5f);
            spawnpoint(p, loc);
            if (SPAWNPOINT_SET.contains(p.getUniqueId()) && loc.getWorld().getBlockAt(loc).getType() != CHECKPOINT) SPAWNPOINT_SET.remove(p.getUniqueId());
            if (loc.getWorld().getBlockAt(loc).getType() == DEATH) p.setHealth(0);
        }
    }

    private void spawnpoint(@NotNull Player player, @NotNull Location loc) {
        if (loc.getWorld().getBlockAt(loc).getType() != CHECKPOINT || SPAWNPOINT_SET.contains(player.getUniqueId())) return;
        SPAWNPOINT_SET.add(player.getUniqueId());
        player.setBedSpawnLocation(player.getLocation(), true);
        player.playSound(Sound.sound(Key.key("minecraft:entity.arrow.hit_player"), Sound.Source.MASTER, 1f, 1f));
        player.sendActionBar(Component.text("Checkpoint").color(NamedTextColor.GREEN).decoration(TextDecoration.BOLD, true));
    }

    @EventHandler
    public void onHit(@NotNull EntityDamageByEntityEvent e) {
        if (e.getEntity() instanceof Player player && e.getDamager() instanceof Player damager) {
            IEvent current = Competitions.getCurrentEvent();
            if (current == Competitions.SUMO) {
                SumoMatch match = ((Sumo) current).getSumoStage().getCurrentMatch();
                boolean b = match != null && match.getStatus() == SumoMatch.MatchStatus.STARTED && match.contain(player.getUniqueId()) && match.contain(damager.getUniqueId());
                if (b || AbstractEvent.inPractice(player, Competitions.SUMO)) {
                    e.setDamage(0);
                    return;
                }
            }
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onDamage(@NotNull EntityDamageEvent e) {
        if (e.getEntity() instanceof Player player) {
            IEvent current = Competitions.getCurrentEvent();
            if (current != null && current.getStatus() == Status.STARTED || AbstractEvent.inPractice(player)) return;
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlace(@NotNull BlockPlaceEvent e) {
        Player p = e.getPlayer();
        if (p.isOp() || p.getGameMode() == GameMode.CREATIVE) return;
        e.setCancelled(true);
    }

    @EventHandler
    public void onBreak(@NotNull BlockBreakEvent e) {
        Player p = e.getPlayer();
        if (p.isOp() || p.getGameMode() == GameMode.CREATIVE) return;
        e.setCancelled(true);
    }

    @EventHandler
    public void onDropItem(@NotNull PlayerDropItemEvent e) {
        Player p = e.getPlayer();
        if (p.isOp() || p.getGameMode() == GameMode.CREATIVE) return;
        if (ItemUtil.isBind(e.getItemDrop().getItemStack())) e.setCancelled(true);
    }

    @EventHandler
    public void onSwapItem(@NotNull PlayerSwapHandItemsEvent e) {
        Player p = e.getPlayer();
        if (p.isOp() || p.getGameMode() == GameMode.CREATIVE) return;
        ItemStack item = e.getOffHandItem();
        if (item == null) return;
        if (ItemUtil.isBind(item)) e.setCancelled(true);
    }

    @EventHandler
    public void onQuitPractice(@NotNull PlayerInteractEvent e) {
        if (e.getItem() == null) return;
        Player p = e.getPlayer();
        if (!ItemUtil.equals(e.getItem(), ItemUtil.LEAVE_PRACTICE)) return;
        AbstractEvent.leavePractice(p);
        p.playSound(Sound.sound(Key.key("minecraft:ui.button.click"), Sound.Source.MASTER, 1f, 1f));
    }

    @EventHandler
    public void onSpray(@NotNull PlayerInteractEvent e) {
        if (e.getItem() != null && ItemUtil.equals(e.getItem(), ItemUtil.SPRAY)) {
            e.setCancelled(true);
            Player p = e.getPlayer();
            if (e.getAction().equals(Action.RIGHT_CLICK_AIR)) {
                p.openInventory(new GraffitiSprayGUI(p).getInventory());
                p.playSound(Sound.sound(Key.key("minecraft:ui.button.click"), Sound.Source.MASTER, 1f, 1f));
                return;
            }
            if (e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
                if (p.getCooldown(ItemUtil.SPRAY.getType()) > 0 && !p.isOp()) {
                    p.sendActionBar(Component.translatable("item.spray.cooldown").args(Component.text(p.getCooldown(ItemUtil.SPRAY.getType()) / 20f)).color(NamedTextColor.YELLOW));
                    return;
                }
                CustomizeGraffitiSpray graffiti = PlayerCustomize.getGraffitiSpray(p);
                if (graffiti == null || Bukkit.getMap(graffiti.ordinal()) == null) return;
                Block b = p.getTargetBlockExact(4);
                BlockFace f = p.getTargetBlockFace(4);
                if (b == null || !b.getType().isOccluding() || f == null) return;
                Location loc = b.getLocation().add(f.getDirection());
                ItemStack map = new ItemStack(Material.FILLED_MAP);
                map.editMeta(MapMeta.class, meta -> meta.setMapView(Bukkit.getMap(graffiti.ordinal())));
                Bukkit.getServer().playSound(Sound.sound(Key.key("minecraft:use_spray"), Sound.Source.MASTER, 1f, 1f));
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (!p.isOp()) p.setCooldown(ItemUtil.SPRAY.getType(), 600);
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

    @EventHandler
    public void onChangeSpray(@NotNull PlayerInteractEntityEvent e) {
        ItemStack item = e.getPlayer().getInventory().getItemInMainHand();
        if (ItemUtil.equals(item, ItemUtil.SPRAY) && e.getRightClicked() instanceof ItemFrame frame) {
            if (!frame.getPersistentDataContainer().has(GRAFFITI)) return;
            e.setCancelled(true);
            Player p = e.getPlayer();
            if (p.getCooldown(ItemUtil.SPRAY.getType()) > 0 && !p.isOp()) {
                p.sendActionBar(Component.translatable("item.spray.cooldown").args(Component.text(p.getCooldown(ItemUtil.SPRAY.getType()) / 20f)).color(NamedTextColor.YELLOW));
                return;
            }
            CustomizeGraffitiSpray graffiti = PlayerCustomize.getGraffitiSpray(p);
            if (graffiti == null || Bukkit.getMap(graffiti.ordinal()) == null) return;
            ItemStack map = new ItemStack(Material.FILLED_MAP);
            map.editMeta(MapMeta.class, meta -> meta.setMapView(Bukkit.getMap(graffiti.ordinal())));
            Bukkit.getServer().playSound(Sound.sound(Key.key("minecraft:use_spray"), Sound.Source.MASTER, 1f, 1f));
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (!p.isOp()) p.setCooldown(ItemUtil.SPRAY.getType(), 600);
                    frame.setItem(map, false);
                }
            }.runTaskLater(PLUGIN, 30L);
        }
    }

    @EventHandler
    public void onClick(@NotNull InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player p) || e.getClickedInventory() == null) return;
        if (e.getInventory().getHolder() instanceof PluginGUI gui) {
            e.setCancelled(true);
            ItemStack item = e.getCurrentItem();
            if (item == null || !ItemUtil.hasID(item)) return;
            gui.click(e, p, item);
        } else {
            if (p.isOp() || p.getGameMode() == GameMode.CREATIVE) return;
            ItemStack current = e.getCurrentItem();
            ItemStack button = e.getHotbarButton() == -1 ? null : p.getInventory().getItem(e.getHotbarButton());
            if (current != null && ItemUtil.isBind(current) || button != null && ItemUtil.isBind(button)) e.setCancelled(true);
        }
    }

    @EventHandler
    public void onClose(@NotNull InventoryCloseEvent e) {
        if (e.getInventory().getHolder() instanceof PluginGUI gui) gui.onClose();
    }

    @EventHandler
    public void onOpenGUI(@NotNull PlayerInteractEvent e) {
        if (e.getItem() == null) return;
        if (ItemUtil.hasID(e.getItem())) e.setCancelled(true);
        Player p = e.getPlayer();
        if (ItemUtil.equals(e.getItem(), ItemUtil.MENU)) {
            p.openInventory(new MenuGUI().getInventory());
            p.playSound(Sound.sound(Key.key("minecraft:ui.button.click"), Sound.Source.MASTER, 1f, 1f));
            return;
        }
        if (ItemUtil.equals(e.getItem(), ItemUtil.CUSTOMIZE)) {
            p.openInventory(new CustomizeMenuGUI(p).getInventory());
            p.playSound(Sound.sound(Key.key("minecraft:ui.button.click"), Sound.Source.MASTER, 1f, 1f));
            return;
        }
        if (ItemUtil.equals(e.getItem(), ItemUtil.OP_BOOK)) {
            if (p.isOp()) {
                p.openInventory(new CompetitionMenuGUI().getInventory());
                p.playSound(Sound.sound(Key.key("minecraft:ui.button.click"), Sound.Source.MASTER, 1f, 1f));
                return;
            }
            // Easter egg, happen if player use the book without op permission
            if (EASTER_EGG.contains(p.getUniqueId())) return;
            EASTER_EGG.add(p.getUniqueId());
            new BukkitRunnable() {
                int i = 0;
                @Override
                public void run() {
                    if (!p.isOnline()) {
                        EASTER_EGG.remove(p.getUniqueId());
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
                    } if (i >= 10) {
                        p.getWorld().setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, false);
                        p.setHealth(0);
                        p.getWorld().setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, true);
                        EASTER_EGG.remove(p.getUniqueId());
                        cancel();
                    }
                    i++;
                }
            }.runTaskTimer(PLUGIN, 0L, 10L);
        }
    }

    private static final Map<UUID, JudgementCut> JUDGEMENT_CUT = new HashMap<>();

    @SuppressWarnings({"SpellCheckingInspection", "deprecation"})
    @EventHandler
    public void onJudgementCut(@NotNull PlayerInteractEvent e) {
        ItemStack item = e.getItem();
        Player p = e.getPlayer();
        if (item == null || !item.hasItemMeta() || p.hasCooldown(item.getType()) || !e.getAction().isRightClick()) return;
        if (Boolean.TRUE.equals(item.getItemMeta().getPersistentDataContainer().get(new NamespacedKey(PLUGIN, "yamato"), PersistentDataType.BOOLEAN))) {
            UUID uuid = p.getUniqueId();
            JUDGEMENT_CUT.putIfAbsent(uuid, new JudgementCut(Objects.requireNonNull(p.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED)).getBaseValue()));
            final JudgementCut jc = JUDGEMENT_CUT.get(uuid);
            final Map<Integer, Boolean> times = jc.times;

            // Set holding
            jc.holding = true;

            // The first click
            if (jc.task == null || jc.task.isCancelled()) {
                jc.task = new BukkitRunnable() {
                    int i = 0;
                    @Override
                    public void run() {
                        if (!JUDGEMENT_CUT.containsKey(uuid) || p.hasCooldown(item.getType())) {
                            cancel();
                            return;
                        }

                        if (++i > 3) {
                            // Make yamato sparkles
                            if (Enchantment.DURABILITY.canEnchantItem(item)) item.addEnchantment(Enchantment.DURABILITY, 1);

                            boolean perfect = i == 4;

                            // Cancel this if this is not first judgement cut and perfect judgement cut
                            if (!times.isEmpty() && !perfect) {
                                p.setCooldown(item.getType(), 20);
                                item.removeEnchantment(Enchantment.DURABILITY);
                                times.clear();
                                cancel();
                                return;
                            }

                            // Release success
                            if (!jc.holding) {
                                Location loc = p.getEyeLocation().add(p.getLocation().getDirection().multiply(50));
                                if (p.getTargetEntity(100, true) instanceof LivingEntity le) {
                                    loc = le.getEyeLocation();
                                } else {
                                    Block b = p.getTargetBlockExact(100);
                                    BlockFace bf = p.getTargetBlockFace(100);
                                    if (b != null && bf != null) loc = b.getLocation().add(bf.getDirection().multiply(2));
                                }
                                ParticleBuilder builder = new ParticleBuilder(Particle.FLASH);
                                builder.location(loc);

                                boolean shift = !perfect && p.isOnGround();
                                if (shift) {
                                    p.setVelocity(new Vector(Math.sin(Math.toRadians(p.getYaw())), 0, -Math.cos(Math.toRadians(p.getYaw()))));
                                    Objects.requireNonNull(p.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED)).setBaseValue(0);
                                }
                                Collection<LivingEntity> le = loc.getNearbyLivingEntities(3);
                                World world = p.getWorld();
                                Location finalLoc = loc;

                                // Do judgement cut
                                new BukkitRunnable() {
                                    int j = perfect ? 3 : 1;
                                    final int k = j;
                                    @Override
                                    public void run() {
                                        if (k == j) {
                                            jc.lock = true;
                                            Objects.requireNonNull(p.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED)).setBaseValue(0);
                                            p.spawnParticle(Particle.SWEEP_ATTACK, p.getLocation().add(p.getLocation().getDirection().setY(0)).add(0, 1, 0), 1);
                                        }
                                        le.forEach(e -> {
                                            e.damage(5, p);
                                            e.setVelocity(new Vector());
                                            e.setNoDamageTicks(1);
                                        });
                                        builder.spawn();
                                        world.playSound(Sound.sound(Key.key("minecraft:entity.player.attack.sweep"), Sound.Source.MASTER, 5f, 1f), finalLoc.x(), finalLoc.y(), finalLoc.z());
                                        if (--j <= 0) {
                                            Objects.requireNonNull(p.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED)).setBaseValue(jc.baseSpeed);
                                            if (times.values().stream().filter(b -> b).count() >= 3) {
                                                p.setCooldown(item.getType(), 20);
                                                times.clear();
                                                p.sendActionBar(Component.text("Jackpot!").color(NamedTextColor.GOLD));
                                            }
                                            item.removeEnchantment(Enchantment.DURABILITY);
                                            cancel();
                                            new BukkitRunnable() {
                                                @Override
                                                public void run() {
                                                    if (!jc.holding) {
                                                        p.setCooldown(item.getType(), 20);
                                                        item.removeEnchantment(Enchantment.DURABILITY);
                                                        times.clear();
                                                    }
                                                    jc.lock = false;
                                                }
                                            }.runTaskLater(PLUGIN, 4L);
                                        }
                                    }
                                }.runTaskTimer(PLUGIN, shift ? 10L : 0L, 2L);
                                jc.task.cancel();
                                times.put(times.size(), perfect);
                                cancel();
                                return;
                            }
                        }

                        // Release fail
                        if (!jc.holding) {
                            if (!times.isEmpty()) {
                                p.setCooldown(item.getType(), 20);
                                item.removeEnchantment(Enchantment.DURABILITY);
                                times.clear();
                            }
                            cancel();
                        }

                        // Init right-clicking status for next times
                        if (!jc.lock) jc.holding = false;
                    }
                }.runTaskTimer(PLUGIN, 0L, 4L);
            }
        }
    }

    static void cancelJudgementCut(@NotNull Player p) {
        UUID uuid = p.getUniqueId();
        if (JUDGEMENT_CUT.containsKey(uuid)) {
            Objects.requireNonNull(p.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED)).setBaseValue(JUDGEMENT_CUT.get(uuid).baseSpeed);
            p.getInventory().getItemInMainHand().removeEnchantment(Enchantment.DURABILITY);
            JUDGEMENT_CUT.remove(uuid);
        }
    }

    private static class JudgementCut {
        boolean holding = false;
        boolean lock = false;
        final Map<Integer, Boolean> times = new HashMap<>();
        final double baseSpeed;
        BukkitTask task;

        private JudgementCut(double baseSpeed) {
            this.baseSpeed = baseSpeed;
        }
    }
}
