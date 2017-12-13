package com.github.ustc_zzzz.elderguardian;

import com.github.ustc_zzzz.elderguardian.service.ElderGuardianService;
import com.github.ustc_zzzz.elderguardian.stat.*;
import com.google.common.base.Throwables;
import com.google.inject.Inject;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.*;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.channel.MessageReceiver;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * @author ustc_zzzz
 */
@Plugin(id = ElderGuardian.PLUGIN_ID, version = ElderGuardian.PLUGIN_VERSION, authors =
        {"ustc_zzzz"}, name = "ElderGuardian", description = ElderGuardian.DESCRIPTION)
public class ElderGuardian
{
    public static final String PLUGIN_ID = "elderguardian";
    public static final String PLUGIN_VERSION = "@version@";
    public static final String DESCRIPTION = "A sponge plugin providing new stats or skills by identifying lores.";

    @Inject
    private Logger logger;

    @Inject
    @DefaultConfig(sharedRoot = false)
    private ConfigurationLoader<CommentedConfigurationNode> config;

    private CommentedConfigurationNode rootConfigNode;

    private ElderGuardianTranslation translation;
    private ElderGuardianService loreStatService;

    private List<ElderGuardianStatBase> stats = new LinkedList<>();

    @Listener
    public void onPostInitialization(GamePostInitializationEvent event)
    {
        this.translation = new ElderGuardianTranslation(this);
        this.loreStatService = new ElderGuardianService(this);
    }

    @Listener
    public void onAboutToStartServer(GameAboutToStartServerEvent event)
    {
        this.registerStat(new StatAOEEffect(this));
        this.registerStat(new StatArrow(this));
        this.registerStat(new StatAttract(this));
        this.registerStat(new StatDamageDecrease(this));
        this.registerStat(new StatDamageIncrease(this));
        this.registerStat(new StatFireball(this));
        this.registerStat(new StatLightning(this));
        this.registerStat(new StatSelfEffect(this));
    }

    @Listener
    public void onStartingServer(GameStartingServerEvent event)
    {
        try
        {
            this.translation.info("elderguardian.load.start");
            this.loadConfig();
            this.saveConfig();
            this.translation.info("elderguardian.load.finish");
        }
        catch (IOException e)
        {
            throw Throwables.propagate(e);
        }
    }

    @Listener
    public void onStartedServer(GameStartedServerEvent event)
    {
        this.translation.info("elderguardian.enable");
        this.loreStatService.loadConfig(this.rootConfigNode.getNode(PLUGIN_ID, "enabled-modules"));
    }

    @Listener
    public void onStoppingServer(GameStoppingServerEvent event)
    {
        this.translation.info("elderguardian.disable");
        this.loreStatService.saveConfig(this.rootConfigNode.getNode(PLUGIN_ID, "enabled-modules"));
    }

    @Listener
    public void onReload(GameReloadEvent event)
    {
        try
        {
            MessageReceiver src = event.getCause().first(CommandSource.class).orElse(Sponge.getServer().getConsole());
            src.sendMessage(this.translation.take("elderguardian.reload.start"));
            this.loadConfig();
            this.saveConfig();
            src.sendMessage(this.translation.take("elderguardian.reload.finish"));
        }
        catch (IOException e)
        {
            throw Throwables.propagate(e);
        }
    }

    private void registerStat(ElderGuardianStatBase stat)
    {
        this.stats.add(stat);
        this.loreStatService.registerStat(stat);
    }

    private void loadConfig() throws IOException
    {
        CommentedConfigurationNode root = config.load();

        this.stats.forEach(stat -> stat.loadConfig(root.getNode(stat.getLoreStatId().replace('_', '-'))));

        this.rootConfigNode = root;
    }

    private void saveConfig() throws IOException
    {
        CommentedConfigurationNode root = Optional.ofNullable(this.rootConfigNode).orElseGet(config::createEmptyNode);

        this.stats.forEach(stat -> stat.saveConfig(root.getNode(stat.getLoreStatId().replace('_', '-'))));

        config.save(root);
    }

    public Logger getLogger()
    {
        return this.logger;
    }

    public ElderGuardianTranslation getTranslation()
    {
        return this.translation;
    }

    public ElderGuardianService getLoreStatService()
    {
        return this.loreStatService;
    }
}
