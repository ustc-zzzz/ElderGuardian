package com.github.ustc_zzzz.elderguardian.api;

import com.google.common.collect.ImmutableList;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.projectile.Projectile;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.util.Collection;
import java.util.List;
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
public interface LoreStatService
{
    void registerStat(LoreStat stat);

    Collection<String> getAvailableStats();

    Optional<LoreStat> getLoreStat(String id);

    CoolDownHelper getCoolDownHelper();

    Optional<ItemStack> getItemStackOfPlayerFrom(Projectile entity);

    default List<DataContainer> getStats(LoreStat stat, Projectile entity)
    {
        Optional<ItemStack> optional = this.getItemStackOfPlayerFrom(entity);
        return optional.map(itemStack -> this.getStats(stat, itemStack)).orElse(ImmutableList.of());
    }

    default List<DataContainer> getStats(LoreStat stat, ItemStack stack)
    {
        ImmutableList.Builder<DataContainer> builder = ImmutableList.builder();
        for (Text lore : stack.get(Keys.ITEM_LORE).orElse(ImmutableList.of()))
        {
            for (LoreTemplate template : stat.getTemplates())
            {
                template.translate(lore).ifPresent(builder::add);
            }
        }
        return builder.build();
    }
}
