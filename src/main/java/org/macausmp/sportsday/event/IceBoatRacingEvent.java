package org.macausmp.sportsday.event;

import org.bukkit.entity.Boat;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDismountEvent;
import org.bukkit.event.entity.EntityMountEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.macausmp.sportsday.customize.PlayerCustomize;
import org.macausmp.sportsday.sport.Sport;
import org.macausmp.sportsday.venue.Track;

import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

public class IceBoatRacingEvent extends TrackEvent {
    private final HashMap<UUID, Boat> boatMap = new HashMap<>();

    public IceBoatRacingEvent(@NotNull Sport sport, @NotNull Track track, @Nullable PersistentDataContainer save) {
        super(sport, track, save);
    }

    @Override
    protected void onEventStart() {
        getContestants().forEach(data -> boatMap.put(data.getUUID(), boat(data.getPlayer())));
    }

    @Override
    protected void onClose() {
        boatMap.values().forEach(Boat::remove);
    }

    @Override
    protected void onRaceFinish(@NotNull Player player) {
        boatMap.get(player.getUniqueId()).remove();
        boatMap.remove(player.getUniqueId());
    }

    @EventHandler
    public void onDismount(@NotNull EntityDismountEvent e) {
        if (e.getEntity() instanceof Player p && e.getDismounted() instanceof Boat) {
            if (predicate.test(p))
                e.setCancelled(true);
            else
                boatMap.remove(p.getUniqueId());
        }
    }

    @EventHandler
    public void onMount(@NotNull EntityMountEvent e) {
        if (e.getEntity() instanceof Player p && e.getMount() instanceof Boat b) {
            if (!predicate.test(p))
                return;
            if (b.getPassengers().isEmpty())
                boatMap.put(p.getUniqueId(), b);
            else
                e.setCancelled(true);
        }
    }

    @EventHandler
    public void onDeath(@NotNull PlayerDeathEvent e) {
        Player p = e.getPlayer();
        if (!predicate.test(p) || !boatMap.containsKey(p.getUniqueId()))
            return;
        boatMap.get(p.getUniqueId()).remove();
        boatMap.put(p.getUniqueId(), boat(p));
    }

    @EventHandler
    public void onBreak(@NotNull VehicleDestroyEvent e) {
        if (e.getVehicle() instanceof Boat boat && !boat.getPassengers().isEmpty()
                && boat.getPassengers().getFirst() instanceof Player p) {
            if (!predicate.test(p))
                return;
            e.setCancelled(true);
        }
    }

    private @NotNull Boat boat(@NotNull Player p) {
        Boat boat = (Boat) p.getWorld().spawnEntity(Objects.requireNonNull(p.getRespawnLocation()), PlayerCustomize.getBoatType(p));
        boat.setInvulnerable(true);
        boat.addPassenger(p);
        return boat;
    }
}
