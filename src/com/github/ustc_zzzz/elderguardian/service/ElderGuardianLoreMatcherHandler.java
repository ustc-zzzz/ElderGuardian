package com.github.ustc_zzzz.elderguardian.service;

import com.github.ustc_zzzz.elderguardian.api.LoreMatcher;
import com.github.ustc_zzzz.elderguardian.api.LoreMatcherHandler;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.util.*;

/**
 * @author ustc_zzzz
 */
@NonnullByDefault
public class ElderGuardianLoreMatcherHandler implements LoreMatcherHandler
{
    protected final Map<String, LinkedList<LoreMatcher>> matchers = new HashMap<>();

    @Override
    public Collection<String> getAvailableLoreMatchers()
    {
        return ImmutableSet.copyOf(this.matchers.keySet());
    }

    @Override
    public List<LoreMatcher> getLoreMatchers(String id)
    {
        return this.matchers.containsKey(id) ? ImmutableList.copyOf(this.matchers.get(id)) : ImmutableList.of();
    }

    @Override
    public void clearLoreMatchers(String id)
    {
        this.matchers.remove(id);
    }

    @Override
    public void addLoreMatcher(String id, LoreMatcher loreMatcher)
    {
        this.matchers.computeIfAbsent(id, k -> new LinkedList<>()).add(loreMatcher);
    }
}
