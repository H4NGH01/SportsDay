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

import java.util.*;

public class JavelinThrow extends AbstractCompetition implements IRoundGame {
    private final Leaderboard<PlayerRecord> leaderboard = new Leaderboard<>();
    private final List<PlayerData> queue = new ArrayList<>();
    private final Map<UUID, PlayerRecord> recordMap = new HashMap<>();

    @Override
    public String getID() {
        return "javelin_throw";
    }

    @Override
    public void onSetup() {
        getQueue().clear();
        getQueue().addAll(getPlayerDataList());
        StringBuilder sb = new StringBuilder("出場順序\n");
        int i = 0;
        for (PlayerData data : getQueue()) {
            if (data.isPlayerOnline()) {
                sb.append("第").append(++i).append("位 ").append(data.getPlayer().getName()).append("\n");
            }
        }
        getOnlinePlayers().forEach(p -> p.sendMessage(sb.toString()));
        recordMap.clear();
        getLeaderboard().clear();
    }

    @Override
    public void onStart() {
        List<Component> lore = new ArrayList<>();
        ItemStack trident = new ItemStack(Material.TRIDENT);
        lore.add(Component.text("射程 V").decoration(TextDecoration.ITALIC, false).color(NamedTextColor.GRAY));
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
                TextComponent message = Component.text("點擊這裡清除三叉戟").clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/kill @e[type=trident]")).decoration(TextDecoration.UNDERLINED, true);
                p.sendMessage(message);
            }
        }
        if (force) return;
        getLeaderboard().getEntry().addAll(recordMap.values());
        getLeaderboard().getEntry().sort((o1, o2) -> Double.compare(o2.getDistance(), o1.getDistance()));
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (PlayerRecord record : getLeaderboard().getEntry()) {
            if (record.isRecorded()) {
                sb.append("第").append(++i).append("名 ").append(record.getPlayer().getName()).append(" 成績").append(record.getDistance()).append("米").append("\n");
            }
        }
        getOnlinePlayers().forEach(p -> p.sendMessage(Component.text(sb.substring(0, sb.length() - 1))));
    }

    @EventHandler
    public void onThrow(ProjectileLaunchEvent e) {
        if (Competitions.getCurrentlyCompetition() == null || !Competitions.getCurrentlyCompetition().equals(this) || !getStage().equals(Stage.STARTED)) return;
        if (e.getEntity().getShooter() instanceof Player p) {
            if (!Competitions.containPlayer(p)) return;
            if (e.getEntity() instanceof Trident trident) {
                recordMap.put(p.getUniqueId(), new PlayerRecord(p.getUniqueId(), p.getLocation()));
                trident.setPickupStatus(AbstractArrow.PickupStatus.DISALLOWED);
                trident.setCustomNameVisible(true);
                trident.customName(Component.text(p.getName() + "的標槍"));
            }
        }
    }

    @EventHandler
    public void onArrived(ProjectileHitEvent e) {
        if (Competitions.getCurrentlyCompetition() == null || !Competitions.getCurrentlyCompetition().equals(this) || !getStage().equals(Stage.STARTED)) return;
        if (e.getEntity().getShooter() instanceof Player player) {
            if (!Competitions.containPlayer(player)) return;
            if (e.getEntity() instanceof Trident trident) {
                PlayerRecord record = recordMap.get(player.getUniqueId());
                if (record == null) {
                    return;
                }
                record.recordTridentLocation(trident);
                trident.customName(Component.text(player.getName() + "的標槍 " + record.getDistance()));
                getWorld().strikeLightningEffect(trident.getLocation());
                getOnlinePlayers().forEach(p -> p.sendMessage(Component.text(player.getName() + "擲出了" + record.getDistance() + "米的成績")));
                onRoundEnd();
            }
        }
    }

    @Override
    public <T extends Event> void onEvent(T event) {

    }

    @Override
    public Leaderboard<PlayerRecord> getLeaderboard() {
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
        runnableList.add(new BukkitRunnable() {
            int i = 3;
            @Override
            public void run() {
                if (i > 0) {
                    getOnlinePlayers().forEach(p -> p.sendActionBar(Component.text(i + "秒後輪到下一位選手").color(NamedTextColor.YELLOW)));
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

    public static class PlayerRecord {
        private final UUID uuid;
        private final Location loc;
        private boolean recorded = false;
        private double distance;

        public PlayerRecord(UUID uuid, Location loc) {
            this.uuid = uuid;
            this.loc = loc;
        }

        public final void recordTridentLocation(@NotNull Trident trident) {
            this.distance = loc.distance(trident.getLocation());
            this.recorded = true;
        }

        public final double getDistance() {
            return this.distance;
        }

        public OfflinePlayer getPlayer() {
            return Bukkit.getOfflinePlayer(this.uuid);
        }

        public boolean isRecorded() {
            return this.recorded;
        }
    }
}
