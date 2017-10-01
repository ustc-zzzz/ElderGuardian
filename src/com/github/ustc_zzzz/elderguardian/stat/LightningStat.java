package com.github.ustc_zzzz.elderguardian.stat;

import com.flowpowered.math.vector.Vector3d;
import com.github.ustc_zzzz.elderguardian.ElderGuardian;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.projectile.Projectile;
import org.spongepowered.api.entity.projectile.explosive.fireball.Fireball;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.action.CollideEvent;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.entity.damage.source.EntityDamageSource;
import org.spongepowered.api.event.cause.entity.spawn.EntitySpawnCause;
import org.spongepowered.api.event.cause.entity.spawn.SpawnCause;
import org.spongepowered.api.event.entity.DamageEntityEvent;
import org.spongepowered.api.event.entity.projectile.TargetProjectileEvent;
import org.spongepowered.api.event.filter.cause.Named;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.world.World;

import java.util.List;
import java.util.Random;

/**
 * @author ustc_zzzz
 */
public final class LightningStat extends ElderGuardianStatBase
{
    private final Cause cause;
    private final Random random = new Random();

    public LightningStat(ElderGuardian plugin)
    {
        super(plugin);
        this.cause = Cause.source(this.getPluginInstance()).build();
    }

    @Override
    public String getLoreStatId()
    {
        return "lightning";
    }

    @Override
    protected String getDefaultTemplateStringTranslationKey()
    {
        return "elderguardian.lightning.defaultTemplate";
    }

    @Listener
    public void onDamageEntity(DamageEntityEvent event, @Named(DamageEntityEvent.SOURCE) EntityDamageSource source)
    {
        Entity sourceEntity = source.getSource();
        if (!(sourceEntity instanceof Player)) return;

        List<DataContainer> stats = this.getStatsInHand((Player) sourceEntity);
        int possibility = stats.stream().mapToInt(this::getPossibility).reduce(Math::min).orElse(0);

        if (possibility > 0 && this.random.nextInt(possibility) == 0)
        {
            World world = sourceEntity.getWorld();
            Vector3d position = event.getTargetEntity().getLocation().getPosition();
            world.spawnEntity(world.createEntity(EntityTypes.LIGHTNING, position), this.cause);
        }
    }

    @Listener
    public void onCollideImpact(CollideEvent.Impact event, @Root Projectile projectile)
    {
        List<DataContainer> stats = this.loreStatService.getStats(this, projectile);
        int possibility = stats.stream().mapToInt(this::getPossibility).reduce(Math::min).orElse(0);

        if (possibility > 0 && this.random.nextInt(possibility) == 0)
        {
            World world = projectile.getWorld();
            Vector3d position = projectile.getLocation().getPosition();
            world.spawnEntity(world.createEntity(EntityTypes.LIGHTNING, position), this.cause);
        }
    }

    private int getPossibility(DataView data)
    {
        return data.getInt(DataQuery.of("possibility")).orElse(1);
    }
}
