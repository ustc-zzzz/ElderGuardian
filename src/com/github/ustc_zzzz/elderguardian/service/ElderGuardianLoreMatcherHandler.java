package com.github.ustc_zzzz.elderguardian.service;

import com.github.ustc_zzzz.elderguardian.api.LoreMatcher;
import com.github.ustc_zzzz.elderguardian.api.LoreMatcherHandler;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.util.*;

/**
 * @author ustc_zzzz
 */
@NonnullByDefault
public class ElderGuardianLoreMatcherHandler implements LoreMatcherHandler
{
    private final Map<String, LinkedList<LoreMatcher>> matchers = new HashMap<>();

    @Override
    public Collection<String> getAvailableLoreMatchers()
    {
        return ImmutableSet.copyOf(this.matchers.keySet());
    }

    @Override
    public List<LoreMatcher> getLoreMatchers(String id)
    {
        return this.matchers.containsKey(id) ? ImmutableList.copyOf(this.matchers.get(id)) : ImmutableList.of();
    }

    @Override
    public void clearLoreMatchers(String id)
    {
        this.matchers.remove(id);
    }

    @Override
    public void addLoreMatcher(String id, LoreMatcher loreMatcher)
    {
        this.matchers.computeIfAbsent(id, k -> new LinkedList<>()).add(loreMatcher);
    }

    public void loadLoreConfig(CommentedConfigurationNode node)
    {
        this.matchers.clear();
        for (Map.Entry<Object, ? extends CommentedConfigurationNode> entry : node.getChildrenMap().entrySet())
        {
            LinkedList<LoreMatcher> matchers = new LinkedList<>();
            for (CommentedConfigurationNode child : entry.getValue().getChildrenList())
            {
                List<String> templateStrings = this.getTemplateStrings(child);
                if (!templateStrings.isEmpty())
                {
                    DataContainer d = new MemoryDataContainer();
                    d.set(LoreMatcher.CLOSE_ARG, child.getNode("close-arg").getString(LoreMatcher.DEFAULT_CLOSE_ARG));
                    d.set(LoreMatcher.OPEN_ARG, child.getNode("open-arg").getString(LoreMatcher.DEFAULT_OPEN_ARG));
                    d.set(LoreMatcher.TEMPLATES, templateStrings);
                    matchers.add(LoreMatcher.fromContainer(d));
                }
            }
            if (!matchers.isEmpty()) this.matchers.put(entry.getKey().toString().replace('-', '_'), matchers);
        }
    }

    public void saveLoreConfig(CommentedConfigurationNode node)
    {
        node.setValue(ImmutableMap.of());
        for (Map.Entry<String, LinkedList<LoreMatcher>> entry : this.matchers.entrySet())
        {
            CommentedConfigurationNode childrenNodeList = node.getNode(entry.getKey().replace('_', '-'));
            childrenNodeList.setValue(ImmutableList.of());
            for (LoreMatcher matcher : entry.getValue())
            {
                CommentedConfigurationNode child = childrenNodeList.getAppendedNode();
                child.getNode("close-arg").setValue(matcher.getCloseArg());
                child.getNode("open-arg").setValue(matcher.getOpenArg());
                this.setTemplateStrings(child, matcher.getTemplates());
            }
        }
    }

    private List<String> getTemplateStrings(CommentedConfigurationNode node)
    {
        List<String> templates = new ArrayList<>();
        String template = node.getNode("template").getString("");
        if (template.isEmpty())
        {
            for (CommentedConfigurationNode templateNode : node.getNode("templates").getChildrenList())
            {
                template = templateNode.getString("");
                if (!template.isEmpty()) templates.add(template);
            }
            return templates;
        }
        templates.add(template);
        return templates;
    }

    private void setTemplateStrings(CommentedConfigurationNode node, List<String> templateStrings)
    {
        switch (templateStrings.size())
        {
        case 0:
            node.removeChild("template");
            node.removeChild("templates");
            break;
        case 1:
            node.removeChild("templates");
            node.getNode("template").setValue(templateStrings.iterator().next());
            break;
        default:
            node.removeChild("template");
            node.getNode("templates").setValue(templateStrings);
        }
    }
}
