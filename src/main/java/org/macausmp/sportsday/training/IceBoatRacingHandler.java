package org.macausmp.sportsday.training;

import org.bukkit.entity.Boat;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDismountEvent;
import org.bukkit.event.entity.EntityMountEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.customize.PlayerCustomize;
import org.macausmp.sportsday.sport.Sport;

import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

public class IceBoatRacingHandler extends TrackSportsHandler {
    private final HashMap<UUID, Boat> boatMap = new HashMap<>();

    public IceBoatRacingHandler(Sport sport) {
        super(sport);
    }

    @Override
    public void equip(@NotNull Player player) {
        boatMap.put(player.getUniqueId(), boat(player));
    }

    @Override
    public void leaveTraining(@NotNull UUID uuid) {
        boatMap.remove(uuid).remove();
        super.leaveTraining(uuid);
    }

    @EventHandler
    public void onDismount(@NotNull EntityDismountEvent e) {
        if (e.getEntity() instanceof Player p && e.getDismounted() instanceof Boat) {
            if (isTraining(p))
                e.setCancelled(true);
            else
                boatMap.remove(p.getUniqueId());
        }
    }

    @EventHandler
    public void onMount(@NotNull EntityMountEvent e) {
        if (e.getEntity() instanceof Player p && e.getMount() instanceof Boat b) {
            if (!isTraining(p))
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
        if (!isTraining(p) || !boatMap.containsKey(p.getUniqueId()))
            return;
        boatMap.get(p.getUniqueId()).remove();
        boatMap.put(p.getUniqueId(), boat(p));
    }

    @EventHandler
    public void onBreak(@NotNull VehicleDestroyEvent e) {
        if (e.getVehicle() instanceof Boat boat && !boat.getPassengers().isEmpty()
                && boat.getPassengers().getFirst() instanceof Player p) {
            if (!isTraining(p))
                return;
            e.setCancelled(true);
        }
    }

    /**
     * Spawn a boat at player's spawnpoint and put player on it.
     * @param p the player
     * @return a new boat containing the player
     */
    private @NotNull Boat boat(@NotNull Player p) {
        Boat boat = (Boat) p.getWorld().spawnEntity(Objects.requireNonNull(p.getRespawnLocation()), PlayerCustomize.getBoatType(p));
        boat.setInvulnerable(true);
        boat.addPassenger(p);
        return boat;
    }
}
