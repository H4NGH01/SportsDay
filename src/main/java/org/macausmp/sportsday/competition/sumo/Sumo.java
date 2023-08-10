package org.macausmp.sportsday.competition.sumo;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.PlayerData;
import org.macausmp.sportsday.SportsDay;
import org.macausmp.sportsday.competition.AbstractCompetition;
import org.macausmp.sportsday.competition.IRoundGame;
import org.macausmp.sportsday.util.Translation;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class Sumo extends AbstractCompetition implements IRoundGame {
    private final List<PlayerData> leaderboard = new ArrayList<>();
    private final List<PlayerData> alive = new ArrayList<>();
    private final List<PlayerData> queue = new ArrayList<>();
    private SumoStage sumoStage = SumoStage.ELIMINATE;
    private final Player[] grandFinal = new Player[2];
    private final Player[] thirdPlace = new Player[2];
    private final Player[] semiFinal = new Player[4];
    private final boolean weapon = SportsDay.getInstance().getConfig().getBoolean(getID() + ".enable_weapon");

    public Sumo() {
        super("sumo");
    }

    @Override
    public void onSetup() {
        alive.clear();
        alive.addAll(getPlayerDataList());
        queue.clear();
        queue.addAll(alive);
        sumoStage.resetStage();
        getOnlinePlayers().forEach(p -> p.sendMessage(Translation.translatable("competition.sumo.rule")));
        int stageRound;
        if (queue.size() <= 4) {
            sumoStage = SumoStage.SEMI_FINAL;
            stageRound = 2;
        } else if (queue.size() <= 8) {
            sumoStage = SumoStage.QUARTER_FINAL;
            stageRound = queue.size() - 4;
        } else {
            sumoStage = SumoStage.ELIMINATE;
            stageRound = queue.size() - 8;
        }
        for (int i = 0; i < stageRound; i++) {
            sumoStage.getRoundList().add(new SumoRound(getFromQueue(), getFromQueue()));
        }
        stageSetup();
    }

    private void stageSetup() {
        List<Component> cl = new ArrayList<>();
        cl.add(Translation.translatable("competition.sumo.current_stage").args(sumoStage.getName()));
        for (int i = 0; i < sumoStage.getRoundList().size();) {
            SumoRound r = sumoStage.getRoundList().get(i++);
            cl.add(Translation.translatable("competition.sumo.queue").args(Component.text(i), r.getPlayers().get(0).displayName(), r.getPlayers().get(1).displayName()));
        }
        getOnlinePlayers().forEach(p -> cl.forEach(p::sendMessage));
    }

    @Override
    public void onStart() {
        onRoundStart();
    }

    @Override
    public void onEnd(boolean force) {
        if (force) return;
        leaderboard.add(1, alive.get(1));
        leaderboard.add(0, alive.get(0));
        List<Component> cl = new ArrayList<>();
        for (int i = 0; i < leaderboard.size();) {
            PlayerData data = leaderboard.get(i++);
            cl.add(Translation.translatable("competition.rank").args(Component.text(i), Component.text(data.getName())));
            if (i <= 3) {
                data.addScore(4 - i);
            }
            data.addScore(1);
        }
        getOnlinePlayers().forEach(p -> cl.forEach(p::sendMessage));
    }

    @Override
    public <T extends Event> void onEvent(T event) {
        if (event instanceof PlayerMoveEvent e) {
            Player p = e.getPlayer();
            SumoRound round = sumoStage.getCurrentRound();
            if (round == null || !round.containPlayer(p)) return;
            if (round.getStatus() == SumoRound.RoundStatus.COMING) {
                e.setCancelled(true);
                return;
            }
            if (p.getLocation().getBlock().getType() != Material.WATER || round.getStatus() != SumoRound.RoundStatus.STARTED) return;
            round.setResult(round.getPlayers().get(0).equals(p) ? round.getPlayers().get(1) : round.getPlayers().get(0), p);
            onRoundEnd();
        }
    }

    private @NotNull Player getFromQueue() {
        PlayerData data = queue.get(new Random().nextInt(queue.size()));
        queue.remove(data);
        return data.getPlayer();
    }

    @Override
    public void onRoundStart() {
        if (sumoStage.getCurrentRound() != null) sumoStage.getCurrentRound().getPlayers().forEach(p -> p.teleport(getLocation()));
        sumoStage.nextRound();
        sumoStage.getCurrentRound().setStatus(SumoRound.RoundStatus.COMING);
        List<Player> pl = sumoStage.getCurrentRound().getPlayers();
        pl.get(0).teleport(Objects.requireNonNull(SportsDay.getInstance().getConfig().getLocation(getID() + ".p1-location")));
        pl.get(1).teleport(Objects.requireNonNull(SportsDay.getInstance().getConfig().getLocation(getID() + ".p2-location")));
        pl.forEach(p -> p.getInventory().clear());
        addRunnable(new BukkitRunnable() {
            int i = 5;
            @Override
            public void run() {
                if (i != 0) {
                    getOnlinePlayers().forEach(p -> p.sendActionBar(Translation.translatable("competition.sumo.round_start_countdown").args(Component.text(i)).color(NamedTextColor.YELLOW)));
                }
                if (i-- == 0) {
                    sumoStage.getCurrentRound().setStatus(SumoRound.RoundStatus.STARTED);
                    getOnlinePlayers().forEach(p -> p.sendActionBar(Translation.translatable("competition.sumo.round_start")));
                    giveWeapon();
                    cancel();
                }
            }
        }.runTaskTimer(SportsDay.getInstance(), 0L, 20L));
    }

    private void giveWeapon() {
        if (weapon) {
            addRunnable(new BukkitRunnable() {
                int i = 30;
                @Override
                public void run() {
                    if (sumoStage.getCurrentRound().getStatus() == SumoRound.RoundStatus.END) {
                        cancel();
                        return;
                    }
                    if (i <= 15 && i % 5 == 0 && i > 0) {
                        getOnlinePlayers().forEach(p -> p.sendActionBar(Translation.translatable("competition.sumo.knockback_stick_countdown").args(Component.text(i)).color(NamedTextColor.YELLOW)));
                    }
                    if (i-- == 0) {
                        sumoStage.getCurrentRound().getPlayers().forEach(p -> p.getInventory().setItem(EquipmentSlot.HAND, weapon()));
                        getOnlinePlayers().forEach(p -> p.sendActionBar(Translation.translatable("competition.sumo.knockback_stick_given")));
                        cancel();
                    }
                }
            }.runTaskTimer(SportsDay.getInstance(), 0L, 20L));
        }
    }

    private @NotNull ItemStack weapon() {
        ItemStack weapon = new ItemStack(Material.BLAZE_ROD);
        weapon.editMeta(meta -> {
            meta.displayName(Translation.translatable("item.sportsday.kb_stick"));
            meta.addEnchant(Enchantment.KNOCKBACK, 1, false);
            meta.addEnchant(Enchantment.BINDING_CURSE, 1, false);
        });
        return weapon;
    }

    @Override
    public void onRoundEnd() {
        SumoRound round = sumoStage.getCurrentRound();
        getWorld().strikeLightningEffect(round.getLoser().getLocation());
        getOnlinePlayers().forEach(p -> {
            p.sendActionBar(Translation.translatable("competition.sumo.round_end"));
            p.sendMessage(Translation.translatable("competition.sumo.round_winner").args(round.getWinner().displayName()).color(NamedTextColor.YELLOW));
        });
        if (sumoStage != SumoStage.SEMI_FINAL) {
            for (PlayerData data : alive) {
                if (data.getUUID().equals(round.getLoser().getUniqueId())) {
                    leaderboard.add(0, data);
                    alive.remove(data);
                    break;
                }
            }
        }
        // After sumo stage
        if (sumoStage == SumoStage.SEMI_FINAL) {
            grandFinal[sumoStage.getRoundIndex() - 1] = round.getWinner();
            thirdPlace[sumoStage.getRoundIndex() - 1] = round.getLoser();
        } else if (sumoStage == SumoStage.QUARTER_FINAL) {
            semiFinal[sumoStage.getRoundIndex() - 1] = round.getWinner();
        }
        if (sumoStage.getRoundRemaining() != 0) {
            nextRound();
        } else {
            if (sumoStage != SumoStage.FINAL) {
                nextSumoStage();
            } else {
                end(false);
            }
        }
    }

    @Override
    public void nextRound() {
        addRunnable(new BukkitRunnable() {
            int i = 5;
            @Override
            public void run() {
                getOnlinePlayers().forEach(p -> p.sendActionBar(Translation.translatable("competition.sumo.next_round_countdown").args(Component.text(i)).color(NamedTextColor.GREEN)));
                if (i-- == 0) {
                    onRoundStart();
                    cancel();
                }
            }
        }.runTaskTimer(SportsDay.getInstance(), 0L, 20L));
    }

    private void nextSumoStage() {
        if (alive.size() <= 8 && sumoStage.hasNextStage()) {
            sumoStage = sumoStage.getNextStage();
        }
        // Before sumo stage
        if (sumoStage == SumoStage.FINAL) {
            sumoStage.getRoundList().add(new SumoRound(grandFinal[0], grandFinal[1]));
        } else if (sumoStage == SumoStage.THIRD_PLACE) {
            sumoStage.getRoundList().add(new SumoRound(thirdPlace[0], thirdPlace[1]));
        } else if (sumoStage == SumoStage.SEMI_FINAL) {
            for (int i = 0; i < alive.size(); i++) {
                sumoStage.getRoundList().add(new SumoRound(semiFinal[i * 2], semiFinal[i * 2 + 1]));
            }
        } else if (sumoStage == SumoStage.QUARTER_FINAL) {
            for (int i = 0; i < alive.size(); i++) {
                sumoStage.getRoundList().add(new SumoRound(getFromQueue(), getFromQueue()));
            }
        } else {
            for (int i = 0; i < alive.size() - 8; i++) {
                sumoStage.getRoundList().add(new SumoRound(getFromQueue(), getFromQueue()));
            }
        }
        stageSetup();
        addRunnable(new BukkitRunnable() {
            int i = 10;
            @Override
            public void run() {
                if (i == 7 || (i <= 5 && i > 0)) {
                    getOnlinePlayers().forEach(p -> p.sendActionBar(Translation.translatable("competition.sumo.next_stage_countdown").args(Component.text(i)).color(NamedTextColor.GREEN)));
                }
                if (i-- == 0) {
                    nextRound();
                    cancel();
                }
            }
        }.runTaskTimer(SportsDay.getInstance(), 0L, 20L));
    }

    public SumoStage getSumoStage() {
        return sumoStage;
    }

    @Override
    public List<PlayerData> getLeaderboard() {
        return leaderboard;
    }
}
