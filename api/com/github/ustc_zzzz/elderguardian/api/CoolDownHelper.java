package com.github.ustc_zzzz.elderguardian.api;

import org.spongepowered.api.entity.living.player.Player;

/**
 * A helper interface for calculating cooldowns.
 * <p>
 * The helper can be fetched by method {@link LoreStatService#getCoolDownHelper()}.
 * String keys are used for identifying different types of cooldowns.
 * </p>
 *
 * @author ustc_zzzz
 */
public interface CoolDownHelper
{
    /**
     * Get the cooldown (ticks) for specified key and player
     *
     * @param key a key for identifying cooldown
     * @param player the target player
     * @return the cooldown if it exists, otherwise 0
     */
    long getCoolDown(String key, Player player);

    /**
     * Set the cooldown (ticks) for specified key and player
     *
     * @param key a key for identifying cooldown
     * @param coolDown the cooldown to be set
     * @param player the target player
     * @return the previous cooldown if it exists, otherwise 0
     */
    long setCoolDown(String key, long coolDown, Player player);

    /**
     * Add the cooldown (ticks) for specified key and player
     *
     * @param key a key for identifying cooldown
     * @param coolDown the cooldown to be added
     * @param player the target player
     * @return the previous cooldown if it exists, otherwise 0
     */
    long addCoolDown(String key, long coolDown, Player player);

    /**
     * Check if a player is still in cooldown for a specified key
     *
     * @param key a key for identifying cooldown
     * @param player the target player
     * @return if the cooldown does exist
     */
    default boolean isInCoolDown(String key, Player player)
    {
        return this.getCoolDown(key, player) > 0L;
    }
}
