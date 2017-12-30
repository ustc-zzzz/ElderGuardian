package com.github.ustc_zzzz.elderguardian.api;

import com.google.common.collect.ImmutableList;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

import java.util.List;
import java.util.Optional;

/**
 * @author ustc_zzzz
 */
public interface LoreMatcherContext
{
    LoreMatcherContext EMPTY = new LoreMatcherContext()
    {
        @Override
        public Optional<Player> getPlayer()
        {
            return Optional.empty();
        }

        @Override
        public List<Text> getHeldItemLore()
        {
            return ImmutableList.of();
        }
    };

    Optional<Player> getPlayer();

    List<Text> getHeldItemLore();
}
