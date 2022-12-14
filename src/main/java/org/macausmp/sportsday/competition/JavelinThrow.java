package org.macausmp.sportsday.competition;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Player;
import org.bukkit.entity.Trident;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.PlayerData;
import org.macausmp.sportsday.SportsDay;
import org.macausmp.sportsday.util.Translation;

import java.util.*;

public class JavelinThrow extends AbstractCompetition implements IRoundGame {
    private final Leaderboard<PlayerResult> leaderboard = new Leaderboard<>();
    private final List<PlayerData> queue = new ArrayList<>();
    private final Map<UUID, PlayerResult> resultMap = new HashMap<>();

    @Override
    public String getID() {
        return "javelin_throw";
    }

    @Override
    public void onSetup() {
        resultMap.clear();
        getQueue().clear();
        getQueue().addAll(getPlayerDataList());
        List<Component> cl = new ArrayList<>();
        cl.add(Translation.translatable("competition.queue_text"));
        for (int i = 0; i < getQueue().size();) {
            PlayerData data = getQueue().get(i++);
            cl.add(Translation.translatable("competition.queue").args(Component.text(i), Component.text(data.getName())));
        }
        getOnlinePlayers().forEach(p -> cl.forEach(p::sendMessage));
    }

    @Override
    public void onStart() {
        List<Component> lore = new ArrayList<>();
        ItemStack trident = new ItemStack(Material.TRIDENT);
        lore.add(Component.text("?????? V").decoration(TextDecoration.ITALIC, false).color(NamedTextColor.GRAY));
        trident.editMeta(meta -> {
            meta.lore(lore);
            meta.setUnbreakable(true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            trident.addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 0);
        });
        getPlayerDataList().forEach(data -> {
            if (data.isPlayerOnline()) {
                data.getPlayer().getInventory().setItem(0, trident);
            }
        });
        onRoundStart();
    }

    @Override
    public void onEnd(boolean force) {
        for (Player p : getOnlinePlayers()) {
            if (p.isOp()) {
                TextComponent message = Component.text("???????????????????????????").clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/kill @e[type=trident]")).decoration(TextDecoration.UNDERLINED, true);
                p.sendMessage(message);
            }
        }
        if (force) return;
        getLeaderboard().sort((r1, r2) -> Double.compare(r2.getDistance(), r1.getDistance()));
        List<Component> cl = new ArrayList<>();
        for (int i = 0; i < getLeaderboard().size();) {
            PlayerResult result = getLeaderboard().get(i++);
            cl.add(Translation.translatable("competition.javelin.rank").args(Component.text(i), Component.text(Competitions.getPlayerData(result.uuid).getName()), Component.text(result.getDistance())));
            if (i <= 3) {
                Competitions.getPlayerData(result.uuid).addScore(4 - i);
            }
            Competitions.getPlayerData(result.uuid).addScore(1);
        }
        getOnlinePlayers().forEach(p -> cl.forEach(p::sendMessage));
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
                trident.customName(Translation.translatable( "competition.javelin.javelin_name").args(p.displayName()));
            }
        }
    }

    @EventHandler
    public void onArrived(ProjectileHitEvent e) {
        if (Competitions.getCurrentlyCompetition() == null || Competitions.getCurrentlyCompetition() != this || getStage() != Stage.STARTED) return;
        if (e.getEntity().getShooter() instanceof Player player) {
            if (!Competitions.containPlayer(player)) return;
            if (e.getEntity() instanceof Trident trident) {
                PlayerResult result = resultMap.get(player.getUniqueId());
                if (result == null) {
                    return;
                }
                result.setTridentLocation(trident);
                getLeaderboard().add(result);
                trident.customName(Translation.translatable( "competition.javelin.javelin_name").args(player.displayName(), Component.text(result.getDistance())));
                resultMap.remove(player.getUniqueId());
                getWorld().strikeLightningEffect(trident.getLocation());
                getOnlinePlayers().forEach(p -> p.sendMessage(Translation.translatable("competition.javelin.result").args(player.displayName(), Component.text(result.getDistance()))));
                onRoundEnd();
            }
        }
    }

    @Override
    public <T extends Event> void onEvent(T event) {

    }

    @Override
    public Leaderboard<PlayerResult> getLeaderboard() {
        return leaderboard;
    }

    @Override
    public void onRoundStart() {
        getOnlinePlayers().forEach(p -> p.setGameMode(GameMode.SPECTATOR));
        Player p = getQueue().get(0).getPlayer();
        p.teleport(getLocation());
        p.setGameMode(GameMode.ADVENTURE);
        getQueue().remove(0);
    }

    @Override
    public void onRoundEnd() {
        if (getQueue().size() != 0) {
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
                if (i > 0) {
                    getOnlinePlayers().forEach(p -> p.sendActionBar(Translation.translatable("competition.javelin.next_round_countdown").args(Component.text(i)).color(NamedTextColor.YELLOW)));
                }
                if (i-- == 0) {
                    onRoundStart();
                    this.cancel();
                }
            }
        }.runTaskTimer(SportsDay.getInstance(), 0L, 20L));
    }

    @Override
    public List<PlayerData> getQueue() {
        return this.queue;
    }

    private static class PlayerResult {
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

        public @NotNull OfflinePlayer getPlayer() {
            return Bukkit.getOfflinePlayer(this.uuid);
        }
    }
}
