package com.github.ustc_zzzz.elderguardian.api;

import org.spongepowered.api.util.annotation.NonnullByDefault;

/**
 * The base class for adding new stats of skills.
 * <p>
 * New LoreStats should be registered at {@link org.spongepowered.api.event.game.state.GameAboutToStartServerEvent}
 * by {@link LoreStatService#registerStat(LoreStat)}. It is ensured that the service is available before the event
 * is fired. The service should be fetched by {@link org.spongepowered.api.service.ServiceManager#provide(Class)}.
 * </p>
 *
 * @author ustc_zzzz
 * @see LoreStatService
 */
@NonnullByDefault
public interface LoreStat
{
    /**
     * The identification of the LoreStat. The format of the id should be snake_case, which should only contain
     * underscores and lowercase characters.
     */
    String getLoreStatId();

    /**
     * This method will be called when the LoreStat is enabled. At that time the {@link LoreStatService} is available.
     */
    void onLoreStatEnable();

    /**
     * This method will be called when the LoreStat is disabled. At that time the {@link LoreStatService} is available.
     */
    void onLoreStatDisable();
}
