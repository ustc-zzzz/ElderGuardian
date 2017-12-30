package com.github.ustc_zzzz.elderguardian.api;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.projectile.Projectile;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.util.Collection;
import java.util.Optional;

/**
 * The service for managing and dispatching LoreStats.
 * <p>
 * The service will be available before {@link org.spongepowered.api.event.game.state.GameAboutToStartServerEvent}.
 * LoreStats should be registered at that time.
 * </p>
 *
 * @author ustc_zzzz
 * @see LoreStat
 * @see LoreStatEventDriven
 */
@NonnullByDefault
public interface LoreStatService extends LoreMatcherHandler
{
    void registerStat(LoreStat stat);

    CoolDownHelper getCoolDownHelper();

    Collection<String> getAvailableStats();

    Optional<LoreStat> getLoreStat(String id);

    LoreMatcherContext getContextBy(Player player);

    LoreMatcherContext getContextBy(Projectile entity);

    LoreMatcherContext getContextBy(Player player, ItemStackSnapshot stack);
}
