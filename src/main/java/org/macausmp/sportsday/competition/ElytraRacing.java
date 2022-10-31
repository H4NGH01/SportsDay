package org.macausmp.sportsday.competition;

import com.destroystokyo.paper.event.player.PlayerElytraBoostEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.PlayerData;

public class ElytraRacing extends AbstractCompetition {
    private final Leaderboard<PlayerData> leaderboard = new Leaderboard<>();

    @Override
    public String getID() {
        return "elytra_racing";
    }

    @Override
    public void onSetup() {
        ItemStack elytra = new ItemStack(Material.ELYTRA);
        elytra.editMeta(meta -> {
            meta.addEnchant(Enchantment.BINDING_CURSE, 1, false);
            meta.setUnbreakable(true);
        });
        getPlayerDataList().forEach(data -> {
            if (data.isPlayerOnline()) {
                data.getPlayer().getInventory().setItem(EquipmentSlot.CHEST, elytra);
            }
        });
    }
    @Override
    public void onStart() {
        ItemStack firework = new ItemStack(Material.FIREWORK_ROCKET);
        firework.setAmount(64);
        firework.editMeta(FireworkMeta.class, meta -> meta.setPower(3));
        getPlayerDataList().forEach(data -> {
            if (data.isPlayerOnline()) {
                Player p = data.getPlayer();
                p.getInventory().setHeldItemSlot(0);
                p.getInventory().setItem(EquipmentSlot.HAND, firework);
            }
        });
    }

    @Override
    public void onEnd(boolean force) {
        if (force) return;
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (PlayerData data : getLeaderboard().getEntry()) {
            sb.append("第").append(++i).append("名 ").append(data.getName()).append("\n");
        }
        getOnlinePlayers().forEach(p -> p.sendMessage(Component.text(sb.substring(0, sb.length() - 1))));
    }

    @EventHandler
    public void onUseFirework(@NotNull PlayerElytraBoostEvent e) {
        if (Competitions.getCurrentlyCompetition() == null || !Competitions.getCurrentlyCompetition().equals(this) || !getStage().equals(Stage.STARTED)) return;
        Player p = e.getPlayer();
        if (!Competitions.containPlayer(p)) return;
        e.setShouldConsume(false);
    }

    @EventHandler
    public void onUseFirework(@NotNull PlayerInteractEvent e) {
        if (Competitions.getCurrentlyCompetition() == null || !Competitions.getCurrentlyCompetition().equals(this) || !getStage().equals(Stage.STARTED)) return;
        Player p = e.getPlayer();
        if (!Competitions.containPlayer(p)) return;
        if (e.getAction().equals(Action.RIGHT_CLICK_BLOCK) && e.getItem() != null && e.getItem().getType().equals(Material.FIREWORK_ROCKET)) {
            e.setCancelled(true);
        }
    }

    @Override
    public <T extends Event> void onEvent(T event) {
        if (event instanceof PlayerMoveEvent e) {
            Player player = e.getPlayer();
            if (getLeaderboard().getEntry().contains(Competitions.getPlayerData(player.getUniqueId()))) return;
            Location loc = player.getLocation().clone();
            loc.setY(loc.getY() - 0.5f);
            if (loc.getBlock().getType().equals(CompetitionListener.FINISH_LINE)) {
                getLeaderboard().getEntry().add(Competitions.getPlayerData(player.getUniqueId()));
                player.playSound(player, Sound.ENTITY_ARROW_HIT_PLAYER, 1f, 1f);
                player.setGameMode(GameMode.SPECTATOR);
                getOnlinePlayers().forEach(p -> p.sendMessage(Component.text(player.getName() + "已成了比賽").color(NamedTextColor.YELLOW)));
                if (getLeaderboard().getEntry().size() >= 3) {
                    end(false);
                }
            }
        }
    }

    @Override
    public Leaderboard<PlayerData> getLeaderboard() {
        return leaderboard;
    }
}
