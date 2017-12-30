package com.github.ustc_zzzz.elderguardian;

import com.github.ustc_zzzz.elderguardian.service.ElderGuardianService;
import com.github.ustc_zzzz.elderguardian.stat.ElderGuardianStat;
import com.github.ustc_zzzz.elderguardian.stat.ElderGuardianStatBase;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.ClassPath;
import com.google.inject.Inject;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GameAboutToStartServerEvent;
import org.spongepowered.api.event.game.state.GamePostInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.game.state.GameStoppingServerEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.channel.MessageReceiver;

import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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
    @ConfigDir(sharedRoot = false)
    private Path configurationDir;

    @Inject
    @DefaultConfig(sharedRoot = false)
    private ConfigurationLoader<CommentedConfigurationNode> configurationLoader;

    private CommentedConfigurationNode rootConfig;

    private ElderGuardianTranslation translation;
    private ElderGuardianService loreStatService;

    private List<ElderGuardianStatBase> stats = new LinkedList<>();
    private Set<String> availableStats;

    @Listener
    public void onPostInitialization(GamePostInitializationEvent event)
    {
        this.translation = new ElderGuardianTranslation(this);
        this.loreStatService = new ElderGuardianService(this);
    }

    @Listener
    public void onAboutToStartServer(GameAboutToStartServerEvent event)
    {
        try
        {
            Class<? extends ElderGuardian> pluginClass = this.getClass();
            ClassPath classPath = ClassPath.from(pluginClass.getClassLoader());
            String packageName = ElderGuardianStat.class.getPackage().getName();
            for (ClassPath.ClassInfo info : classPath.getTopLevelClasses(packageName))
            {
                Class<?> statClass = info.load();
                if (statClass.isAnnotationPresent(ElderGuardianStat.class))
                {
                    Object instance = statClass.getConstructor(pluginClass).newInstance(this);
                    this.translation.info("elderguardian.register", statClass.getName());
                    this.registerStat((ElderGuardianStatBase) instance);
                }
            }
        }
        catch (IOException | ReflectiveOperationException | ClassCastException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Listener
    public void onStartedServer(GameStartedServerEvent event)
    {
        this.translation.info("elderguardian.load.start");

        this.loadConfig();

        this.loreStatService.enableStats(this.availableStats);
        this.translation.info("elderguardian.enable");

        this.saveConfig();

        this.translation.info("elderguardian.load.finish");

    }

    @Listener
    public void onStoppingServer(GameStoppingServerEvent event)
    {
        this.availableStats = this.loreStatService.disableStats();
        this.translation.info("elderguardian.disable");
    }

    @Listener
    public void onReload(GameReloadEvent event)
    {
        MessageReceiver src = event.getCause().first(CommandSource.class).orElse(Sponge.getServer().getConsole());
        src.sendMessage(this.translation.take("elderguardian.reload.start"));

        this.availableStats = this.loreStatService.disableStats();
        this.translation.info("elderguardian.disable");

        this.loadConfig();

        this.loreStatService.enableStats(this.availableStats);
        this.translation.info("elderguardian.enable");

        this.saveConfig();

        src.sendMessage(this.translation.take("elderguardian.reload.finish"));
    }

    private void registerStat(ElderGuardianStatBase stat)
    {
        this.stats.add(stat);
        this.loreStatService.registerStat(stat);
    }

    private Set<String> getEnabledStats(CommentedConfigurationNode node)
    {
        ImmutableSet.Builder<String> builder = ImmutableSet.builder();
        for (String id : this.loreStatService.getAvailableStats())
        {
            if (node.getNode(id.replace('_', '-')).getBoolean(true)) builder.add(id);
        }
        return builder.build();
    }

    private void setEnabledStats(CommentedConfigurationNode node, Set<String> enabledModules)
    {
        for (String id : this.loreStatService.getAvailableStats())
        {
            node.getNode(id.replace('_', '-')).setValue(enabledModules.contains(id));
        }
    }

    private void loadConfig()
    {
        try
        {
            CommentedConfigurationNode root = configurationLoader.load();

            this.availableStats = this.getEnabledStats(root.getNode(PLUGIN_ID, "enabled-modules"));
            this.loreStatService.loadConfig(root.getNode(PLUGIN_ID, "data-storage"));

            this.rootConfig = root;
        }
        catch (IOException e)
        {
            throw Throwables.propagate(e);
        }
    }

    private void saveConfig()
    {
        try
        {
            CommentedConfigurationNode root = Optional.ofNullable(this.rootConfig).orElseGet(configurationLoader::createEmptyNode);

            this.setEnabledStats(root.getNode(PLUGIN_ID, "enabled-modules"), availableStats);
            this.loreStatService.saveConfig(root.getNode(PLUGIN_ID, "data-storage"));

            configurationLoader.save(root);
        }
        catch (IOException e)
        {
            throw Throwables.propagate(e);
        }
    }

    public Logger getLogger()
    {
        return this.logger;
    }

    public Path getConfigurationDir()
    {
        return this.configurationDir;
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
