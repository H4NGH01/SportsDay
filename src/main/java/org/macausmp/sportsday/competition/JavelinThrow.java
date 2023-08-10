package org.macausmp.sportsday.competition;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
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
    private final List<PlayerResult> leaderboard = new ArrayList<>();
    private final List<PlayerData> queue = new ArrayList<>();
    private final Map<UUID, PlayerResult> resultMap = new HashMap<>();

    public JavelinThrow() {
        super("javelin_throw");
    }

    @Override
    public void onSetup() {
        resultMap.clear();
        queue.clear();
        queue.addAll(getPlayerDataList());
        List<Component> cl = new ArrayList<>();
        cl.add(Translation.translatable("competition.queue_text"));
        for (int i = 0; i < queue.size();) {
            PlayerData data = queue.get(i++);
            cl.add(Translation.translatable("competition.queue").args(Component.text(i), Component.text(data.getName())));
        }
        getOnlinePlayers().forEach(p -> cl.forEach(p::sendMessage));
    }

    @Override
    public void onStart() {
        ItemStack trident = new ItemStack(Material.TRIDENT);
        trident.editMeta(meta -> {
            meta.displayName(Translation.translatable("item.sportsday.javelin").decoration(TextDecoration.ITALIC, false));
            List<Component> lore = new ArrayList<>();
            lore.add(Translation.translatable("enchantment.sportsday.range").appendSpace().append(Translation.translatable("enchantment.level.5")).color(NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
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
                p.sendMessage(Translation.translatable("competition.javelin.clear").clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/kill @e[type=trident]")));
            }
        }
        if (force) return;
        leaderboard.sort((r1, r2) -> Double.compare(r2.getDistance(), r1.getDistance()));
        List<Component> cl = new ArrayList<>();
        for (int i = 0; i < leaderboard.size();) {
            PlayerResult result = leaderboard.get(i++);
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
                leaderboard.add(result);
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

    @SuppressWarnings("ClassEscapesDefinedScope")
    @Override
    public List<PlayerResult> getLeaderboard() {
        return leaderboard;
    }

    @Override
    public void onRoundStart() {
        getOnlinePlayers().forEach(p -> p.setGameMode(GameMode.SPECTATOR));
        Player p = queue.get(0).getPlayer();
        p.teleport(getLocation());
        p.setGameMode(GameMode.ADVENTURE);
        queue.remove(0);
    }

    @Override
    public void onRoundEnd() {
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
    }
}
