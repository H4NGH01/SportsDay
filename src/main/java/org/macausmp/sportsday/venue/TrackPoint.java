package org.macausmp.sportsday.venue;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;

public class TrackPoint {
    private Location location;
    private BoundingBox boundingBox;

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

    public BoundingBox getBoundingBox() {
        return boundingBox;
    }

    public void setBoundingBox(BoundingBox boundingBox) {
        this.boundingBox = boundingBox;
    }

    public boolean overlaps(@NotNull Entity entity) {
        return boundingBox.overlaps(entity.getBoundingBox());
    }
}
