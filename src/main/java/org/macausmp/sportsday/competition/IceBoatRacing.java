package org.macausmp.sportsday.competition;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.PlayerData;
import org.spigotmc.event.entity.EntityDismountEvent;

import java.util.HashMap;
import java.util.Objects;

public class IceBoatRacing extends AbstractCompetition {
    private final Leaderboard<PlayerData> leaderboard = new Leaderboard<>();
    private final HashMap<PlayerData, Integer> lapMap = new HashMap<>();
    private final HashMap<Player, Boat> boatMap = new HashMap<>();

    @Override
    public String getID() {
        return "ice_boat_racing";
    }

    @Override
    public void onSetup() {
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
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (PlayerData data : getLeaderboard().getEntry()) {
            sb.append("第").append(++i).append("名 ").append(data.getName()).append("\n");
        }
        getOnlinePlayers().forEach(p -> p.sendMessage(Component.text(sb.substring(0, sb.length() - 1))));
    }

    @Override
    public <T extends Event> void onEvent(T event) {
        if (event instanceof PlayerMoveEvent e) {
            Player player = e.getPlayer();
            if (getLeaderboard().contains(Competitions.getPlayerData(player.getUniqueId()))) return;
            Location loc = player.getLocation().clone();
            loc.setY(loc.getY() - 0.5f);
            CompetitionListener.spawnpoint(player, loc);
            if (loc.getBlock().getType() == CompetitionListener.FINISH_LINE) {
                PlayerData data = Competitions.getPlayerData(player.getUniqueId());
                lapMap.put(data, lapMap.get(data) + 1);
                player.playSound(player, Sound.ENTITY_ARROW_HIT_PLAYER, 1f, 1f);
                if (lapMap.get(data) == 1) {
                    boatMap.get(player).remove();
                    boatMap.put(player, getWorld().spawn(getLocation(), Boat.class));
                    boatMap.get(player).addPassenger(player);
                    getOnlinePlayers().forEach(p -> p.sendMessage(Component.text(player.getName() + "已成了第一圈").color(NamedTextColor.YELLOW)));
                } else if (lapMap.get(data) >= 2) {
                    getLeaderboard().add(Competitions.getPlayerData(player.getUniqueId()));
                    player.setGameMode(GameMode.SPECTATOR);
                    boatMap.get(player).remove();
                    boatMap.remove(player);
                    getOnlinePlayers().forEach(p -> p.sendMessage(Component.text(player.getName() + "已成了比賽").color(NamedTextColor.YELLOW)));
                    if (getLeaderboard().size() >= 3) {
                        end(false);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onDismount(@NotNull EntityDismountEvent e) {
        if (Competitions.getCurrentlyCompetition() == null || Competitions.getCurrentlyCompetition() != this) return;
        if (e.getEntity() instanceof Player p && e.getDismounted() instanceof Boat) {
            if (p.getGameMode() != GameMode.ADVENTURE || !Competitions.containPlayer(p)) return;
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onFall(PlayerDeathEvent e) {
        if (Competitions.getCurrentlyCompetition() == null || Competitions.getCurrentlyCompetition() != this) return;
        Player p = e.getPlayer();
        boatMap.get(p).remove();
        boatMap.put(p, getWorld().spawn(Objects.requireNonNull(p.getBedSpawnLocation()), Boat.class));
        boatMap.get(p).addPassenger(p);
    }

    @Override
    public Leaderboard<PlayerData> getLeaderboard() {
        return leaderboard;
    }
}
