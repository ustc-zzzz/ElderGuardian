package com.github.ustc_zzzz.elderguardian.command;

import com.github.ustc_zzzz.elderguardian.ElderGuardian;
import com.github.ustc_zzzz.elderguardian.ElderGuardianTranslation;
import com.github.ustc_zzzz.elderguardian.service.ElderGuardianService;
import com.github.ustc_zzzz.elderguardian.util.ElderGuardianHelper;
import org.spongepowered.api.command.*;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.data.key.Keys;
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
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
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

    public ElderGuardianCommandManager(ElderGuardian plugin)
    {
        this.plugin = plugin;
        this.service = plugin.getLoreStatService();
        this.translation = plugin.getTranslation();

        this.listCommand = CommandSpec.builder()
                .arguments(GenericArguments.optional(GenericArguments.string(Text.of("wildcard"))))
                .executor(this::executeList).build();
        this.loadCommand = CommandSpec.builder()
                .arguments(GenericArguments.playerOrSource(Text.of("player")), GenericArguments.string(Text.of("key")))
                .executor(this::executeLoad).build();
        this.saveCommand = CommandSpec.builder()
                .arguments(GenericArguments.flags().flag("f").buildWith(GenericArguments.string(Text.of("key"))))
                .executor(this::executeSave).build();
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

        PaginationList.builder()
                .linesPerPage(lines.size() / items.size() * 2 + 2)
                .title(this.translation.take("elderguardian.command.list.header", items.size()))
                .contents(lines).sendTo(src);
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
                .description(this.translation.take("elderguardian.commandDescription")).build();
    }
}
