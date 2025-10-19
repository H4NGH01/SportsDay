package org.macausmp.sportsday.replay;

import org.bukkit.World;
import org.bukkit.event.Event;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.FileUtil;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.SportsDay;

import java.io.File;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Replay {
    private static final SportsDay PLUGIN = SportsDay.getInstance();
    private final Set<UUID> players = new HashSet<>();
    private final List<Tick> ticks = new ArrayList<>();
    private int tick = 0;

    public Replay(@NotNull World world) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH-mm-ss").withZone(ZoneId.systemDefault());
        File file = new File(PLUGIN.getDataFolder() + "/replays", formatter.format(Instant.now()));
        if (!file.exists() && file.mkdirs()) {
            FileUtil.copy(world.getWorldFolder(), file);
        }
    }

    public void record(@NotNull Tick tick) {
        ticks.add(tick);
    }

    public void play() {
        new BukkitRunnable() {
            @Override
            public void run() {
                Tick t = ticks.get(tick);
                for (Event e : t.getActions()) {
                    continue;
                }
                if (++tick > ticks.size()) {
                    cancel();
                }
            }
        }.runTaskTimer(PLUGIN, 0L, 1L);
    }
}
