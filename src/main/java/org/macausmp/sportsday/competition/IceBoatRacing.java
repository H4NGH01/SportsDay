package org.macausmp.sportsday.competition;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.util.PlayerCustomize;
import org.spigotmc.event.entity.EntityDismountEvent;
import org.spigotmc.event.entity.EntityMountEvent;

import java.util.HashMap;
import java.util.Objects;

public class IceBoatRacing extends AbstractTrackEvent {
    private final HashMap<Player, Boat> boatMap = new HashMap<>();

    public IceBoatRacing() {
        super("ice_boat_racing");
    }

    @Override
    protected void onSetup() {
        getLocation().getWorld().getEntitiesByClass(Boat.class).forEach(Boat::remove);
        getPlayerDataList().forEach(data -> boatMap.put(data.getPlayer(), boat(data.getPlayer())));
    }

    @Override
    protected void onStart() {
    }

    @Override
    protected void onEnd(boolean force) {
        boatMap.forEach((p, b) -> b.remove());
    }

    @Override
    protected void onPractice(@NotNull Player p) {
        boatMap.put(p, boat(p));
    }

    @EventHandler
    public void onMove(@NotNull PlayerMoveEvent e) {
        IEvent event = Competitions.getCurrentlyEvent();
        Player p = e.getPlayer();
        boolean b = event == this && getStage() == Stage.STARTED && Competitions.containPlayer(p) && !getLeaderboard().contains(Competitions.getPlayerData(p.getUniqueId()));
        if ((b || AbstractEvent.inPractice(p)) && boatMap.get(p) != null) bounce(p);
    }

    private void bounce(@NotNull Player p) {
        Location loc = p.getLocation().clone();
        loc.setY(loc.getY() - 0.5f);
        if (loc.getBlock().getType() == Material.IRON_TRAPDOOR) boatMap.get(p).setVelocity(new Vector(0f, 1.0f, 0f));
    }

    @Override
    protected void onCompletedLap(@NotNull Player p) {
        boatMap.get(p).remove();
        boatMap.put(p, boat(p));
    }

    @Override
    protected void onRaceFinish(@NotNull Player p) {
        boatMap.get(p).remove();
        boatMap.remove(p);
    }

    @EventHandler
    public void onDismount(@NotNull EntityDismountEvent e) {
        if (e.getEntity() instanceof Player p && e.getDismounted() instanceof Boat) {
            IEvent event = Competitions.getCurrentlyEvent();
            boolean bl = event == this && Competitions.containPlayer(p) && !getLeaderboard().contains(Competitions.getPlayerData(p.getUniqueId()));
            if (bl || AbstractEvent.inPractice(p)) {
                e.setCancelled(true);
            } else {
                boatMap.remove(p);
            }
        }
    }

    @EventHandler
    public void onMount(@NotNull EntityMountEvent e) {
        if (e.getEntity() instanceof Player p && e.getMount() instanceof Boat b) {
            IEvent event = Competitions.getCurrentlyEvent();
            boolean bl = event == this && Competitions.containPlayer(p) && !getLeaderboard().contains(Competitions.getPlayerData(p.getUniqueId()));
            if (bl || AbstractEvent.inPractice(p)) boatMap.put(p, b);
        }
    }

    @EventHandler
    public void onDeath(@NotNull PlayerDeathEvent e) {
        IEvent event = Competitions.getCurrentlyEvent();
        Player p = e.getPlayer();
        boolean bl = event == this && Competitions.containPlayer(p) && !getLeaderboard().contains(Competitions.getPlayerData(p.getUniqueId()));
        if ((bl || AbstractEvent.inPractice(p)) && boatMap.get(p) != null) {
            boatMap.get(p).remove();
            boatMap.put(p, boat(p));
        }
    }

    @EventHandler
    public void onFall(@NotNull EntityDamageEvent e) {
        if (!(e.getEntity() instanceof Player p)) return;
        IEvent event = Competitions.getCurrentlyEvent();
        boolean bl = event == this && Competitions.containPlayer(p) && !getLeaderboard().contains(Competitions.getPlayerData(p.getUniqueId()));
        if ((bl || AbstractEvent.inPractice(p)) && boatMap.get(p) != null && e.getCause().equals(EntityDamageEvent.DamageCause.FALL)) e.setCancelled(true);
    }

    @EventHandler
    public void onBreak(@NotNull VehicleDestroyEvent e) {
        if (!e.getVehicle().getPassengers().isEmpty()) e.setCancelled(true);
    }

    private @NotNull Boat boat(@NotNull Player p) {
        Boat boat = getWorld().spawn(Objects.requireNonNull(p.getBedSpawnLocation()), Boat.class);
        Boat.Type type = PlayerCustomize.getBoatType(p);
        if (type != null) boat.setBoatType(type);
        boat.setInvulnerable(true);
        boat.addPassenger(p);
        return boat;
    }
}
