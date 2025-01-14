package org.macausmp.sportsday.sport;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.SportsDay;
import org.macausmp.sportsday.SportsRegistry;
import org.macausmp.sportsday.util.TextUtil;
import org.macausmp.sportsday.venue.VenueType;

public class SportType implements Keyed, ComponentLike {
    private static final SportsDay PLUGIN = SportsDay.getInstance();
    public static final SportType ATHLETICS = register("athletics", VenueType.VENUE);
    public static final SportType AIR_SPORT = register("air_sport", VenueType.TRACK);
    public static final SportType MOTO_SPORT = register("moto_sport", VenueType.TRACK);
    public static final SportType PARKOUR = register("parkour", VenueType.TRACK);
    public static final SportType COMBAT = register("combat", VenueType.COMBAT_VENUE);

    private static @NotNull SportType register(@NotNull String id, @NotNull VenueType<?> venueType) {
        SportType type = new SportType(venueType, id);
        SportsRegistry.SPORT_TYPE.add(new NamespacedKey(PLUGIN, id), type);
        return type;
    }

    private final VenueType<?> venueType;
    private final Component name;

    private SportType(@NotNull VenueType<?> venueType, @NotNull String id) {
        this.venueType = venueType;
        this.name = TextUtil.convert(Component.translatable("sport.type." + id));
    }

    @Override
    public @NotNull NamespacedKey getKey() {
        return SportsRegistry.SPORT_TYPE.getKeyOrThrow(this);
    }

    @Override
    public @NotNull Component asComponent() {
        return name;
    }

    public VenueType<?> getVenueType() {
        return venueType;
    }
}
