package com.github.ustc_zzzz.elderguardian.stat;

import com.github.ustc_zzzz.elderguardian.ElderGuardian;
import com.github.ustc_zzzz.elderguardian.service.ElderGuardianCoolDownHelper;
import com.github.ustc_zzzz.elderguardian.util.ElderGuardianHelper;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.manipulator.mutable.PotionEffectData;
import org.spongepowered.api.effect.potion.PotionEffect;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.item.inventory.InteractItemEvent;

import java.util.List;
import java.util.Optional;

/**
 * @author ustc_zzzz
 */
@ElderGuardianStat
public class StatSelfEffect extends ElderGuardianStatBase
{
    private final ElderGuardianCoolDownHelper coolDownHelper;

    public StatSelfEffect(ElderGuardian plugin)
    {
        super(plugin, "self_effect");
        this.coolDownHelper = plugin.getLoreStatService().getCoolDownHelper();
    }

    @Override
    protected String getDefaultTemplateStringTranslationKey()
    {
        return "elderguardian.selfEffect.defaultTemplate";
    }

    @Listener
    public void onUseItemStack(InteractItemEvent.Secondary event, @First Player player)
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
            potionEffectOptional.ifPresent(potionEffect -> this.giveEffect(player, potionEffect));
        }
    }

    private void giveEffect(Player player, PotionEffect effect)
    {
        player.getOrCreate(PotionEffectData.class).ifPresent(data -> player.offer(data.addElement(effect)));
    }

    private int getCoolDown(DataView data)
    {
        return data.getInt(DataQuery.of("cooldown")).orElse(0);
    }
}
