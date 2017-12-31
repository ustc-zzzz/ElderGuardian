package com.github.ustc_zzzz.elderguardian.stat;

import com.github.ustc_zzzz.elderguardian.ElderGuardian;
import com.github.ustc_zzzz.elderguardian.api.LoreMatcher;
import com.github.ustc_zzzz.elderguardian.api.LoreMatcherContext;
import com.github.ustc_zzzz.elderguardian.api.LoreStatEventDriven;
import com.github.ustc_zzzz.elderguardian.service.ElderGuardianService;
import com.google.common.collect.ImmutableList;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.projectile.Projectile;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;
import org.spongepowered.api.util.GuavaCollectors;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author ustc_zzzz
 */
public abstract class ElderGuardianStatBase extends LoreStatEventDriven
{
    protected final String id;
    protected final ElderGuardianService loreStatService;

    public ElderGuardianStatBase(ElderGuardian plugin, String statID)
    {
        super(plugin);
        this.id = statID;
        this.loreStatService = plugin.getLoreStatService();
    }

    @Override
    public void onLoreStatEnable()
    {
        super.onLoreStatEnable();
        if (!this.loreStatService.getLoreMatchers(this).isEmpty()) return;

        for (String template : this.getDefaultTemplates())
        {
            DataContainer data = new MemoryDataContainer().set(LoreMatcher.TEMPLATES, ImmutableList.of(template));
            this.loreStatService.addLoreMatcher(this, LoreMatcher.fromContainer(data));
        }
    }

    protected List<DataContainer> getStatsInHand(Player player, ItemStackSnapshot snapshot)
    {
        DataContainer presets = this.loreStatService.getLoreStatPresetsContainer(this.id);
        List<LoreMatcher> matchers = this.loreStatService.getLoreMatchers(this.id);
        LoreMatcherContext c = this.loreStatService.getContextBy(player, snapshot);
        List<Text> l = c.getHeldItemLore();

        return matchers.stream().flatMap(m -> m.match(l, c, presets).stream()).collect(GuavaCollectors.toImmutableList());
    }

    protected List<DataContainer> getStatsInHand(Player player)
    {
        return this.loreStatService.matchLoreByHeldItem(this, this.loreStatService.getContextBy(player));
    }

    protected List<DataContainer> getStatsInHand(Projectile projectile)
    {
        DataContainer presets = this.loreStatService.getLoreStatPresetsContainer(this.id);
        List<LoreMatcher> matchers = this.loreStatService.getLoreMatchers(this.id);
        LoreMatcherContext c = this.loreStatService.getContextBy(projectile);
        List<Text> l = c.getHeldItemLore();

        return matchers.stream().flatMap(m -> m.match(l, c, presets).stream()).collect(GuavaCollectors.toImmutableList());
    }

    protected Collection<String> getDefaultTemplates()
    {
        Text result = this.getPluginInstance().getTranslation().take(this.getDefaultTemplateStringTranslationKey());
        return Collections.singletonList(TextSerializers.FORMATTING_CODE.serialize(result));
    }

    protected abstract String getDefaultTemplateStringTranslationKey();

    @Nonnull
    @Override
    protected ElderGuardian getPluginInstance()
    {
        return (ElderGuardian) super.getPluginInstance();
    }

    @Nonnull
    @Override
    public String getLoreStatId()
    {
        return this.id;
    }
}
