package org.macausmp.sportsday.competition;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffectType;
import org.macausmp.sportsday.PlayerData;

import java.util.HashMap;

public class ObstacleCourse extends AbstractCompetition {
    private final Leaderboard<PlayerData> leaderboard = new Leaderboard<>();
    private final HashMap<PlayerData, Integer> lapMap = new HashMap<>();

    @Override
    public String getID() {
        return "obstacle_course";
    }

    @Override
    public void onSetup() {
        getPlayerDataList().forEach(data -> {
            if (data.isPlayerOnline()) {
                lapMap.put(data, 0);
                data.getPlayer().setCollidable(false);
            }
        });
    }

    @Override
    public void onStart() {

    }

    @Override
    public void onEnd(boolean force) {
        getPlayerDataList().forEach(data -> {
            if (data.isPlayerOnline()) {
                data.getPlayer().setCollidable(true);
                data.getPlayer().removePotionEffect(PotionEffectType.INVISIBILITY);
            }
        });
        if (force) return;
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (PlayerData data : getLeaderboard().getEntry()) {
            sb.append("第").append(++i).append("名 ").append(data.getName()).append("\n");
        }
        getOnlinePlayers().forEach(p -> p.sendMessage(Component.text(sb.substring(0, sb.length() - 1))));
    }

    @Override
    public <T extends Event> void onEvent(T event) {
        if (event instanceof PlayerMoveEvent e) {
            Player player = e.getPlayer();
            if (getLeaderboard().getEntry().contains(Competitions.getPlayerData(player.getUniqueId()))) return;
            Location loc = player.getLocation().clone();
            loc.setY(loc.getY() - 0.5f);
            CompetitionListener.spawnpoint(player, loc);
            if (loc.getBlock().getType().equals(CompetitionListener.FINISH_LINE)) {
                PlayerData data = Competitions.getPlayerData(player.getUniqueId());
                lapMap.put(data, lapMap.get(data) + 1);
                player.playSound(player, Sound.ENTITY_ARROW_HIT_PLAYER, 1f, 1f);
                player.teleport(getLocation());
                if (lapMap.get(data) == 1) {
                    getOnlinePlayers().forEach(p -> p.sendMessage(Component.text(player.getName() + "已成了第一圈").color(NamedTextColor.YELLOW)));
                } else if (lapMap.get(data) >= 2) {
                    getLeaderboard().getEntry().add(Competitions.getPlayerData(player.getUniqueId()));
                    getOnlinePlayers().forEach(p -> p.sendMessage(Component.text(player.getName() + "已成了比賽").color(NamedTextColor.YELLOW)));
                    if (getLeaderboard().getEntry().size() >= 3) {
                        end(false);
                    }
                }
            }
        }
    }

    @Override
    public Leaderboard<PlayerData> getLeaderboard() {
        return leaderboard;
    }
}
