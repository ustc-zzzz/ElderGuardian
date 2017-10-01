package com.github.ustc_zzzz.elderguardian.api;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.util.Optional;

/**
 * An event driven implementation of LoreStat.
 * <p>
 * You can use the {@link org.spongepowered.api.event.Listener} class to annotate your methods, and add your features
 * by hooking events.
 * </p>
 *
 * @author ustc_zzzz
 * @see LoreStat
 * @see LoreStatService
 */
@NonnullByDefault
public abstract class LoreStatEventDriven implements LoreStat
{
    private boolean isEnabled;

    private final Object pluginInstance;

    public LoreStatEventDriven(Object plugin)
    {
        Optional<PluginContainer> optional = Sponge.getPluginManager().fromInstance(plugin);
        if (!optional.isPresent()) throw new IllegalArgumentException("Not a plugin instance");

        this.pluginInstance = plugin;
    }

    @Override
    public void onLoreStatEnable()
    {
        this.isEnabled = true;
        Sponge.getEventManager().registerListeners(this.pluginInstance, this);
    }

    @Override
    public void onLoreStatDisable()
    {
        this.isEnabled = false;
        Sponge.getEventManager().unregisterListeners(this);
    }

    public boolean isEnabled()
    {
        return this.isEnabled;
    }

    protected Object getPluginInstance()
    {
        return this.pluginInstance;
    }
}
