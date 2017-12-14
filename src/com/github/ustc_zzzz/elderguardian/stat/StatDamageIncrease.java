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
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.action.CollideEvent;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.entity.damage.DamageModifier;
import org.spongepowered.api.event.cause.entity.damage.DamageModifierTypes;
import org.spongepowered.api.event.cause.entity.damage.source.EntityDamageSource;
import org.spongepowered.api.event.cause.entity.damage.source.IndirectEntityDamageSource;
import org.spongepowered.api.event.entity.AttackEntityEvent;
import org.spongepowered.api.event.entity.DamageEntityEvent;
import org.spongepowered.api.event.filter.cause.Named;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.world.World;

import java.util.List;

/**
 * @author ustc_zzzz
 */
@ElderGuardianStat
public class StatDamageIncrease extends ElderGuardianStatBase
{
    public StatDamageIncrease(ElderGuardian plugin)
    {
        super(plugin, "damage_increase");
    }

    @Override
    protected String getDefaultTemplateStringTranslationKey()
    {
        return "elderguardian.damageIncrease.defaultTemplate";
    }

    @Listener
    public void onAttackEntity(AttackEntityEvent event, @Named(AttackEntityEvent.SOURCE) EntityDamageSource source)
    {
        Entity sourceEntity = source.getSource();
        if (!(sourceEntity instanceof Player)) return;

        List<DataContainer> stats = this.getStatsInHand((Player) sourceEntity);
        double modifier = stats.stream().mapToDouble(this::getModifier).reduce(0, (a, b) -> a + b);

        event.setBaseOutputDamage(event.getBaseOutputDamage() * (1 + modifier));
    }

    @Listener
    public void onDamageEntity(DamageEntityEvent event, @Named(DamageEntityEvent.SOURCE) IndirectEntityDamageSource source)
    {
        Entity sourceEntity = source.getIndirectSource();
        if (!(sourceEntity instanceof Player)) return;

        List<DataContainer> stats = this.getStatsInHand((Player) sourceEntity);
        double modifier = stats.stream().mapToDouble(this::getModifier).reduce(0, (a, b) -> a + b);

        event.setBaseDamage(event.getBaseDamage() * (1 + modifier));
    }

    private double getModifier(DataView data)
    {
        return data.getDouble(DataQuery.of("modifier"))
                .orElse(data.getDouble(DataQuery.of("modifier-percent")).orElse(0.0) / 100);
    }
}
