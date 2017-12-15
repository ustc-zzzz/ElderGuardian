package com.github.ustc_zzzz.elderguardian.stat;

import com.github.ustc_zzzz.elderguardian.ElderGuardian;
import com.github.ustc_zzzz.elderguardian.service.ElderGuardianCoolDownHelper;
import com.github.ustc_zzzz.elderguardian.util.ElderGuardianHelper;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.manipulator.mutable.PotionEffectData;
import org.spongepowered.api.effect.potion.PotionEffect;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.cause.entity.damage.source.EntityDamageSource;
import org.spongepowered.api.event.cause.entity.damage.source.IndirectEntityDamageSource;
import org.spongepowered.api.event.entity.AttackEntityEvent;
import org.spongepowered.api.event.entity.DamageEntityEvent;
import org.spongepowered.api.event.filter.cause.Named;

import java.util.List;
import java.util.Optional;

/**
 * @author ustc_zzzz
 */
@ElderGuardianStat
public final class StatTargetEffect extends ElderGuardianStatBase
{
    private final ElderGuardianCoolDownHelper coolDownHelper;

    public StatTargetEffect(ElderGuardian plugin)
    {
        super(plugin, "target_effect");
        this.coolDownHelper = plugin.getLoreStatService().getCoolDownHelper();
    }

    @Override
    protected String getDefaultTemplateStringTranslationKey()
    {
        return "elderguardian.targetEffect.defaultTemplate";
    }

    @Listener
    public void onAttackEntity(AttackEntityEvent event, @Named(AttackEntityEvent.SOURCE) EntityDamageSource ds)
    {
        Entity sourceEntity = ds.getSource();
        if (!(sourceEntity instanceof Player)) return;
        this.giveTargetEffect(event.getTargetEntity(), (Player) sourceEntity);
    }

    @Listener
    public void onDamageEntity(DamageEntityEvent event, @Named(DamageEntityEvent.SOURCE) IndirectEntityDamageSource ds)
    {
        Entity sourceEntity = ds.getIndirectSource();
        if (!(sourceEntity instanceof Player)) return;
        this.giveTargetEffect(event.getTargetEntity(), (Player) sourceEntity);
    }

    private void giveTargetEffect(Entity target, Player player)
    {
        List<DataContainer> stats = this.getStatsInHand(player);
        int coolDown = stats.stream().mapToInt(this::getCoolDown).reduce(0, Math::max);
        if (this.coolDownHelper.isInCoolDown(this.id, player)) return;
        this.coolDownHelper.setCoolDown(this.id, coolDown, player);

        for (DataContainer stat : stats)
        {
            String effect = stat.getString(DataQuery.of("effect")).orElse("");
            int duration = stat.getInt(DataQuery.of("duration")).orElse(450);

            Optional<PotionEffect> potionEffectOptional = ElderGuardianHelper.getPotionEffect(effect, duration);
            potionEffectOptional.ifPresent(potionEffect -> this.giveEffect(target, potionEffect));
        }
    }

    private void giveEffect(Entity target, PotionEffect effect)
    {
        target.getOrCreate(PotionEffectData.class).ifPresent(data -> target.offer(data.addElement(effect)));
    }

    private int getCoolDown(DataView data)
    {
        return data.getInt(DataQuery.of("cooldown")).orElse(0);
    }
}
