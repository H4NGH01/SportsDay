package org.macausmp.sportsday.util;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

@SuppressWarnings("SpellCheckingInspection")
public final class SkullTextureUtil {
    public static final String START = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMmEzYjhmNjgxZGFhZDhiZjQzNmNhZThkYTNmZTgxMzFmNjJhMTYyYWI4MWFmNjM5YzNlMDY0NGFhNmFiYWMyZiJ9fX0";

    /**
     * Get custom texture player head
     * @param value Profile textures value
     * @return Player head with custom texture
     */
    public static @NotNull ItemStack getSkull(String value) {
        ItemStack stack = new ItemStack(Material.PLAYER_HEAD);
        if (value == null || value.isEmpty()) return stack;
        stack.editMeta(SkullMeta.class, meta -> {
            PlayerProfile profile = Bukkit.createProfile(UUID.randomUUID());
            profile.getProperties().add(new ProfileProperty("textures", value));
            meta.setPlayerProfile(profile);
        });
        return stack;
    }
}
