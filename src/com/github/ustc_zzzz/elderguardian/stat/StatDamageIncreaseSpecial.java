package com.github.ustc_zzzz.elderguardian.stat;

import com.flowpowered.math.GenericMath;
import com.github.ustc_zzzz.elderguardian.ElderGuardian;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.cause.entity.damage.DamageModifier;
import org.spongepowered.api.event.cause.entity.damage.source.EntityDamageSource;
import org.spongepowered.api.event.cause.entity.damage.source.IndirectEntityDamageSource;
import org.spongepowered.api.event.entity.AttackEntityEvent;
import org.spongepowered.api.event.entity.DamageEntityEvent;
import org.spongepowered.api.event.filter.cause.Named;
import org.spongepowered.api.util.Tuple;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * @author ustc_zzzz
 */
@ElderGuardianStat
public final class StatDamageIncreaseSpecial extends ElderGuardianStatBase
{
    public StatDamageIncreaseSpecial(ElderGuardian plugin)
    {
        super(plugin, "damage_increase_special");
    }

    @Override
    protected String getDefaultTemplateStringTranslationKey()
    {
        return "elderguardian.damageIncreaseSpecial.defaultTemplate";
    }

    @Listener
    public void onAttackEntity(AttackEntityEvent event, @Named(AttackEntityEvent.SOURCE) EntityDamageSource source)
    {
        Entity sourceEntity = source.getSource();
        if (!(sourceEntity instanceof Player)) return;

        List<DataContainer> stats = this.getStatsInHand((Player) sourceEntity);
        Map<String, Double> specialModifierMap = new HashMap<>();
        for (DataContainer data : stats)
        {
            String modifierType = this.getModifierType(data);
            if (!modifierType.isEmpty())
            {
                double original = specialModifierMap.getOrDefault(modifierType, 0.0);
                specialModifierMap.put(modifierType, original + this.getModifier(data));
            }
        }

        for (Tuple<DamageModifier, Function<? super Double, Double>> tuple : event.getModifiers())
        {
            DamageModifier modifierKey = tuple.getFirst();
            String modifierKeyString = modifierKey.getType().getId();
            double modifier = Math.max(-1, specialModifierMap.getOrDefault(modifierKeyString, 0.0));
            if (modifier > GenericMath.DBL_EPSILON || modifier < -GenericMath.DBL_EPSILON)
            {
                specialModifierMap.remove(modifierKeyString);
                Function<? super Double, Double> originalFunction = tuple.getSecond();
                event.setOutputDamage(modifierKey, v -> originalFunction.apply(v) * (1 + modifier) + v * modifier);
            }
        }
    }

    @Listener
    public void onDamageEntity(DamageEntityEvent event, @Named(DamageEntityEvent.SOURCE) IndirectEntityDamageSource source)
    {
        Entity sourceEntity = source.getIndirectSource();
        if (!(sourceEntity instanceof Player)) return;

        List<DataContainer> stats = this.getStatsInHand((Player) sourceEntity);
        Map<String, Double> specialModifierMap = new HashMap<>();
        for (DataContainer data : stats)
        {
            String modifierType = this.getModifierType(data);
            if (!modifierType.isEmpty())
            {
                double original = specialModifierMap.getOrDefault(modifierType, 0.0);
                specialModifierMap.put(modifierType, original + this.getModifier(data));
            }
        }

        for (Tuple<DamageModifier, Function<? super Double, Double>> tuple : event.getModifiers())
        {
            DamageModifier modifierKey = tuple.getFirst();
            String modifierKeyString = modifierKey.getType().getId();
            double modifier = Math.max(-1, specialModifierMap.getOrDefault(modifierKeyString, 0.0));
            if (modifier > GenericMath.DBL_EPSILON || modifier < -GenericMath.DBL_EPSILON)
            {
                specialModifierMap.remove(modifierKeyString);
                Function<? super Double, Double> originalFunction = tuple.getSecond();
                event.setDamage(modifierKey, v -> originalFunction.apply(v) * (1 + modifier) + v * modifier);
            }
        }
    }

    private String getModifierType(DataContainer data)
    {
        return data.getString(DataQuery.of("modifier-type")).orElse("");
    }

    private double getModifier(DataView data)
    {
        return data.getDouble(DataQuery.of("modifier"))
                .orElse(data.getDouble(DataQuery.of("modifier-percent")).orElse(0.0) / 100);
    }
}
