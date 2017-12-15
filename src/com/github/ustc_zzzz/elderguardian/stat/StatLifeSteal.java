package com.github.ustc_zzzz.elderguardian.stat;

import com.flowpowered.math.GenericMath;
import com.github.ustc_zzzz.elderguardian.ElderGuardian;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.value.BoundedValue;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.cause.entity.damage.source.EntityDamageSource;
import org.spongepowered.api.event.cause.entity.damage.source.IndirectEntityDamageSource;
import org.spongepowered.api.event.entity.AttackEntityEvent;
import org.spongepowered.api.event.entity.DamageEntityEvent;
import org.spongepowered.api.event.filter.cause.Named;

import java.util.Optional;

/**
 * @author ustc_zzzz
 */
@ElderGuardianStat
public final class StatLifeSteal extends ElderGuardianStatBase
{
    public StatLifeSteal(ElderGuardian plugin)
    {
        super(plugin, "life_steal");
    }

    @Override
    protected String getDefaultTemplateStringTranslationKey()
    {
        return "elderguardian.lifeSteal.defaultTemplate";
    }

    @Listener(order = Order.LAST)
    public void onAttackEntity(AttackEntityEvent event, @Named(AttackEntityEvent.SOURCE) EntityDamageSource src)
    {
        Entity sourceEntity = src.getSource();
        if (!(sourceEntity instanceof Player)) return;

        Player player = (Player) sourceEntity;
        BoundedValue<Double> health = player.health();
        double playerHealth = health.get(), minHealth = health.getMinValue(), maxHealth = health.getMaxValue();

        double outputDamage = event.getFinalOutputDamage();
        Optional<Double> targetHealth = event.getTargetEntity().get(Keys.HEALTH);
        if (targetHealth.isPresent()) outputDamage = Math.min(outputDamage, targetHealth.get());

        for (DataContainer stat : this.getStatsInHand((Player) sourceEntity))
        {
            double added = outputDamage * this.getModifier(stat);
            Optional<Double> upperBound = stat.getDouble(DataQuery.of("maximum"));

            // noinspection OptionalIsPresent
            playerHealth += (upperBound.isPresent() ? Math.min(added, upperBound.get()) : added);
        }
        player.offer(Keys.HEALTH, GenericMath.clamp(playerHealth, minHealth, maxHealth));
    }

    @Listener(order = Order.LAST)
    public void onDamageEntity(DamageEntityEvent event, @Named(DamageEntityEvent.SOURCE) IndirectEntityDamageSource src)
    {
        Entity sourceEntity = src.getIndirectSource();
        if (!(sourceEntity instanceof Player)) return;

        Player player = (Player) sourceEntity;
        BoundedValue<Double> health = player.health();
        double playerHealth = health.get(), minHealth = health.getMinValue(), maxHealth = health.getMaxValue();

        double outputDamage = event.getFinalDamage();
        Optional<Double> targetHealth = event.getTargetEntity().get(Keys.HEALTH);
        if (targetHealth.isPresent()) outputDamage = Math.min(outputDamage, targetHealth.get());

        for (DataContainer stat : this.getStatsInHand((Player) sourceEntity))
        {
            double added = outputDamage * this.getModifier(stat);
            Optional<Double> upperBound = stat.getDouble(DataQuery.of("maximum"));

            // noinspection OptionalIsPresent
            playerHealth += (upperBound.isPresent() ? Math.min(added, upperBound.get()) : added);
        }
        player.offer(Keys.HEALTH, GenericMath.clamp(playerHealth, minHealth, maxHealth));
    }

    private double getModifier(DataView data)
    {
        return data.getDouble(DataQuery.of("modifier"))
                .orElse(data.getDouble(DataQuery.of("modifier-percent")).orElse(0.0) / 100);
    }
}
