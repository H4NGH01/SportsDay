package org.macausmp.sportsday.competition;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.PlayerData;
import org.macausmp.sportsday.SportsDay;
import org.spigotmc.event.entity.EntityDismountEvent;
import org.spigotmc.event.entity.EntityMountEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class IceBoatRacing extends AbstractCompetition {
    private final Leaderboard<PlayerData> leaderboard = new Leaderboard<>();
    private final HashMap<PlayerData, Integer> lapMap = new HashMap<>();
    private final HashMap<Player, Boat> boatMap = new HashMap<>();
    private boolean ending = false;

    @Override
    public String getID() {
        return "ice_boat_racing";
    }

    @Override
    public void onSetup() {
        ending = false;
        getPlayerDataList().forEach(data -> {
            if (data.isPlayerOnline()) {
                lapMap.put(data, 0);
                boatMap.put(data.getPlayer(), getWorld().spawn(getLocation(), Boat.class));
            }
        });
        boatMap.forEach((p, b) -> b.addPassenger(p));
    }

    @Override
    public void onStart() {

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
                PlayerData data = Competitions.getPlayerData(player.getUniqueId());
                lapMap.put(data, lapMap.get(data) + 1);
                player.playSound(player, Sound.ENTITY_ARROW_HIT_PLAYER, 1f, 1f);
                if (lapMap.get(data) < 2) {
                    boatMap.get(player).remove();
                    boatMap.put(player, getWorld().spawn(getLocation(), Boat.class));
                    boatMap.get(player).addPassenger(player);
                    getOnlinePlayers().forEach(p -> p.sendMessage(Component.text(player.getName() + "完成了第一圈").color(NamedTextColor.YELLOW)));
                } else {
                    getLeaderboard().add(Competitions.getPlayerData(player.getUniqueId()));
                    player.setGameMode(GameMode.SPECTATOR);
                    boatMap.get(player).remove();
                    boatMap.remove(player);
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
                return;
            }
            if (loc.getBlock().getType() == Material.IRON_TRAPDOOR) {
                boatMap.get(player).setVelocity(new Vector(0f, 1.3f, 0f));
            }
        }
    }

    @EventHandler
    public void onDismount(@NotNull EntityDismountEvent e) {
        if (Competitions.getCurrentlyCompetition() == null || Competitions.getCurrentlyCompetition() != this) return;
        if (e.getEntity() instanceof Player p && e.getDismounted() instanceof Boat) {
            if (getLeaderboard().contains(Competitions.getPlayerData(p.getUniqueId())) || p.getGameMode() != GameMode.ADVENTURE || !Competitions.containPlayer(p)) return;
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onMount(EntityMountEvent e) {
        if (Competitions.getCurrentlyCompetition() == null || Competitions.getCurrentlyCompetition() != this) return;
        if (e.getEntity() instanceof Player p && e.getMount() instanceof Boat b) {
            if (getLeaderboard().contains(Competitions.getPlayerData(p.getUniqueId())) || !Competitions.containPlayer(p)) return;
            boatMap.put(p, b);
        }
    }

    @EventHandler
    public void onFall(PlayerDeathEvent e) {
        if (Competitions.getCurrentlyCompetition() == null || Competitions.getCurrentlyCompetition() != this) return;
        Player p = e.getPlayer();
        if (getLeaderboard().contains(Competitions.getPlayerData(p.getUniqueId())) || boatMap.get(p) == null) return;
        boatMap.get(p).remove();
        boatMap.put(p, getWorld().spawn(Objects.requireNonNull(p.getBedSpawnLocation()), Boat.class));
        boatMap.get(p).addPassenger(p);
    }

    @Override
    public Leaderboard<PlayerData> getLeaderboard() {
        return leaderboard;
    }
}
