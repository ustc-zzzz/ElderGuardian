package com.github.ustc_zzzz.elderguardian.service;

import com.github.ustc_zzzz.elderguardian.api.LoreMatcherContext;
import com.google.common.collect.ImmutableList;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.projectile.Projectile;
import org.spongepowered.api.entity.projectile.source.ProjectileSource;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.text.Text;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * @author ustc_zzzz
 */
public class ElderGuardianProjectileMatcherContext implements LoreMatcherContext
{
    private final ItemStackSnapshot itemStackSnapshot;
    private final WeakReference<Projectile> projectileWeakReference;

    ElderGuardianProjectileMatcherContext(Projectile projectile)
    {
        this.itemStackSnapshot = ItemStackSnapshot.NONE;
        this.projectileWeakReference = new WeakReference<>(projectile);
    }

    ElderGuardianProjectileMatcherContext(ItemStackSnapshot stackSnapshot, Projectile projectile)
    {
        this.itemStackSnapshot = stackSnapshot;
        this.projectileWeakReference = new WeakReference<>(projectile);
    }

    @Override
    public Optional<Player> getPlayer()
    {
        Projectile projectile = this.projectileWeakReference.get();
        if (Objects.isNull(projectile)) return Optional.empty();

        ProjectileSource source = projectile.getShooter();
        if (source instanceof Player) return Optional.of((Player) source);

        return Optional.empty();
    }

    @Override
    public List<Text> getHeldItemLore()
    {
        return this.itemStackSnapshot.get(Keys.ITEM_LORE).orElse(ImmutableList.of());
    }
}
