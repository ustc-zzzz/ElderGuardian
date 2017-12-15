package com.github.ustc_zzzz.elderguardian.stat;

import com.github.ustc_zzzz.elderguardian.ElderGuardian;
import com.github.ustc_zzzz.elderguardian.service.ElderGuardianCoolDownHelper;
import com.github.ustc_zzzz.elderguardian.util.ElderGuardianHelper;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.manipulator.mutable.PotionEffectData;
import org.spongepowered.api.effect.potion.PotionEffect;
import org.spongepowered.api.effect.sound.SoundTypes;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.item.inventory.InteractItemEvent;
import org.spongepowered.api.util.AABB;
import org.spongepowered.api.world.World;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

/**
 * @author ustc_zzzz
 */
@ElderGuardianStat
public final class StatAOEEffect extends ElderGuardianStatBase
{
    private final ElderGuardianCoolDownHelper coolDownHelper;

    public StatAOEEffect(ElderGuardian plugin)
    {
        super(plugin, "aoe_effect");
        this.coolDownHelper = plugin.getLoreStatService().getCoolDownHelper();
    }

    @Override
    protected String getDefaultTemplateStringTranslationKey()
    {
        return "elderguardian.aoeEffect.defaultTemplate";
    }

    @Listener
    public void onUseItemStack(InteractItemEvent.Secondary event, @First Player player)
    {
        Optional<AABB> aabbOptional = player.getBoundingBox();
        if (!aabbOptional.isPresent()) return;
        AABB aabb = aabbOptional.get();

        List<DataContainer> stats = this.getStatsInHand(player);
        int coolDown = stats.stream().mapToInt(this::getCoolDown).reduce(0, Math::max);
        if (this.coolDownHelper.isInCoolDown(this.id, player)) return;
        this.coolDownHelper.setCoolDown(this.id, coolDown, player);

        for (DataContainer stat : stats)
        {
            String effect = stat.getString(DataQuery.of("effect")).orElse("");
            int duration = stat.getInt(DataQuery.of("duration")).orElse(450);
            int diameter = stat.getInt(DataQuery.of("range")).orElse(5) * 2;

            Optional<PotionEffect> potionEffectOptional = ElderGuardianHelper.getPotionEffect(effect, duration);
            if (potionEffectOptional.isPresent())
            {
                World world = player.getWorld();
                AABB range = aabb.expand(diameter, diameter, diameter);
                PotionEffect potionEffect = potionEffectOptional.get();
                this.giveRangeEffect(world, range, e -> e instanceof Living && !e.equals(player), potionEffect);
            }
        }
    }

    private void giveRangeEffect(World world, AABB range, Predicate<Entity> filter, PotionEffect effect)
    {
        Set<Entity> entities = world.getIntersectingEntities(range, filter);
        for (Entity entity : entities)
        {
            world.playSound(SoundTypes.ENTITY_SPLASH_POTION_BREAK, entity.getLocation().getPosition(), 1.0);
            entity.getOrCreate(PotionEffectData.class).ifPresent(data -> entity.offer(data.addElement(effect)));
        }
    }

    private int getCoolDown(DataView data)
    {
        return data.getInt(DataQuery.of("cooldown")).orElse(0);
    }
}
