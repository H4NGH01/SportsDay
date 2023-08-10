package org.macausmp.sportsday.util;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class SkullTextureUtil {
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
