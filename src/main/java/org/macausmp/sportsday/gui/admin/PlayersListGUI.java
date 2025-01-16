package org.macausmp.sportsday.gui.admin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.ContestantData;
import org.macausmp.sportsday.SportsDay;
import org.macausmp.sportsday.gui.*;
import org.macausmp.sportsday.training.SportsTrainingHandler;
import org.macausmp.sportsday.util.ItemUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@PermissionRequired
public class PlayersListGUI extends PluginGUI {
    private final Scoreboard scoreboard = PLUGIN.getServer().getScoreboardManager().getMainScoreboard();
    private final Filter<OfflinePlayer> roleFilter = new Filter<>(
            new String[]{
                    "gui.players_list.filter.all",
                    "gui.players_list.filter.contestants",
                    "gui.players_list.filter.referees",
                    "gui.players_list.filter.audiences"
            },
            (player, count) -> switch (count) {
                case 0 -> true;
                case 1 -> SportsDay.CONTESTANTS.hasPlayer(player);
                case 2 -> SportsDay.REFEREES.hasPlayer(player);
                case 3 -> SportsDay.AUDIENCES.hasPlayer(player);
                default -> false;
            }
    );
    private final PageBox<OfflinePlayer> pageBox = new PageBox<>(this, 0, 45,
            () -> Arrays.stream(PLUGIN.getServer().getOfflinePlayers()).toList(), roleFilter);

    public PlayersListGUI() {
        super(54, Component.translatable("gui.players_list.title"));
        for (int i = 45; i < 54; i++)
            getInventory().setItem(i, BOARD);
        getInventory().setItem(53, BACK);
        getInventory().setItem(48, PREVIOUS_PAGE);
        getInventory().setItem(50, NEXT_PAGE);
        update();
    }

    @Override
    public void update() {
        getInventory().setItem(49, pages());
        pageBox.updatePage(this::icon);
        getInventory().setItem(45, roleFilter.filterItem(Material.HOPPER, "role_filter"));
    }

    public static void updateGUI() {
        PLUGIN.getServer().getOnlinePlayers().stream().map(p -> p.getOpenInventory().getTopInventory())
                .filter(inv -> inv.getHolder() instanceof PlayersListGUI)
                .map(inv -> (PlayersListGUI) inv.getHolder())
                .forEach(PlayersListGUI::update);
    }

    private @NotNull ItemStack pages() {
        ItemStack stack = new ItemStack(Material.PLAYER_HEAD);
        stack.editMeta(meta -> meta.displayName(Component.translatable("book.pageIndicator")
                .arguments(Component.text(pageBox.getPage() + 1), Component.text(pageBox.getMaxPage()))
                .decoration(TextDecoration.ITALIC, false)));
        return stack;
    }

    private @NotNull ItemStack icon(@NotNull OfflinePlayer player) {
        boolean online = player.isOnline();
        Component display = Component.translatable(Objects.requireNonNull(player.getName()) + " (%s)")
                .arguments(Component.translatable(online ? "player.online" : "player.offline"));
        List<Component> lore = new ArrayList<>();
        Team team = scoreboard.getPlayerTeam(player);
        lore.add(team != null ? team.displayName().color(team.color()) : Component.translatable("gui.text.none"));
        if (online) {
            Component status;
            if (SportsTrainingHandler.getTrainingSport(player.getUniqueId()) != null) {
                status = Component.translatable("player.status.training");
            } else if (SportsDay.getCurrentEvent() != null && SportsDay.getCurrentEvent().getContestants()
                    .contains(SportsDay.getContestant(player.getUniqueId()))) {
                status = Component.translatable("player.status.competition");
            } else {
                status = Component.translatable("player.status.free");
            }
            lore.add(Component.translatable("player.status").arguments(status));
        }
        if (SportsDay.isContestant(player)) {
            ContestantData data = SportsDay.getContestant(player.getUniqueId());
            lore.add(Component.translatable("contestant.number")
                    .arguments(Component.text(data.getNumber()))
                    .color(NamedTextColor.YELLOW));
            lore.add(Component.translatable("contestant.score")
                    .arguments(Component.text(data.getScore()))
                    .color(NamedTextColor.YELLOW));
        }
        lore.add(Component.text(""));
        lore.add(Component.translatable("gui.players_list.player_profile")
                .arguments(Component.text(player.getName())).color(NamedTextColor.YELLOW));
        ItemStack icon = ItemUtil.item(Material.PLAYER_HEAD, "player_icon", display, lore.toArray());
        icon.editMeta(SkullMeta.class, meta -> meta.setOwningPlayer(player));
        return icon;
    }

    @ButtonHandler("player_icon")
    public void profile(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        OfflinePlayer player = Objects.requireNonNull(((SkullMeta) item.getItemMeta()).getOwningPlayer());
        p.openInventory(new PlayerProfileGUI(player).getInventory());
        p.playSound(UI_BUTTON_CLICK_SOUND);
    }

    @ButtonHandler("next_page")
    public void next(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        pageBox.nextPage();
        p.playSound(UI_BUTTON_CLICK_SOUND);
    }

    @ButtonHandler("prev_page")
    public void prev(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        pageBox.previousPage();
        p.playSound(UI_BUTTON_CLICK_SOUND);
    }

    @ButtonHandler("role_filter")
    public void roleFilter(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        p.playSound(UI_BUTTON_CLICK_SOUND);
        roleFilter.next();
        update();
    }

    @ButtonHandler("back")
    public void back(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        p.openInventory(new AdminMenuGUI().getInventory());
        p.playSound(UI_BUTTON_CLICK_SOUND);
    }
}
