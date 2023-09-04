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
    private static final ItemStack ELYTRA = elytra();
    private static final ItemStack FIREWORK = firework();

    public ElytraRacing() {
        super("elytra_racing");
    }

    @Override
    protected void onSetup() {
        Competitions.getOnlineCompetitors().forEach(d -> d.getPlayer().getInventory().setItem(EquipmentSlot.CHEST, ELYTRA));
    }

    @Override
    protected void onStart() {
        Competitions.getOnlineCompetitors().forEach(d -> d.getPlayer().getInventory().setItem(0, FIREWORK));
    }

    @Override
    protected void onEnd(boolean force) {
    }

    @Override
    protected void onPractice(@NotNull Player p) {
        p.getInventory().setItem(EquipmentSlot.CHEST, ELYTRA);
        p.getInventory().setItem(0, FIREWORK);
    }

    @EventHandler
    public void onElytraBoost(@NotNull PlayerElytraBoostEvent e) {
        e.setShouldConsume(false);
    }

    @EventHandler
    public void onUseFirework(@NotNull PlayerInteractEvent e) {
        IEvent event = Competitions.getCurrentEvent();
        Player p = e.getPlayer();
        if ((event == this && getStage() == Stage.STARTED && Competitions.containPlayer(p)) || inPractice(p, this)) {
            if (e.getAction() == Action.RIGHT_CLICK_BLOCK && e.getItem() != null && e.getItem().getType() == Material.FIREWORK_ROCKET) e.setCancelled(true);
        }
    }

    @EventHandler
    public void onDropFirework(@NotNull PlayerDropItemEvent e) {
        if (e.getItemDrop().getItemStack().getType() == Material.FIREWORK_ROCKET) e.setCancelled(true);
    }

    private static @NotNull ItemStack elytra() {
        ItemStack elytra = new ItemStack(Material.ELYTRA);
        elytra.editMeta(meta -> {
            meta.addEnchant(Enchantment.BINDING_CURSE, 1, false);
            meta.setUnbreakable(true);
        });
        return elytra;
    }

    private static @NotNull ItemStack firework() {
        ItemStack firework = new ItemStack(Material.FIREWORK_ROCKET);
        firework.setAmount(64);
        firework.editMeta(FireworkMeta.class, meta -> meta.setPower(3));
        return firework;
    }
}
