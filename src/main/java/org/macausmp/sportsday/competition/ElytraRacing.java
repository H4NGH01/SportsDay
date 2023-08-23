package org.macausmp.sportsday.competition;

import com.destroystokyo.paper.event.player.PlayerElytraBoostEvent;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.jetbrains.annotations.NotNull;

public class ElytraRacing extends AbstractTrackEvent {
    public ElytraRacing() {
        super("elytra_racing");
    }

    @Override
    public void onSetup() {
        ItemStack elytra = new ItemStack(Material.ELYTRA);
        elytra.editMeta(meta -> {
            meta.addEnchant(Enchantment.BINDING_CURSE, 1, false);
            meta.setUnbreakable(true);
        });
        Competitions.getOnlinePlayers().forEach(d -> d.getPlayer().getInventory().setItem(EquipmentSlot.CHEST, elytra));
    }

    @Override
    public void onStart() {
        ItemStack firework = new ItemStack(Material.FIREWORK_ROCKET);
        firework.setAmount(64);
        firework.editMeta(FireworkMeta.class, meta -> meta.setPower(3));
        Competitions.getOnlinePlayers().forEach(d -> d.getPlayer().getInventory().setItem(0, firework));
    }

    @Override
    protected void onEnd(boolean force) {
    }

    @EventHandler
    public void onElytraBoost(@NotNull PlayerElytraBoostEvent e) {
        if (Competitions.getCurrentlyCompetition() == null || Competitions.getCurrentlyCompetition() != this || getStage() != Stage.STARTED) return;
        Player p = e.getPlayer();
        if (!Competitions.containPlayer(p)) return;
        e.setShouldConsume(false);
    }

    @EventHandler
    public void onUseFirework(@NotNull PlayerInteractEvent e) {
        if (Competitions.getCurrentlyCompetition() == null || Competitions.getCurrentlyCompetition() != this || getStage() != Stage.STARTED) return;
        Player p = e.getPlayer();
        if (!Competitions.containPlayer(p)) return;
        if (e.getAction() == Action.RIGHT_CLICK_BLOCK && e.getItem() != null && e.getItem().getType() == Material.FIREWORK_ROCKET) e.setCancelled(true);
    }

    @EventHandler
    public void onDropFirework(@NotNull PlayerDropItemEvent e) {
        if (Competitions.getCurrentlyCompetition() == null || Competitions.getCurrentlyCompetition() != this) return;
        Player p = e.getPlayer();
        if (!Competitions.containPlayer(p)) return;
        if (e.getItemDrop().getItemStack().getType() == Material.FIREWORK_ROCKET) e.setCancelled(true);
    }
}
