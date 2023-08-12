package org.macausmp.sportsday.competition;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.event.PlayerFinishCompetitionEvent;
import org.macausmp.sportsday.event.PlayerFinishLapEvent;
import org.spigotmc.event.entity.EntityDismountEvent;
import org.spigotmc.event.entity.EntityMountEvent;

import java.util.HashMap;
import java.util.Objects;

public class IceBoatRacing extends AbstractTrackCompetition {
    private final HashMap<Player, Boat> boatMap = new HashMap<>();

    public IceBoatRacing() {
        super("ice_boat_racing");
    }

    @Override
    public void onSetup() {
        getPlayerDataList().forEach(data -> boatMap.put(data.getPlayer(), boat(data.getPlayer())));
    }

    @Override
    public void onStart() {

    }

    @Override
    protected void onEnd(boolean force) {
        boatMap.forEach((p, b) -> b.remove());
    }

    @Override
    public <T extends Event> void onEvent(T event) {
        if (event instanceof PlayerMoveEvent e) {
            Player player = e.getPlayer();
            if (getLeaderboard().contains(Competitions.getPlayerData(player.getUniqueId())) || boatMap.get(player) == null) return;
            super.onEvent(event);
            Location loc = player.getLocation().clone();
            loc.setY(loc.getY() - 0.5f);
            if (loc.getBlock().getType() == Material.IRON_TRAPDOOR) {
                boatMap.get(player).setVelocity(new Vector(0f, 1.3f, 0f));
            }
        }
    }

    @EventHandler
    public void onLapFinished(@NotNull PlayerFinishLapEvent e) {
        if (e.getCompetition() != this) return;
        Player p = e.getPlayer();
        boatMap.get(p).remove();
        boatMap.put(p, boat(p));
    }

    @EventHandler
    public void onFinished(@NotNull PlayerFinishCompetitionEvent e) {
        if (e.getCompetition() != this) return;
        Player p = e.getPlayer();
        boatMap.get(p).remove();
        boatMap.remove(p);
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
        boatMap.put(p, boat(p));
    }

    private @NotNull Boat boat(@NotNull Player p) {
        Boat boat = getWorld().spawn(Objects.requireNonNull(p.getBedSpawnLocation()), Boat.class);
        boat.setInvulnerable(true);
        boat.addPassenger(p);
        return boat;
    }
}
