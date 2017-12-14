package com.github.ustc_zzzz.elderguardian.stat;

import com.flowpowered.math.vector.Vector3d;
import com.github.ustc_zzzz.elderguardian.ElderGuardian;
import com.github.ustc_zzzz.elderguardian.service.ElderGuardianCoolDownHelper;
import com.github.ustc_zzzz.elderguardian.unsafe.SpongeUnimplemented;
import com.github.ustc_zzzz.elderguardian.util.ElderGuardianHelper;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.effect.sound.SoundTypes;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.projectile.explosive.fireball.SmallFireball;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.entity.spawn.EntitySpawnCause;
import org.spongepowered.api.event.cause.entity.spawn.SpawnTypes;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.item.inventory.InteractItemEvent;
import org.spongepowered.api.world.World;

import java.util.List;

/**
 * @author ustc_zzzz
 */
@ElderGuardianStat
public final class StatFireball extends ElderGuardianStatBase
{
    private final ElderGuardianCoolDownHelper coolDownHelper;

    public StatFireball(ElderGuardian plugin)
    {
        super(plugin, "fireball");
        this.coolDownHelper = plugin.getLoreStatService().getCoolDownHelper();
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
        if (this.coolDownHelper.isInCoolDown(this.id, player)) return;
        this.coolDownHelper.setCoolDown(this.id, coolDown, player);

        World world = player.getWorld();
        Vector3d position = player.getLocation().getPosition().add(Vector3d.from(0, 1.5, 0));
        SmallFireball smallFireball = (SmallFireball) world.createEntity(EntityTypes.SMALL_FIREBALL, position);
        smallFireball.setShooter(player);

        Vector3d acceleration = ElderGuardianHelper.getPlayerHeadingVector(player, 0.1);
        SpongeUnimplemented.setFireballPower(smallFireball, acceleration);

        world.playSound(SoundTypes.ENTITY_GHAST_SHOOT, position, 1.0F);

        Cause cause = Cause
                .source(EntitySpawnCause.builder().entity(smallFireball).type(SpawnTypes.CUSTOM).build())
                .named("Player", player).build();
        world.spawnEntity(smallFireball, cause);
    }

    private int getCoolDown(DataView data)
    {
        return data.getInt(DataQuery.of("cooldown")).orElse(0);
    }
}
