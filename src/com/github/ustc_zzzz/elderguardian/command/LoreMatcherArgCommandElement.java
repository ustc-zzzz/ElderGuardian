package com.github.ustc_zzzz.elderguardian.command;

import com.github.ustc_zzzz.elderguardian.ElderGuardian;
import com.github.ustc_zzzz.elderguardian.ElderGuardianTranslation;
import com.github.ustc_zzzz.elderguardian.api.LoreMatcher;
import com.github.ustc_zzzz.elderguardian.api.LoreTemplate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.ArgumentParseException;
import org.spongepowered.api.command.args.CommandArgs;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.GuavaCollectors;
import org.spongepowered.api.util.StartsWithPredicate;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author ustc_zzzz
 */
@NonnullByDefault
public class LoreMatcherArgCommandElement extends CommandElement
{
    @Nullable
    private final Text loreMatcherArgName;
    private final ElderGuardianTranslation translation;

    LoreMatcherArgCommandElement(ElderGuardian plugin, @Nullable Text loreMatcherArgName, @Nullable Text key)
    {
        super(key);
        this.translation = plugin.getTranslation();
        this.loreMatcherArgName = loreMatcherArgName;
    }

    @Nullable
    @Override
    protected Object parseValue(CommandSource source, CommandArgs args) throws ArgumentParseException
    {
        List<String> arguments = new LinkedList<>();
        while (args.hasNext()) arguments.add(args.next());
        Map<String, String> argumentMap = new TreeMap<>();
        for (String argument : arguments)
        {
            int index = argument.indexOf('=');
            if (index <= 0)
            {
                throw args.createError(this.translation.take("elderguardian.command.matcherApply.invalidArg", argument));
            }
            String key = argument.substring(0, index), value = argument.substring(index + 1);
            if (argumentMap.containsKey(key))
            {
                throw args.createError(this.translation.take("elderguardian.command.matcherApply.duplicateArgKey", argument, key));
            }
            argumentMap.put(key, value);
        }
        return ImmutableMap.copyOf(argumentMap);
    }

    @Override
    public List<String> complete(CommandSource src, CommandArgs args, CommandContext context)
    {
        String prefix = "";
        Object startState = args.getState();
        while (args.hasNext()) prefix = args.nextIfPresent().orElse("");

        args.setState(startState);
        if (prefix.indexOf('=') >= 0) return ImmutableList.of();
        if (Objects.isNull(this.loreMatcherArgName)) return ImmutableList.of();
        // noinspection ConstantConditions
        List<LoreTemplate> templates = context.<LoreMatcher>getOne(this.loreMatcherArgName).get().getLoreTemplates();
        Set<String> argSet = templates.stream().flatMap(t -> t.getTemplateArgs().stream()).collect(Collectors.toSet());
        return argSet.stream().filter(new StartsWithPredicate(prefix)).map(s -> s + '=').collect(GuavaCollectors.toImmutableList());
    }
}
