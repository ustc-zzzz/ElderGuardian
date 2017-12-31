package com.github.ustc_zzzz.elderguardian.command;

import com.github.ustc_zzzz.elderguardian.ElderGuardian;
import com.github.ustc_zzzz.elderguardian.ElderGuardianTranslation;
import com.github.ustc_zzzz.elderguardian.service.ElderGuardianService;
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
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * @author ustc_zzzz
 */
@NonnullByDefault
public class LoreMatcherCommandElement extends CommandElement
{
    private final ElderGuardianService service;
    private final ElderGuardianTranslation translation;

    LoreMatcherCommandElement(ElderGuardian plugin, @Nullable Text key)
    {
        super(key);
        this.service = plugin.getLoreStatService();
        this.translation = plugin.getTranslation();
    }

    @Nullable
    @Override
    protected Object parseValue(CommandSource source, CommandArgs args) throws ArgumentParseException
    {
        String key = args.next(), indexString = args.next();
        try
        {
            int index = Integer.parseInt(indexString);
            return this.service.getLoreMatchers(key).get(index);
        }
        catch (IndexOutOfBoundsException | NumberFormatException e)
        {
            Text message = this.translation.take("elderguardian.command.matcherApply.noSuchIndex", key, indexString);
            throw args.createError(message);
        }
    }

    @Override
    public List<String> complete(CommandSource src, CommandArgs args, CommandContext context)
    {
        String keyPrefix = args.nextIfPresent().orElse("");
        if (args.hasNext())
        {
            String intPrefix = args.nextIfPresent().orElse("");
            String[] indices = new String[this.service.getLoreMatchers(keyPrefix).size()];
            for (int i = 0; i < indices.length; i++) indices[i] = Integer.toString(i);
            StartsWithPredicate predicate = new StartsWithPredicate(intPrefix);
            return Arrays.stream(indices).filter(predicate).collect(GuavaCollectors.toImmutableList());
        }
        else
        {
            Collection<String> all = this.service.getAvailableLoreMatchers();
            return all.stream().filter(new StartsWithPredicate(keyPrefix)).collect(GuavaCollectors.toImmutableList());
        }
    }
}
