package com.github.ustc_zzzz.elderguardian.command;

import com.github.ustc_zzzz.elderguardian.ElderGuardian;
import com.github.ustc_zzzz.elderguardian.ElderGuardianTranslation;
import com.github.ustc_zzzz.elderguardian.api.LoreMatcher;
import com.github.ustc_zzzz.elderguardian.api.LoreTemplate;
import com.github.ustc_zzzz.elderguardian.service.ElderGuardianService;
import com.github.ustc_zzzz.elderguardian.util.ElderGuardianHelper;
import org.spongepowered.api.command.*;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.args.parsing.InputTokenizer;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.HandType;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.transaction.InventoryTransactionResult;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;
import org.spongepowered.api.text.serializer.TextSerializers;
import org.spongepowered.api.util.Tuple;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author ustc_zzzz
 */
@NonnullByDefault
public class ElderGuardianCommandManager implements Supplier<CommandCallable>
{
    private final ElderGuardian plugin;
    private final ElderGuardianService service;
    private final ElderGuardianTranslation translation;

    private final CommandSpec listCommand;
    private final CommandSpec loadCommand;
    private final CommandSpec saveCommand;

    private final CommandSpec presetsListCommand;
    private final CommandSpec presetsSetCommand;
    private final CommandSpec presetsClearCommand;

    private final CommandSpec matcherListCommand;
    private final CommandSpec matcherAddCommand;
    private final CommandSpec matcherClearCommand;
    private final CommandSpec matcherApplyCommand;

    public ElderGuardianCommandManager(ElderGuardian plugin)
    {
        this.plugin = plugin;
        this.service = plugin.getLoreStatService();
        this.translation = plugin.getTranslation();

        this.listCommand = CommandSpec.builder()
                .arguments(
                        GenericArguments.optional(
                                GenericArguments.string(Text.of("wildcard"))))
                .executor(this::executeList).build();
        this.loadCommand = CommandSpec.builder()
                .arguments(
                        GenericArguments.playerOrSource(Text.of("player")),
                        GenericArguments.string(Text.of("key")))
                .executor(this::executeLoad).build();
        this.saveCommand = CommandSpec.builder()
                .arguments(
                        GenericArguments.flags().flag("f").buildWith(
                                GenericArguments.string(Text.of("key"))))
                .executor(this::executeSave).build();

        this.presetsListCommand = CommandSpec.builder()
                .arguments(
                        GenericArguments.string(Text.of("key")))
                .executor(this::executePresetsList).build();
        this.presetsSetCommand = CommandSpec.builder()
                .arguments(
                        GenericArguments.string(Text.of("key")),
                        GenericArguments.string(Text.of("preset-key")),
                        GenericArguments.string(Text.of("preset-value")))
                .executor(this::executePresetsSet).build();
        this.presetsClearCommand = CommandSpec.builder()
                .arguments(
                        GenericArguments.string(Text.of("key")),
                        GenericArguments.string(Text.of("preset-key")))
                .executor(this::executePresetsClear).build();

        this.matcherListCommand = CommandSpec.builder()
                .arguments(
                        GenericArguments.optional(
                                GenericArguments.string(Text.of("wildcard"))))
                .executor(this::executeMatcherList).build();
        this.matcherAddCommand = CommandSpec.builder()
                .arguments(
                        GenericArguments.flags().valueFlag(
                                GenericArguments.string(Text.of("open-arg")), "-open-arg").valueFlag(
                                GenericArguments.string(Text.of("close-arg")), "-close-arg").buildWith(
                                GenericArguments.seq(
                                        GenericArguments.string(Text.of("key")),
                                        GenericArguments.allOf(
                                                GenericArguments.string(Text.of("template"))))))
                .inputTokenizer(InputTokenizer.quotedStrings(true))
                .executor(this::executeMatcherAdd).build();
        this.matcherClearCommand = CommandSpec.builder()
                .arguments(
                        GenericArguments.string(Text.of("key")))
                .executor(this::executeMatcherClear).build();
        this.matcherApplyCommand = CommandSpec.builder()
                .arguments(
                        new LoreMatcherCommandElement(this.plugin, Text.of("lore-matcher")),
                        new LoreMatcherArgCommandElement(this.plugin, Text.of("lore-matcher"), Text.of("args")))
                .inputTokenizer(InputTokenizer.quotedStrings(true))
                .executor(this::executeMatcherApply).build();
    }

    private CommandResult executeList(CommandSource src, CommandContext args) throws CommandException
    {
        String wildcard = args.<String>getOne(Text.of("wildcard")).orElse("");
        this.checkPermission(src, "elderguardian.list", "elderguardian.command.list.noPermission");

        List<String> items = this.service.listStacks().stream().filter(key ->
        {
            boolean canLoad = src.hasPermission("elderguardian.load." + key);
            boolean canSave = src.hasPermission("elderguardian.save." + key);
            return (wildcard.isEmpty() || ElderGuardianHelper.matchWildcard(wildcard, key)) && (canLoad || canSave);
        }).collect(Collectors.toList());

        List<Text> lines = items.stream().flatMap(key ->
        {
            String command = "/elderguardian load " + key;
            ItemStackSnapshot stack = this.service.getStack(key);
            int loreCount = stack.get(Keys.ITEM_LORE).map(List::size).orElse(0);
            return Stream.of(
                    Text.of("* " + key),
                    Text.of(" |- ", Text.builder("[give]")
                            .color(TextColors.BLUE)
                            .style(TextStyles.UNDERLINE)
                            .onHover(TextActions.showText(Text.of("Execute \"" + command + "\"")))
                            .onClick(TextActions.runCommand(command))),
                    Text.of(" |- ", Text.builder(stack.createStack().toString())
                            .color(TextColors.LIGHT_PURPLE)
                            .style(TextStyles.UNDERLINE)
                            .onHover(TextActions.showItem(stack))),
                    Text.of(" |- ", this.translation.take("elderguardian.command.list.showLore", loreCount)));
        }).collect(Collectors.toList());

        if (!items.isEmpty())
        {
            PaginationList.builder()
                    .linesPerPage(lines.size() / items.size() * 2 + 2)
                    .title(this.translation.take("elderguardian.command.list.header", items.size()))
                    .contents(lines).sendTo(src);
        }
        else
        {
            src.sendMessage(this.translation.take("elderguardian.command.list.showEmpty"));
        }
        return CommandResult.success();
    }

    private CommandResult executeLoad(CommandSource src, CommandContext args) throws CommandException
    {
        // noinspection ConstantConditions
        String key = args.<String>getOne(Text.of("key")).get();
        this.checkPermission(src, "elderguardian.load." + key, "elderguardian.command.load.noPermission");

        ItemStackSnapshot stackSnapshot = this.service.getStack(key);
        if (stackSnapshot == ItemStackSnapshot.NONE)
        {
            Text msg = this.translation.take("elderguardian.command.load.itemUnavailable", key);
            throw new CommandException(msg);
        }

        Collection<Player> players = args.getAll(Text.of("player"));
        for (Player player : players)
        {
            InventoryTransactionResult result = player.getInventory().offer(stackSnapshot.createStack());
            if (InventoryTransactionResult.Type.SUCCESS.equals(result.getType()))
            {
                src.sendMessage(this.translation.take("elderguardian.command.load.itemLoadedSuccessfully", key));
            }
            else
            {
                src.sendMessage(this.translation.take("elderguardian.command.load.itemFailedToLoad", key));
            }
        }

        return CommandResult.success();
    }

    private CommandResult executeSave(CommandSource src, CommandContext args) throws CommandException
    {
        // noinspection ConstantConditions
        String key = args.<String>getOne(Text.of("key")).get();
        Player player = this.checkPlayer(src, "elderguardian.command.save.notThePlayer");
        this.checkPermission(src, "elderguardian.save." + key, "elderguardian.command.save.noPermission");

        ItemStackSnapshot stackSnapshot = this.service.getStack(key);
        if (stackSnapshot != ItemStackSnapshot.NONE && !args.hasAny(Text.of("f")))
        {
            throw new CommandException(this.translation.take("elderguardian.command.save.itemExisted"));
        }

        Optional<ItemStack> playerStack = player.getItemInHand(HandTypes.MAIN_HAND);
        if (!playerStack.isPresent()) playerStack = player.getItemInHand(HandTypes.OFF_HAND);
        if (!playerStack.isPresent())
        {
            throw new CommandException(this.translation.take("elderguardian.command.save.itemUnavailable"));
        }

        this.service.setStack(key, playerStack.get().createSnapshot());
        src.sendMessage(this.translation.take("elderguardian.command.save.itemSavedSuccessfully", key));

        return CommandResult.success();
    }

    private CommandResult executePresetsList(CommandSource src, CommandContext args) throws CommandException
    {
        // noinspection ConstantConditions
        String key = args.<String>getOne(Text.of("key")).get();
        this.checkPermission(src, "elderguardian.presets.list", "elderguardian.command.presetsList.noPermission");
        Map<String, String> presets = this.service.getLoreStatPresets(key);
        if (presets.isEmpty())
        {
            src.sendMessage(this.translation.take("elderguardian.command.presetsList.showEmpty", key));
        }
        else
        {
            boolean notFirstJoin = false;
            Text.Builder builder = Text.builder();
            builder.append(Text.of(TextColors.GREEN, "("));
            for (Map.Entry<String, String> entry : presets.entrySet())
            {
                if (notFirstJoin) builder.append(Text.of(TextColors.GREEN, ", "));
                builder.append(Text.of(TextColors.BLUE, entry.getKey()));
                builder.append(Text.of(TextColors.GREEN, " => "));
                builder.append(Text.of(TextColors.LIGHT_PURPLE, entry.getValue()));
                notFirstJoin = true;
            }
            builder.append(Text.of(TextColors.GREEN, ")"));
            src.sendMessage(this.translation.take("elderguardian.command.presetsList.showNonEmpty", key, presets.size()));
            src.sendMessage(builder.build());
        }
        return CommandResult.success();
    }

    private CommandResult executePresetsSet(CommandSource src, CommandContext args) throws CommandException
    {
        // noinspection ConstantConditions
        String key = args.<String>getOne(Text.of("key")).get();
        this.checkPermission(src, "elderguardian.presets.modify", "elderguardian.command.presetsSet.noPermission");
        // noinspection ConstantConditions
        String presetKey = args.<String>getOne(Text.of("preset-key")).get();
        // noinspection ConstantConditions
        String presetValue = args.<String>getOne(Text.of("preset-value")).get();

        this.service.addLoreStatPreset(key, presetKey, presetValue);
        src.sendMessage(this.translation.take("elderguardian.command.presetsSet.doneSuccessfully", presetKey, presetValue));
        return CommandResult.success();
    }

    private CommandResult executePresetsClear(CommandSource src, CommandContext args) throws CommandException
    {
        // noinspection ConstantConditions
        String key = args.<String>getOne(Text.of("key")).get();
        this.checkPermission(src, "elderguardian.presets.modify", "elderguardian.command.presetsClear.noPermission");
        // noinspection ConstantConditions
        String presetKey = args.<String>getOne(Text.of("preset-key")).get();
        if ("--all".equals(presetKey))
        {
            this.service.clearLoreStatPresets(key);
            src.sendMessage(this.translation.take("elderguardian.command.presetsClear.clearedAllSuccessfully", key));
        }
        else
        {
            this.service.addLoreStatPreset(key, presetKey, "");
            src.sendMessage(this.translation.take("elderguardian.command.presetsClear.clearedSuccessfully", key, presetKey));
        }
        return CommandResult.success();
    }

    private CommandResult executeMatcherList(CommandSource src, CommandContext args) throws CommandException
    {
        String wildcard = args.<String>getOne(Text.of("wildcard")).orElse("");
        this.checkPermission(src, "elderguardian.matcher.list", "elderguardian.command.matcherList.noPermission");

        List<Tuple<String, Tuple<Integer, LoreMatcher>>> items = this.service.getAvailableLoreMatchers().stream().filter(key ->
        {
            boolean canLoad = src.hasPermission("elderguardian.matcher.load." + key);
            boolean canSave = src.hasPermission("elderguardian.matcher.save." + key);
            return (wildcard.isEmpty() || ElderGuardianHelper.matchWildcard(wildcard, key)) && (canLoad || canSave);
        }).flatMap(key ->
        {
            List<LoreMatcher> matchers = this.service.getLoreMatchers(key);
            Stream.Builder<Tuple<String, Tuple<Integer, LoreMatcher>>> builder = Stream.builder();
            for (int i = 0; i < matchers.size(); i++) builder.add(Tuple.of(key, Tuple.of(i, matchers.get(i))));
            return builder.build();
        }).collect(Collectors.toList());

        List<Text> lines = items.stream().flatMap(tuple ->
        {
            String key = tuple.getFirst();
            Tuple<Integer, LoreMatcher> value = tuple.getSecond();

            int index = value.getFirst();
            LoreMatcher matcher = value.getSecond();
            int size = matcher.getLoreTemplates().size();

            String command = "/elderguardian matcher-apply " + key + " " + index;
            Stream.Builder<Text> builder = Stream.builder();
            builder.add(Text.of("* " + key + " (" + ElderGuardianHelper.indexToOrdinalString(index) + ")"));
            List<Text> lores = matcher.getLoreTemplates().stream()
                    .map(t -> Text.of(TextColors.LIGHT_PURPLE, t.toString())).collect(Collectors.toList());
            builder.add(Text.of(" |- ", Text.builder("[apply]")
                    .color(TextColors.BLUE)
                    .style(TextStyles.UNDERLINE)
                    .onHover(TextActions.showText(Text.of("Execute \"" + command + "\"")))
                    .onClick(TextActions.suggestCommand(command + " ")), Text.of(" "), Text.builder("[lores]")
                    .color(TextColors.BLUE)
                    .style(TextStyles.UNDERLINE)
                    .onHover(TextActions.showText(Text.joinWith(Text.of("\n"), lores)))));
            builder.add(Text.of(" |- ", this.translation.take("elderguardian.command.matcherList.showLines", size)));
            return builder.build();
        }).collect(Collectors.toList());

        if (!items.isEmpty())
        {
            PaginationList.builder()
                    .linesPerPage(lines.size() / items.size() * 4 + 2)
                    .title(this.translation.take("elderguardian.command.matcherList.header", items.size()))
                    .contents(lines).sendTo(src);
        }
        else
        {
            src.sendMessage(this.translation.take("elderguardian.command.matcherList.showEmpty"));
        }
        return CommandResult.success();
    }

    private CommandResult executeMatcherAdd(CommandSource src, CommandContext args) throws CommandException
    {
        // noinspection ConstantConditions
        String key = args.<String>getOne(Text.of("key")).get();
        this.checkPermission(src, "elderguardian.matcher.modify", "elderguardian.command.matcherAdd.noPermission");
        Collection<String> templates = args.getAll(Text.of("template"));
        if (templates.isEmpty())
        {
            throw new CommandException(this.translation.take("elderguardian.command.matcherAdd.noEmptyTemplate"));
        }
        Optional<String> openArg = args.getOne(Text.of("open-arg"));
        Optional<String> closeArg = args.getOne(Text.of("close-arg"));
        DataContainer data = new MemoryDataContainer();
        data.set(LoreMatcher.TEMPLATES, templates);
        openArg.ifPresent(s -> data.set(LoreMatcher.OPEN_ARG, s));
        closeArg.ifPresent(s -> data.set(LoreMatcher.CLOSE_ARG, s));
        this.service.addLoreMatcher(key, LoreMatcher.fromContainer(data));
        src.sendMessage(this.translation.take("elderguardian.command.matcherAdd.matcherAddedSuccessfully", key));
        return CommandResult.success();
    }

    private CommandResult executeMatcherClear(CommandSource src, CommandContext args) throws CommandException
    {
        // noinspection ConstantConditions
        String key = args.<String>getOne(Text.of("key")).get();
        this.checkPermission(src, "elderguardian.matcher.modify", "elderguardian.command.matcherClear.noPermission");
        if ("--all".equals(key))
        {
            for (String id : this.service.getAvailableLoreMatchers()) this.service.clearLoreMatchers(id);
            src.sendMessage(this.translation.take("elderguardian.command.matcherClear.clearedAllSuccessfully"));
            return CommandResult.success();
        }
        else
        {
            List<LoreMatcher> matchers = this.service.getLoreMatchers(key);
            if (matchers.isEmpty())
            {
                throw new CommandException(this.translation.take("elderguardian.command.matcherClear.alreadyEmpty", key));
            }
            this.service.clearLoreMatchers(key);
            src.sendMessage(this.translation.take("elderguardian.command.matcherClear.clearedSuccessfully"));
            return CommandResult.success();
        }
    }

    private CommandResult executeMatcherApply(CommandSource src, CommandContext args) throws CommandException
    {
        // noinspection ConstantConditions
        LoreMatcher matcher = args.<LoreMatcher>getOne(Text.of("lore-matcher")).get();
        this.checkPermission(src, "elderguardian.matcher.apply", "elderguardian.command.matcherApply.noPermission");
        Player player = this.checkPlayer(src, "elderguardian.command.matcherApply.notThePlayer");
        // noinspection ConstantConditions
        Map<String, String> arguments = args.<Map<String, String>>getOne(Text.of("args")).get();
        DataContainer data = new MemoryDataContainer();
        for (Map.Entry<String, String> e : arguments.entrySet()) data.set(DataQuery.of(e.getKey()), e.getValue());
        HandType handType = HandTypes.MAIN_HAND;
        Optional<ItemStack> stackOptional = player.getItemInHand(HandTypes.MAIN_HAND);
        if (!stackOptional.isPresent())
        {
            handType = HandTypes.OFF_HAND;
            stackOptional = player.getItemInHand(HandTypes.OFF_HAND);
        }
        if (!stackOptional.isPresent())
        {
            throw new CommandException(this.translation.take("elderguardian.command.matcherApply.itemUnavailable"));
        }
        ItemStack playerStack = stackOptional.get();
        List<Text> lores = playerStack.get(Keys.ITEM_LORE).map(LinkedList::new).orElseGet(LinkedList::new);
        for (LoreTemplate template : matcher.getLoreTemplates())
        {
            for (String s : template.getTemplateArgs())
            {
                DataQuery query = DataQuery.of(s);
                if (!data.contains(query))
                {
                    player.sendMessage(this.translation.take("elderguardian.command.matcherApply.ignoreArgument", s));
                    data.set(query, "");
                }
            }
            template.translate(data).ifPresent(text ->
            {
                lores.add(text);
                String unformattedText = TextSerializers.FORMATTING_CODE.serialize(text);
                player.sendMessage(this.translation.take("elderguardian.command.matcherApply.processing", unformattedText));
            });
        }
        playerStack.offer(Keys.ITEM_LORE, lores);
        player.setItemInHand(handType, playerStack);
        player.sendMessage(this.translation.take("elderguardian.command.matcherApply.loreAppliedSuccessfully"));
        return CommandResult.success();
    }

    private CommandSource checkPermission(CommandSource src, String p, String err) throws CommandPermissionException
    {
        if (!src.hasPermission(p))
        {
            Text msg = this.translation.take(err, p);
            throw new CommandPermissionException(msg);
        }
        return src;
    }

    private Player checkPlayer(CommandSource src, String err) throws CommandException
    {
        if (!(src instanceof Player))
        {
            Text msg = this.translation.take(err);
            throw new CommandException(msg);
        }
        return (Player) src;
    }

    @Override
    public CommandCallable get()
    {
        return CommandSpec.builder()
                .child(this.listCommand, "list", "l")
                .child(this.loadCommand, "load", "give", "g")
                .child(this.saveCommand, "save", "s")
                .child(this.presetsListCommand, "presets-list", "pl")
                .child(this.presetsSetCommand, "presets-set", "ps")
                .child(this.presetsClearCommand, "presets-clear", "pc")
                .child(this.matcherListCommand, "matcher-list", "ml")
                .child(this.matcherAddCommand, "matcher-add", "mp")
                .child(this.matcherClearCommand, "matcher-clear", "mc")
                .child(this.matcherApplyCommand, "matcher-apply", "ma")
                .description(this.translation.take("elderguardian.commandDescription")).build();
    }
}
