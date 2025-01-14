package org.macausmp.sportsday.util;

import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.SportsDay;

import java.util.Objects;

public class BoundingBoxDataType implements PersistentDataType<PersistentDataContainer, BoundingBox> {
    private static final SportsDay PLUGIN = SportsDay.getInstance();
    public static final BoundingBoxDataType BOUNDING_BOX_DATA_TYPE = new BoundingBoxDataType();

    private BoundingBoxDataType() {}

    @Override
    public @NotNull Class<PersistentDataContainer> getPrimitiveType() {
        return PersistentDataContainer.class;
    }

    @Override
    public @NotNull Class<BoundingBox> getComplexType() {
        return BoundingBox.class;
    }

    @Override
    public @NotNull PersistentDataContainer toPrimitive(@NotNull BoundingBox complex, @NotNull PersistentDataAdapterContext context) {
        PersistentDataContainer container = context.newPersistentDataContainer();
        container.set(new NamespacedKey(PLUGIN, "minX"), DOUBLE, complex.getMinX());
        container.set(new NamespacedKey(PLUGIN, "minY"), DOUBLE, complex.getMinY());
        container.set(new NamespacedKey(PLUGIN, "minZ"), DOUBLE, complex.getMinZ());
        container.set(new NamespacedKey(PLUGIN, "maxX"), DOUBLE, complex.getMaxX());
        container.set(new NamespacedKey(PLUGIN, "maxY"), DOUBLE, complex.getMaxY());
        container.set(new NamespacedKey(PLUGIN, "maxZ"), DOUBLE, complex.getMaxZ());
        return container;
    }

    @Override
    public @NotNull BoundingBox fromPrimitive(@NotNull PersistentDataContainer primitive, @NotNull PersistentDataAdapterContext context) {
        double minX = Objects.requireNonNull(primitive.get(new NamespacedKey(PLUGIN, "minX"), DOUBLE));
        double minY = Objects.requireNonNull(primitive.get(new NamespacedKey(PLUGIN, "minY"), DOUBLE));
        double minZ = Objects.requireNonNull(primitive.get(new NamespacedKey(PLUGIN, "minZ"), DOUBLE));
        double maxX = Objects.requireNonNull(primitive.get(new NamespacedKey(PLUGIN, "maxX"), DOUBLE));
        double maxY = Objects.requireNonNull(primitive.get(new NamespacedKey(PLUGIN, "maxY"), DOUBLE));
        double maxZ = Objects.requireNonNull(primitive.get(new NamespacedKey(PLUGIN, "maxZ"), DOUBLE));
        return new BoundingBox(minX, minY, minZ, maxX, maxY, maxZ);
    }
}
