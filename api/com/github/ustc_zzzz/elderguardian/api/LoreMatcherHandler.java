package com.github.ustc_zzzz.elderguardian.api;

import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.GuavaCollectors;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.util.Collection;
import java.util.List;

/**
 * @author ustc_zzzz
 */
@NonnullByDefault
public interface LoreMatcherHandler
{
    Collection<String> getAvailableLoreMatchers();

    List<LoreMatcher> getLoreMatchers(String id);

    default List<LoreMatcher> getLoreMatchers(LoreStat stat)
    {
        return this.getLoreMatchers(stat.getLoreStatId());
    }

    void clearLoreMatchers(String id);

    default void clearLoreMatchers(LoreStat stat)
    {
        this.clearLoreMatchers(stat.getLoreStatId());
    }

    void addLoreMatcher(String id, LoreMatcher loreMatcher);

    default void addLoreMatcher(LoreStat stat, LoreMatcher loreMatcher)
    {
        this.addLoreMatcher(stat.getLoreStatId(), loreMatcher);
    }

    default List<DataContainer> matchLoreByHeldItem(String id, LoreMatcherContext context)
    {
        List<Text> l = context.getHeldItemLore();
        List<LoreMatcher> matchers = this.getLoreMatchers(id);
        return matchers.stream().flatMap(m -> m.match(l, context).stream()).collect(GuavaCollectors.toImmutableList());
    }

    default List<DataContainer> matchLoreByHeldItem(LoreStat stat, LoreMatcherContext context)
    {
        return this.matchLoreByHeldItem(stat.getLoreStatId(), context);
    }
}
