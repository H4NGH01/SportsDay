package org.macausmp.sportsday.venue;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.SportsDay;

public class TrackPoint {
    private Location location;
    private Vector corner1 = new Vector();
    private Vector corner2 = new Vector();
    private final BoundingBox boundingBox;

    public TrackPoint(Location location, BoundingBox boundingBox) {
        this.location = location;
        this.boundingBox = boundingBox;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public void setBoundingBoxCorner1(Vector vector) {
        this.corner1 = vector;
        this.boundingBox.resize(corner1.getX(), corner1.getY(), corner1.getZ(), corner2.getX(), corner2.getY(), corner2.getZ());
    }

    public void setBoundingBoxCorner2(Vector vector) {
        this.corner2 = vector;
        this.boundingBox.resize(corner1.getX(), corner1.getY(), corner1.getZ(), corner2.getX(), corner2.getY(), corner2.getZ());
    }

    public BoundingBox getBoundingBox() {
        return boundingBox;
    }

    public boolean overlaps(@NotNull Entity entity) {
        return boundingBox.overlaps(entity.getBoundingBox());
    }

    public void glow(@NotNull Player player) {
        double x1 = corner1.getBlockX(), x2 = corner2.getBlockX(), y1 = corner1.getBlockY(), y2 = corner2.getBlockY(), z1 = corner1.getBlockZ(), z2 = corner2.getBlockZ();
        new BukkitRunnable() {
            int i = 0;
            @Override
            public void run() {
                for (double x = x1, max = x2 + 0.5; x < max; x += 0.5) {
                    player.spawnParticle(Particle.HAPPY_VILLAGER, x, y1, z1, 1);
                    player.spawnParticle(Particle.HAPPY_VILLAGER, x, y2, z1, 1);
                    player.spawnParticle(Particle.HAPPY_VILLAGER, x, y1, z2, 1);
                    player.spawnParticle(Particle.HAPPY_VILLAGER, x, y2, z2, 1);
                }
                for (double y = y1, max = y2 + 0.5; y < max; y += 0.5) {
                    player.spawnParticle(Particle.HAPPY_VILLAGER, x1, y, z1, 1);
                    player.spawnParticle(Particle.HAPPY_VILLAGER, x2, y, z1, 1);
                    player.spawnParticle(Particle.HAPPY_VILLAGER, x1, y, z2, 1);
                    player.spawnParticle(Particle.HAPPY_VILLAGER, x2, y, z2, 1);
                }
                for (double z = z1, max = z2 + 0.5; z < max; z += 0.5) {
                    player.spawnParticle(Particle.HAPPY_VILLAGER, x1, y1, z, 1);
                    player.spawnParticle(Particle.HAPPY_VILLAGER, x2, y1, z, 1);
                    player.spawnParticle(Particle.HAPPY_VILLAGER, x1, y2, z, 1);
                    player.spawnParticle(Particle.HAPPY_VILLAGER, x2, y2, z, 1);
                }
                if (i++ >= 15)
                    cancel();
            }
        }.runTaskTimer(SportsDay.getInstance(), 0L, 20L);
    }
}
