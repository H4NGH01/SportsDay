package org.macausmp.sportsday.competition;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Player;
import org.bukkit.entity.Trident;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.util.*;

import java.util.*;

public class JavelinThrow extends AbstractEvent implements IFieldEvent {
    private final List<PlayerResult> leaderboard = new ArrayList<>();
    private final List<PlayerData> queue = new ArrayList<>();
    private final Map<UUID, PlayerResult> resultMap = new HashMap<>();
    private PlayerData currentPlayer = null;
    private BukkitTask reconnectTask;
    private static final ItemStack TRIDENT = trident();

    public JavelinThrow() {
        super("javelin_throw");
    }

    @Override
    public void onSetup() {
        resultMap.clear();
        queue.clear();
        queue.addAll(getPlayerDataList());
        currentPlayer = null;
        Component c = Component.translatable("event.javelin.queue_text");
        for (int i = 0; i < queue.size();) {
            PlayerData data = queue.get(i++);
            c = c.appendNewline().append(Component.translatable("event.javelin.queue").args(Component.text(i), Component.text(data.getName())));
        }
        Bukkit.broadcast(c);
    }

    @Override
    public void onStart() {
        Competitions.getOnlinePlayers().forEach(d -> d.getPlayer().getInventory().setItem(0, TRIDENT));
        onRoundStart();
    }

    private static @NotNull ItemStack trident() {
        ItemStack trident = new ItemStack(Material.TRIDENT);
        trident.editMeta(meta -> {
            meta.displayName(TextUtil.text(Component.translatable("item.sportsday.javelin")));
            List<Component> lore = new ArrayList<>();
            lore.add(TextUtil.text(Component.translatable("enchantment.sportsday.range").args(Component.translatable("enchantment.level.5")).color(NamedTextColor.GRAY)));
            meta.lore(lore);
            meta.setUnbreakable(true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            trident.addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 0);
        });
        return trident;
    }

    @Override
    public void onEnd(boolean force) {
        Bukkit.broadcast(Component.translatable("event.javelin.clear").clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/kill @e[type=trident]")), Server.BROADCAST_CHANNEL_ADMINISTRATIVE);
        if (force) return;
        leaderboard.sort((r1, r2) -> Double.compare(r2.getDistance(), r1.getDistance()));
        Component c = Component.text().build();
        for (int i = 0; i < leaderboard.size();) {
            PlayerResult result = leaderboard.get(i++);
            c = c.append(Component.translatable("event.javelin.rank").args(Component.text(i), Component.text(Competitions.getPlayerData(result.uuid).getName()), Component.text(result.getDistance())));
            if (i < leaderboard.size()) c = c.appendNewline();
            if (i <= 3) Competitions.getPlayerData(result.uuid).addScore(4 - i);
            Competitions.getPlayerData(result.uuid).addScore(1);
        }
        Bukkit.broadcast(c);
    }

    @EventHandler
    public void onThrow(ProjectileLaunchEvent e) {
        if (Competitions.getCurrentlyCompetition() == null || Competitions.getCurrentlyCompetition() != this || getStage() != Stage.STARTED) return;
        if (e.getEntity().getShooter() instanceof Player p) {
            if (!Competitions.containPlayer(p)) return;
            if (e.getEntity() instanceof Trident trident) {
                resultMap.put(p.getUniqueId(), new PlayerResult(p.getUniqueId(), p.getLocation()));
                trident.setPickupStatus(AbstractArrow.PickupStatus.DISALLOWED);
                trident.setCustomNameVisible(true);
                trident.customName(Component.translatable("event.javelin.javelin_name").args(p.displayName()));
                addRunnable(new BukkitRunnable() {
                    private final CustomizeParticleEffect effect = PlayerCustomize.getProjectileTrail(p);
                    @Override
                    public void run() {
                        if (effect == null || trident.isDead() || trident.isOnGround()) {
                            cancel();
                            return;
                        }
                        p.spawnParticle(effect.getParticle(), trident.getLocation(), 1, 0.3f, 0.3f, 0.3f, effect.getData());
                    }
                }.runTaskTimer(PLUGIN, 0, 1L));
            }
        }
    }

    @EventHandler
    public void onArrived(ProjectileHitEvent e) {
        if (Competitions.getCurrentlyCompetition() == null || Competitions.getCurrentlyCompetition() != this || getStage() != Stage.STARTED) return;
        if (e.getEntity().getShooter() instanceof Player p) {
            if (!Competitions.containPlayer(p)) return;
            if (e.getEntity() instanceof Trident trident) {
                PlayerResult result = resultMap.get(p.getUniqueId());
                if (result == null) return;
                result.setTridentLocation(trident);
                leaderboard.add(result);
                trident.customName(Component.translatable("event.javelin.javelin_name").args(p.displayName(), Component.text(result.getDistance())));
                resultMap.remove(p.getUniqueId());
                getWorld().strikeLightningEffect(trident.getLocation());
                Bukkit.broadcast(Component.translatable("event.javelin.result").args(p.displayName(), Component.text(result.getDistance())));
                onRoundEnd();
            }
        }
    }

    @Override
    public <T extends Event> void onEvent(T event) {
        if (event instanceof PlayerJoinEvent e) {
            Player p = e.getPlayer();
            if (resultMap.containsKey(p.getUniqueId())) return;
            if (currentPlayer != null && p.getUniqueId().equals(currentPlayer.getUUID())) {
                reconnectTask.cancel();
                p.getInventory().setItem(0, TRIDENT);
                Bukkit.getServer().sendActionBar(Component.translatable("event.javelin.player_reconnected").color(NamedTextColor.YELLOW));
                p.teleport(getLocation());
                p.setGameMode(GameMode.ADVENTURE);
                queue.remove(Competitions.getPlayerData(p.getUniqueId()));
            }
            return;
        }
        if (event instanceof PlayerQuitEvent e) {
            Player p = e.getPlayer();
            if (resultMap.containsKey(p.getUniqueId())) return;
            if (currentPlayer != null && p.getUniqueId().equals(currentPlayer.getUUID())) {
                p.getInventory().clear();
                reconnectTask = addRunnable(new BukkitRunnable() {
                    int i = PLUGIN.getConfig().getInt("reconnect_time");
                    @Override
                    public void run() {
                        if (i > 0) Bukkit.getServer().sendActionBar(Component.translatable("event.javelin.player_disconnected").args(Component.text(i)).color(NamedTextColor.YELLOW));
                        if (i-- == 0) {
                            onRoundEnd();
                            this.cancel();
                        }
                    }
                }.runTaskTimer(PLUGIN, 0L, 20L));
            }
        }
    }

    @SuppressWarnings("ClassEscapesDefinedScope")
    @Override
    public List<PlayerResult> getLeaderboard() {
        return leaderboard;
    }

    @Override
    public void onRoundStart() {
        Bukkit.getOnlinePlayers().forEach(p -> p.setGameMode(GameMode.SPECTATOR));
        for (PlayerData d : queue) {
            currentPlayer = d;
            if (d.getOfflinePlayer().isOnline()) {
                currentPlayer.getPlayer().teleport(getLocation());
                currentPlayer.getPlayer().setGameMode(GameMode.ADVENTURE);
                queue.remove(d);
                return;
            }
        }
        if (reconnectTask == null || reconnectTask.isCancelled()) {
            reconnectTask = addRunnable(new BukkitRunnable() {
                int i = PLUGIN.getConfig().getInt("reconnect_time");
                @Override
                public void run() {
                    if (i > 0) Bukkit.getServer().sendActionBar(Component.translatable("event.javelin.player_disconnected").args(Component.text(i)).color(NamedTextColor.YELLOW));
                    if (i-- == 0) {
                        queue.remove(currentPlayer);
                        onRoundEnd();
                        this.cancel();
                    }
                }
            }.runTaskTimer(PLUGIN, 0L, 20L));
        }
    }

    @Override
    public void onRoundEnd() {
        currentPlayer = null;
        if (!queue.isEmpty()) {
            nextRound();
        } else {
            end(false);
        }
    }

    @Override
    public void nextRound() {
        addRunnable(new BukkitRunnable() {
            int i = 3;
            @Override
            public void run() {
                if (i > 0) Bukkit.getServer().sendActionBar(Component.translatable("event.javelin.next_round_countdown").args(Component.text(i)).color(NamedTextColor.YELLOW));
                if (i-- == 0) {
                    onRoundStart();
                    this.cancel();
                }
            }
        }.runTaskTimer(PLUGIN, 0L, 20L));
    }

    private static class PlayerResult implements PlayerHolder {
        private final UUID uuid;
        private final Location loc;
        private double distance;

        public PlayerResult(UUID uuid, Location loc) {
            this.uuid = uuid;
            this.loc = loc;
        }

        public final void setTridentLocation(@NotNull Trident trident) {
            this.distance = loc.distance(trident.getLocation());
        }

        public final double getDistance() {
            return this.distance;
        }

        @Override
        public @NotNull UUID getUUID() {
            return uuid;
        }
    }
}
