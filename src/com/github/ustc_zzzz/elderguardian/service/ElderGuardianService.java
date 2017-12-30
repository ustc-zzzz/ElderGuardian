package com.github.ustc_zzzz.elderguardian.service;

import com.github.ustc_zzzz.elderguardian.ElderGuardian;
import com.github.ustc_zzzz.elderguardian.api.LoreMatcher;
import com.github.ustc_zzzz.elderguardian.api.LoreMatcherContext;
import com.github.ustc_zzzz.elderguardian.api.LoreStat;
import com.github.ustc_zzzz.elderguardian.api.LoreStatService;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.projectile.Projectile;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.util.*;

/**
 * @author ustc_zzzz
 */
@NonnullByDefault
public class ElderGuardianService extends ElderGuardianLoreMatcherHandler implements LoreStatService
{
    private final ElderGuardianCoolDownHelper coolDownHelper;
    private final Map<String, LoreStat> stats = new HashMap<>();
    private final Set<String> enabledStats = new LinkedHashSet<>();
    private final Map<Projectile, ItemStack> stacks = new WeakHashMap<>();

    public ElderGuardianService(ElderGuardian plugin)
    {
        this.coolDownHelper = new ElderGuardianCoolDownHelper(plugin);
        Sponge.getServiceManager().setProvider(plugin, LoreStatService.class, this);
        Sponge.getEventManager().registerListener(plugin, SpawnEntityEvent.class, this::onSpawnEntity);
    }

    @Override
    public void registerStat(LoreStat stat)
    {
        String id = stat.getLoreStatId();
        if (ElderGuardian.PLUGIN_ID.equals(id))
        {
            String msg = "Plugin id is not allowed for stat id";
            throw new IllegalArgumentException(msg);
        }
        this.stats.put(id, stat);
    }

    @Override
    public Optional<LoreStat> getLoreStat(String id)
    {
        return Optional.ofNullable(this.stats.get(id));
    }

    @Override
    public LoreMatcherContext getContextBy(Player player)
    {
        return new ElderGuardianPlayerMatcherContext(player);
    }

    @Override
    public LoreMatcherContext getContextBy(Projectile entity)
    {
        if (this.stacks.containsKey(entity))
        {
            return new ElderGuardianProjectileMatcherContext(this.stacks.get(entity).createSnapshot(), entity);
        }
        else
        {
            return new ElderGuardianProjectileMatcherContext(entity);
        }
    }

    @Override
    public LoreMatcherContext getContextBy(Player player, ItemStackSnapshot stack)
    {
        return new ElderGuardianPlayerMatcherContext(stack, player);
    }

    @Override
    public ElderGuardianCoolDownHelper getCoolDownHelper()
    {
        return this.coolDownHelper;
    }

    @Override
    public Collection<String> getAvailableStats()
    {
        return ImmutableSet.copyOf(this.stats.keySet());
    }

    private void onSpawnEntity(SpawnEntityEvent event)
    {
        Optional<Player> playerOptional = event.getCause().first(Player.class);
        if (!playerOptional.isPresent()) return;
        Optional<ItemStack> itemStack = playerOptional.get().getItemInHand(HandTypes.MAIN_HAND);
        if (!itemStack.isPresent()) itemStack = playerOptional.get().getItemInHand(HandTypes.OFF_HAND);
        if (!itemStack.isPresent()) return;
        for (Entity newbie : event.getEntities())
        {
            if (newbie instanceof Projectile) this.stacks.put(((Projectile) newbie), itemStack.get());
        }
    }

    public void enableStats(Set<String> enabledStats)
    {
        for (String id : enabledStats)
        {
            if (!this.enabledStats.contains(id))
            {
                this.enabledStats.add(id);
                if (this.stats.containsKey(id)) this.stats.get(id).onLoreStatEnable();
            }
        }
    }

    public Set<String> disableStats()
    {
        ImmutableSet.Builder<String> builder = ImmutableSet.builder();
        for (String id : this.enabledStats)
        {
            builder.add(id);
            if (this.stats.containsKey(id)) this.stats.get(id).onLoreStatDisable();
        }
        this.enabledStats.clear();
        return builder.build();
    }
}
