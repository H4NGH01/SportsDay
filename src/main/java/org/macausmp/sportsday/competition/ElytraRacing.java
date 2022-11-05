package org.macausmp.sportsday.competition;

import com.destroystokyo.paper.event.player.PlayerElytraBoostEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.PlayerData;
import org.macausmp.sportsday.SportsDay;

import java.util.ArrayList;
import java.util.List;

public class ElytraRacing extends AbstractCompetition {
    private final Leaderboard<PlayerData> leaderboard = new Leaderboard<>();
    private boolean ending = false;

    @Override
    public String getID() {
        return "elytra_racing";
    }

    @Override
    public void onSetup() {
        this.ending = false;
        ItemStack elytra = new ItemStack(Material.ELYTRA);
        elytra.editMeta(meta -> {
            meta.addEnchant(Enchantment.BINDING_CURSE, 1, false);
            meta.setUnbreakable(true);
        });
        getPlayerDataList().forEach(data -> {
            if (data.isPlayerOnline()) {
                data.getPlayer().getInventory().setItem(EquipmentSlot.CHEST, elytra);
            }
        });
    }
    @Override
    public void onStart() {
        ItemStack firework = new ItemStack(Material.FIREWORK_ROCKET);
        firework.setAmount(64);
        firework.editMeta(FireworkMeta.class, meta -> meta.setPower(3));
        getPlayerDataList().forEach(data -> {
            if (data.isPlayerOnline()) {
                Player p = data.getPlayer();
                p.getInventory().setHeldItemSlot(0);
                p.getInventory().setItem(EquipmentSlot.HAND, firework);
            }
        });
    }

    @Override
    public void onEnd(boolean force) {
        if (force) return;
        List<Component> cl = new ArrayList<>();
        int i = 0;
        for (PlayerData data : getLeaderboard().getEntry()) {
            cl.add(Component.translatable("第%s名 %s").args(Component.text(++i), Component.text(data.getName())));
            if (i <= 3) {
                data.addScore(4 - i);
            }
            data.addScore(1);
        }
        getOnlinePlayers().forEach(p -> cl.forEach(p::sendMessage));
    }

    @EventHandler
    public void onElytraBoost(@NotNull PlayerElytraBoostEvent e) {
        if (Competitions.getCurrentlyCompetition() == null || Competitions.getCurrentlyCompetition() != this || getStage() != Stage.STARTED) return;
        Player p = e.getPlayer();
        if (!Competitions.containPlayer(p) || p.getGameMode() != GameMode.ADVENTURE) return;
        e.setShouldConsume(false);
    }

    @EventHandler
    public void onUseFirework(@NotNull PlayerInteractEvent e) {
        if (Competitions.getCurrentlyCompetition() == null || Competitions.getCurrentlyCompetition() != this || getStage() != Stage.STARTED) return;
        Player p = e.getPlayer();
        if (!Competitions.containPlayer(p) || p.getGameMode() != GameMode.ADVENTURE) return;
        if (e.getAction() == Action.RIGHT_CLICK_BLOCK && e.getItem() != null && e.getItem().getType() == Material.FIREWORK_ROCKET) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onDropFirework(@NotNull PlayerDropItemEvent e) {
        if (Competitions.getCurrentlyCompetition() == null || Competitions.getCurrentlyCompetition() != this || getStage() != Stage.STARTED) return;
        Player p = e.getPlayer();
        if (!Competitions.containPlayer(p) || p.getGameMode() != GameMode.ADVENTURE) return;
        if (e.getItemDrop().getItemStack().getType() == Material.FIREWORK_ROCKET) {
            e.setCancelled(true);
        }
    }

    private BukkitTask task;

    @Override
    public <T extends Event> void onEvent(T event) {
        if (event instanceof PlayerMoveEvent e) {
            Player player = e.getPlayer();
            if (getLeaderboard().contains(Competitions.getPlayerData(player.getUniqueId()))) return;
            Location loc = player.getLocation().clone();
            loc.setY(loc.getY() - 0.5f);
            CompetitionListener.spawnpoint(player, loc);
            if (loc.getBlock().getType() == FINISH_LINE) {
                getLeaderboard().add(Competitions.getPlayerData(player.getUniqueId()));
                player.playSound(player, Sound.ENTITY_ARROW_HIT_PLAYER, 1f, 1f);
                player.setGameMode(GameMode.SPECTATOR);
                getOnlinePlayers().forEach(p -> p.sendMessage(Component.text(player.getName() + "完成了比賽").color(NamedTextColor.YELLOW)));
                if (getLeaderboard().size() == getPlayerDataList().size()) {
                    if (task != null) task.cancel();
                    getOnlinePlayers().forEach(p -> p.sendActionBar(Component.text("所有選手已完成比賽，比賽結束").color(NamedTextColor.YELLOW)));
                    end(false);
                    return;
                }
                if (getLeaderboard().size() >= 3 && !ending) {
                    ending = true;
                    getOnlinePlayers().forEach(p -> p.sendMessage(Component.text("前三名已完成了比賽，比賽將於30秒後結束").color(NamedTextColor.YELLOW)));
                    task = addRunnable(new BukkitRunnable() {
                        int i = 30;
                        @Override
                        public void run() {
                            if (i > 0) {
                                getOnlinePlayers().forEach(p -> p.sendActionBar(Component.text("比賽將於" + i + "秒後結束").color(NamedTextColor.YELLOW)));
                            }
                            if (i-- == 0) {
                                getOnlinePlayers().forEach(p -> p.sendActionBar(Component.text("比賽結束").color(NamedTextColor.YELLOW)));
                                end(false);
                                cancel();
                            }
                        }
                    }.runTaskTimer(SportsDay.getInstance(), 0L, 20L));
                }
            }
        }
    }

    @Override
    public Leaderboard<PlayerData> getLeaderboard() {
        return leaderboard;
    }
}
