package org.macausmp.sportsday.venue;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.util.BoundingBoxDataType;
import org.macausmp.sportsday.util.LocationDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class Track extends Venue {
    public static final TrackDataType TRACK_DATA_TYPE = new TrackDataType();
    private static final TrackPointDataType TRACK_POINT_DATA_TYPE = new TrackPointDataType();
    private TrackPoint start;
    private TrackPoint end;
    private final List<TrackPoint> checkpoints = new ArrayList<>();

    public Track(@NotNull VenueType<? extends Track> type, @NotNull UUID uuid, @NotNull String name, @NotNull Location location) {
        super(type, uuid, name, location);
        this.start = new TrackPoint(location, new BoundingBox());
        this.end = new TrackPoint(location, new BoundingBox());
    }

    public TrackPoint getStartPoint() {
        return start;
    }

    public TrackPoint getEndPoint() {
        return end;
    }

    public List<TrackPoint> getCheckPoints() {
        return checkpoints;
    }

    public static class TrackDataType implements PersistentDataType<PersistentDataContainer, Track> {
        private TrackDataType() {}

        @Override
        public @NotNull Class<PersistentDataContainer> getPrimitiveType() {
            return PersistentDataContainer.class;
        }

        @Override
        public @NotNull Class<Track> getComplexType() {
            return Track.class;
        }

        @Override
        public @NotNull PersistentDataContainer toPrimitive(@NotNull Track complex, @NotNull PersistentDataAdapterContext context) {
            PersistentDataContainer container = context.newPersistentDataContainer();
            container.set(new NamespacedKey(PLUGIN, "uuid"), STRING, complex.getUUID().toString());
            container.set(new NamespacedKey(PLUGIN, "name"), STRING, complex.getName());
            container.set(new NamespacedKey(PLUGIN, "item"), STRING, complex.getItem().name());
            container.set(new NamespacedKey(PLUGIN, "location"), LocationDataType.LOCATION_DATA_TYPE, complex.getLocation());
            container.set(new NamespacedKey(PLUGIN, "start"), TRACK_POINT_DATA_TYPE, complex.start);
            container.set(new NamespacedKey(PLUGIN, "end"), TRACK_POINT_DATA_TYPE, complex.end);
            container.set(new NamespacedKey(PLUGIN, "checkpoints"), LIST.listTypeFrom(TRACK_POINT_DATA_TYPE), complex.checkpoints);
            return container;
        }

        @Override
        public @NotNull Track fromPrimitive(@NotNull PersistentDataContainer primitive, @NotNull PersistentDataAdapterContext context) {
            UUID uuid = UUID.fromString(Objects.requireNonNull(primitive.get(new NamespacedKey(PLUGIN, "uuid"), STRING)));
            String name = Objects.requireNonNull(primitive.get(new NamespacedKey(PLUGIN, "name"), STRING));
            Material item = Material.getMaterial(Objects.requireNonNull(primitive.get(new NamespacedKey(PLUGIN, "item"), STRING)));
            Location location = Objects.requireNonNull(primitive.get(new NamespacedKey(PLUGIN, "location"), LocationDataType.LOCATION_DATA_TYPE));
            Track track = new Track(VenueType.TRACK, uuid, name, location);
            track.setItem(Objects.requireNonNull(item));
            track.start = Objects.requireNonNull(primitive.get(new NamespacedKey(PLUGIN, "start"), TRACK_POINT_DATA_TYPE));
            track.end = Objects.requireNonNull(primitive.get(new NamespacedKey(PLUGIN, "end"), TRACK_POINT_DATA_TYPE));
            track.checkpoints.addAll(Objects.requireNonNull(primitive.get(new NamespacedKey(PLUGIN, "checkpoints"), LIST.listTypeFrom(TRACK_POINT_DATA_TYPE))));
            return track;
        }
    }

    private static class TrackPointDataType implements PersistentDataType<PersistentDataContainer, TrackPoint> {
        @Override
        public @NotNull Class<PersistentDataContainer> getPrimitiveType() {
            return PersistentDataContainer.class;
        }

        @Override
        public @NotNull Class<TrackPoint> getComplexType() {
            return TrackPoint.class;
        }

        @Override
        public @NotNull PersistentDataContainer toPrimitive(@NotNull TrackPoint complex, @NotNull PersistentDataAdapterContext context) {
            PersistentDataContainer container = context.newPersistentDataContainer();
            container.set(new NamespacedKey(PLUGIN, "location"), LocationDataType.LOCATION_DATA_TYPE, complex.getLocation());
            container.set(new NamespacedKey(PLUGIN, "boundingBox"), BoundingBoxDataType.BOUNDING_BOX_DATA_TYPE, complex.getBoundingBox());
            return container;
        }

        @Override
        public @NotNull TrackPoint fromPrimitive(@NotNull PersistentDataContainer primitive, @NotNull PersistentDataAdapterContext context) {
            Location location = Objects.requireNonNull(primitive.get(new NamespacedKey(PLUGIN, "location"), LocationDataType.LOCATION_DATA_TYPE));
            BoundingBox boundingBox = Objects.requireNonNull(primitive.get(new NamespacedKey(PLUGIN, "boundingBox"), BoundingBoxDataType.BOUNDING_BOX_DATA_TYPE));
            return new TrackPoint(location, boundingBox);
        }
    }
}
