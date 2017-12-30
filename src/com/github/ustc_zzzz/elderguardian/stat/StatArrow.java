package com.github.ustc_zzzz.elderguardian.stat;

import com.flowpowered.math.vector.Vector3d;
import com.github.ustc_zzzz.elderguardian.ElderGuardian;
import com.github.ustc_zzzz.elderguardian.service.ElderGuardianCoolDownHelper;
import com.github.ustc_zzzz.elderguardian.util.ElderGuardianHelper;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.PickupRules;
import org.spongepowered.api.effect.sound.SoundTypes;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.projectile.arrow.TippedArrow;
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
public final class StatArrow extends ElderGuardianStatBase
{
    private final ElderGuardianCoolDownHelper coolDownHelper;

    public StatArrow(ElderGuardian plugin)
    {
        super(plugin, "arrow");
        this.coolDownHelper = plugin.getLoreStatService().getCoolDownHelper();
    }

    @Override
    protected String getDefaultTemplateStringTranslationKey()
    {
        return "elderguardian.arrow.defaultTemplate";
    }

    @Listener
    public void onUseItemStack(InteractItemEvent.Primary event, @First Player player)
    {
        List<DataContainer> stats = this.getStatsInHand(player, event.getItemStack());
        if (stats.isEmpty()) return;

        int coolDown = stats.stream().mapToInt(this::getCoolDown).reduce(0, Math::max);
        if (this.coolDownHelper.isInCoolDown(this.id, player)) return;
        this.coolDownHelper.setCoolDown(this.id, coolDown, player);

        World world = player.getWorld();
        Vector3d position = player.getLocation().getPosition().add(Vector3d.from(0, 1.5, 0));
        TippedArrow tippedArrow = (TippedArrow) world.createEntity(EntityTypes.TIPPED_ARROW, position);
        tippedArrow.setShooter(player);

        Vector3d acceleration = ElderGuardianHelper.getPlayerHeadingVector(player, 1.6);
        tippedArrow.setVelocity(acceleration);
        tippedArrow.offer(Keys.FIRE_TICKS, 2000);
        tippedArrow.offer(Keys.PICKUP_RULE, PickupRules.CREATIVE_ONLY);

        world.playSound(SoundTypes.ENTITY_ARROW_SHOOT, position, 1.0);

        Cause cause = Cause
                .source(EntitySpawnCause.builder().entity(tippedArrow).type(SpawnTypes.CUSTOM).build())
                .named("Player", player).build();
        world.spawnEntity(tippedArrow, cause);
    }

    private int getCoolDown(DataView data)
    {
        return data.getInt(DataQuery.of("cooldown")).orElse(0);
    }
}
