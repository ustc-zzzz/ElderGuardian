package com.github.ustc_zzzz.elderguardian.stat;

import com.github.ustc_zzzz.elderguardian.ElderGuardian;
import com.github.ustc_zzzz.elderguardian.api.LoreStatEventDriven;
import com.github.ustc_zzzz.elderguardian.api.LoreStatService;
import com.github.ustc_zzzz.elderguardian.api.LoreTemplate;
import com.google.common.collect.ImmutableList;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * @author ustc_zzzz
 */
public abstract class ElderGuardianStatBase extends LoreStatEventDriven
{
    protected final LoreStatService loreStatService;
    protected final List<String> templateStrings = new LinkedList<>();
    protected final List<LoreTemplate> templates = new LinkedList<>();

    private String openArg;
    private String closeArg;

    public ElderGuardianStatBase(ElderGuardian plugin)
    {
        super(plugin);
        this.loreStatService = Sponge.getServiceManager().provideUnchecked(LoreStatService.class);
    }

    public void loadConfig(CommentedConfigurationNode node)
    {
        this.templates.clear();
        this.templateStrings.clear();
        this.openArg = node.getNode("open-arg").getString( "{{");
        this.closeArg = node.getNode("close-arg").getString( "}}");
        for (CommentedConfigurationNode child : node.getNode("templates").getChildrenList())
        {
            String value = child.getString();
            if (value != null)
            {
                this.templates.add(LoreTemplate.of(value, this.openArg, this.closeArg));
                this.templateStrings.add(value);
            }
        }
        if (this.templates.isEmpty())
        {
            String defaultTemplateString = this.getDefaultTemplateString();
            this.templates.add(LoreTemplate.of(defaultTemplateString, this.openArg, this.closeArg));
            this.templateStrings.add(defaultTemplateString);
        }
    }

    public void saveConfig(CommentedConfigurationNode node)
    {
        node.getNode("open-arg").setValue(this.openArg);
        node.getNode("close-arg").setValue(this.closeArg);
        node.getNode("templates").setValue(this.templateStrings);
    }

    public String getDefaultTemplateString()
    {
        Text result = this.getPluginInstance().getTranslation().take(this.getDefaultTemplateStringTranslationKey());
        return TextSerializers.FORMATTING_CODE.serialize(result);
    }

    protected abstract String getDefaultTemplateStringTranslationKey();

    protected List<DataContainer> getStatsInHand(Player player)
    {
        Optional<ItemStack> stackOptional = player.getItemInHand(HandTypes.MAIN_HAND);
        if (!stackOptional.isPresent()) stackOptional = player.getItemInHand(HandTypes.OFF_HAND);
        if (!stackOptional.isPresent()) return ImmutableList.of();
        return this.loreStatService.getStats(this, stackOptional.get());
    }

    @Override
    public Collection<LoreTemplate> getTemplates()
    {
        return this.templates;
    }

    @Override
    protected ElderGuardian getPluginInstance()
    {
        return (ElderGuardian) super.getPluginInstance();
    }
}
