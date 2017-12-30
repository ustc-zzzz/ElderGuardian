package com.github.ustc_zzzz.elderguardian.service;

import com.github.ustc_zzzz.elderguardian.api.LoreMatcherContext;
import com.google.common.collect.ImmutableList;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Optional;

/**
 * @author ustc_zzzz
 */
@NonnullByDefault
public class ElderGuardianPlayerMatcherContext implements LoreMatcherContext
{
    private final ItemStackSnapshot itemStackSnapshot;
    private final WeakReference<Player> playerWeakReference;

    ElderGuardianPlayerMatcherContext(Player player)
    {
        this.itemStackSnapshot = ItemStackSnapshot.NONE;
        this.playerWeakReference = new WeakReference<>(player);
    }

    ElderGuardianPlayerMatcherContext(ItemStackSnapshot stackSnapshot, Player player)
    {
        this.itemStackSnapshot = stackSnapshot;
        this.playerWeakReference = new WeakReference<>(player);
    }

    @Override
    public Optional<Player> getPlayer()
    {
        return Optional.ofNullable(this.playerWeakReference.get());
    }

    @Override
    public List<Text> getHeldItemLore()
    {
        if (this.itemStackSnapshot == ItemStackSnapshot.NONE)
        {
            Optional<Player> playerOptional = this.getPlayer();
            if (!playerOptional.isPresent()) return ImmutableList.of();

            Player player = playerOptional.get();
            Optional<ItemStack> stackOptional = player.getItemInHand(HandTypes.MAIN_HAND);
            if (!stackOptional.isPresent()) stackOptional = player.getItemInHand(HandTypes.OFF_HAND);

            if (!stackOptional.isPresent()) return ImmutableList.of();
            return stackOptional.get().get(Keys.ITEM_LORE).orElse(ImmutableList.of());
        }
        else
        {
            return itemStackSnapshot.get(Keys.ITEM_LORE).orElse(ImmutableList.of());
        }
    }
}
