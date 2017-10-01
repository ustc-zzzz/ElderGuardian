package com.github.ustc_zzzz.elderguardian.stat;

import com.flowpowered.math.TrigMath;
import com.flowpowered.math.vector.Vector3d;
import com.github.ustc_zzzz.elderguardian.ElderGuardian;
import com.github.ustc_zzzz.elderguardian.unsafe.SpongeUnimplemented;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.projectile.explosive.fireball.SmallFireball;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.entity.spawn.EntitySpawnCause;
import org.spongepowered.api.event.cause.entity.spawn.SpawnTypes;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.item.inventory.InteractItemEvent;
import org.spongepowered.api.world.World;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;

/**
 * @author ustc_zzzz
 */
public final class FireballStat extends ElderGuardianStatBase
{
    private final Map<Player, Long> lastActionAt = new WeakHashMap<>();

    public FireballStat(ElderGuardian plugin)
    {
        super(plugin);
    }

    @Override
    public String getLoreStatId()
    {
        return "fireball";
    }

    @Override
    protected String getDefaultTemplateStringTranslationKey()
    {
        return "elderguardian.fireball.defaultTemplate";
    }

    @Listener
    public void onUseItemStack(InteractItemEvent.Primary event, @First Player player)
    {
        List<DataContainer> stats = this.loreStatService.getStats(this, event.getItemStack().createStack());
        if (stats.isEmpty()) return;

        int coolDown = stats.stream().mapToInt(this::getCoolDown).reduce(0, Math::max);
        if (!this.checkCoolDown(player, coolDown)) return;

        World world = player.getWorld();
        Vector3d position = player.getLocation().getPosition().add(Vector3d.UNIT_Y);
        SmallFireball smallFireball = (SmallFireball) world.createEntity(EntityTypes.SMALL_FIREBALL, position);
        smallFireball.setShooter(player);

        double pitch = player.getHeadRotation().getX(), yaw = player.getHeadRotation().getY();
        double yawCos = TrigMath.cos(-yaw * TrigMath.DEG_TO_RAD - TrigMath.PI);
        double yawSin = TrigMath.sin(-yaw * TrigMath.DEG_TO_RAD - TrigMath.PI);
        double pitchCos = -TrigMath.cos(-pitch * TrigMath.DEG_TO_RAD);
        double pitchSin = TrigMath.sin(-pitch * TrigMath.DEG_TO_RAD);
        Vector3d acceleration = Vector3d.from(yawSin * pitchCos / 10, pitchSin / 10, yawCos * pitchCos / 10);

        SpongeUnimplemented.setFireballPower(smallFireball, acceleration);
        Cause cause = Cause
                .source(EntitySpawnCause.builder().entity(smallFireball).type(SpawnTypes.CUSTOM).build())
                .named("Player", player).build();
        world.spawnEntity(smallFireball, cause);
    }

    @Listener
    public void onMoveEntity(MoveEntityEvent event)
    {
        Entity targetEntity = event.getTargetEntity();
        if (!(targetEntity instanceof Player)) return;
        World fromExtent = event.getFromTransform().getExtent();
        World toExtent = event.getToTransform().getExtent();
        if (!Objects.equals(fromExtent, toExtent)) return;
        if (!(this.lastActionAt.containsKey(targetEntity))) return;
        long diff = toExtent.getProperties().getTotalTime() - fromExtent.getProperties().getTotalTime();
        this.lastActionAt.put((Player) targetEntity, this.lastActionAt.get(targetEntity) + diff);
    }

    private boolean checkCoolDown(Player player, int coolDown)
    {
        boolean allowAction = true;
        long now = player.getWorld().getProperties().getTotalTime();
        if (this.lastActionAt.containsKey(player))
        {
            long boundary = this.lastActionAt.get(player) + coolDown;
            allowAction = now >= boundary;
        }
        if (allowAction)
        {
            this.lastActionAt.put(player, now);
            return true;
        }
        return false;
    }

    private int getCoolDown(DataView data)
    {
        return data.getInt(DataQuery.of("cooldown")).orElse(0);
    }
}
