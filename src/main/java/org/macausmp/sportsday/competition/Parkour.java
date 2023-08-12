package org.macausmp.sportsday.competition;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.SportsDay;
import org.macausmp.sportsday.event.PlayerFinishCompetitionEvent;

public class Parkour extends AbstractTrackCompetition {
    public Parkour() {
        super("parkour");
    }

    @Override
    public void onSetup() {
        getPlayerDataList().forEach(data -> data.getPlayer().setCollidable(false));
    }

    @Override
    public void onStart() {
        ItemStack boots = new ItemStack(Material.LEATHER_BOOTS);
        boots.editMeta(meta -> {
            meta.addEnchant(Enchantment.BINDING_CURSE, 1, false);
            meta.setUnbreakable(true);
        });
        getPlayerDataList().forEach(data -> {
            if (data.isPlayerOnline()) {
                data.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 0, false, false, false));
                data.getPlayer().getInventory().setItem(EquipmentSlot.FEET, boots);
            }
        });
    }

    @Override
    public void onEnd(boolean force) {
        getPlayerDataList().forEach(data -> {
            if (data.isPlayerOnline()) {
                data.getPlayer().setCollidable(true);
                data.getPlayer().removePotionEffect(PotionEffectType.INVISIBILITY);
            }
        });
    }

    @EventHandler
    public void onFinished(@NotNull PlayerFinishCompetitionEvent e) {
        if (e.getCompetition() != this) return;
        e.getPlayer().removePotionEffect(PotionEffectType.INVISIBILITY);
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {
        if (Competitions.getCurrentlyCompetition() == null || Competitions.getCurrentlyCompetition() != this || getStage() != Stage.STARTED) return;
        Player p = e.getPlayer();
        if (getLeaderboard().contains(Competitions.getPlayerData(p.getUniqueId()))) return;
        addRunnable(new BukkitRunnable() {
            @Override
            public void run() {
                p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 0, false, false, false));
                cancel();
            }
        }.runTaskLater(SportsDay.getInstance(), 5L));
    }
}
