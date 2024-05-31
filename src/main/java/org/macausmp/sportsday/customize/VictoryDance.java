package org.macausmp.sportsday.customize;

import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.macausmp.sportsday.SportsDay;
import org.macausmp.sportsday.util.TextUtil;

import java.util.Random;
import java.util.function.Consumer;

public enum VictoryDance {
    DRAGON_RIDER("customize.victory_dance.dragon_rider", Material.DRAGON_HEAD, p -> {
        p.setGameMode(GameMode.ADVENTURE);
        EnderDragon dragon = p.getWorld().spawn(p.getLocation(), EnderDragon.class);
        dragon.setInvulnerable(true);
        new BukkitRunnable() {
            int i = 0;
            @Override
            public void run() {
                dragon.addPassenger(p);
                dragon.setPhase(EnderDragon.Phase.FLY_TO_PORTAL);
                dragon.setPhase(EnderDragon.Phase.LAND_ON_PORTAL);
                dragon.setPodium(p.getLocation().add(p.getLocation().getDirection().multiply(50)));
                dragon.setVelocity(p.getLocation().getDirection());
                if (i++ > 197) {
                    dragon.remove();
                    cancel();
                }
            }
        }.runTaskTimer(SportsDay.getInstance(), 0, 1);
    }),
    THUNDERSTORM("customize.victory_dance.thunderstorm", Material.LIGHTNING_ROD, p -> new BukkitRunnable() {
        int i = 0;
        final Location loc = p.getLocation();
        final World world = loc.getWorld();
        final Random random = new Random();
        @Override
        public void run() {
            Bukkit.getOnlinePlayers().forEach(player -> player.setPlayerWeather(WeatherType.DOWNFALL));
            for (int i = 0; i < 5; i++) {
                int x = (int) (loc.x() + 64 - random.nextInt(128));
                int z = (int) (loc.z() + 64 - random.nextInt(128));
                int y = world.getHighestBlockAt(x, z).getY();
                world.strikeLightningEffect(new Location(world, x, y, z));
            }
            if (i++ > 200) {
                Bukkit.getOnlinePlayers().forEach(Player::resetPlayerWeather);
                cancel();
            }
        }
    }.runTaskTimer(SportsDay.getInstance(), 0, 1)),
    MADE_IN_HEAVEN("customize.victory_dance.made_in_heaven", Material.CLOCK, p -> new BukkitRunnable() {
        int i = 0;
        final World world = p.getWorld();
        final long ft = world.getFullTime();
        final boolean cycle = Boolean.TRUE.equals(world.getGameRuleValue(GameRule.DO_DAYLIGHT_CYCLE));
        @Override
        public void run() {
            world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, true);
            world.setFullTime(ft - i * 100L);
            if (i++ > 200) {
                world.setFullTime(ft);
                world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, cycle);
                cancel();
            }
        }
    }.runTaskTimer(SportsDay.getInstance(), 0, 1));

    private final Component name;
    private final Material material;
    private final Consumer<Player> animation;

    VictoryDance(String code, Material material, Consumer<Player> animation) {
        this.name = TextUtil.text(Component.translatable(code));
        this.material = material;
        this.animation = animation;
    }

    public Component getName() {
        return name;
    }

    public Material getMaterial() {
        return material;
    }

    public void play(Player player) {
        animation.accept(player);
        //animation.run(player);
    }

    public void stop() {
        //animation.stop();
    }

    private abstract static class Animation {
        protected abstract void run(Player player);

        protected abstract void stop();
    }
}
