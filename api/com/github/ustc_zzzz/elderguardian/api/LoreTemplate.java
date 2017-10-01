package com.github.ustc_zzzz.elderguardian.api;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.TextRepresentable;
import org.spongepowered.api.text.serializer.TextSerializers;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * A class for translating between a text and a data container by a template.
 * <p>
 * For example:
 * <br>&nbsp;&nbsp;&nbsp;&nbsp;Template: "Skill: {{name}} (Cooldown {{cooldown}})"
 * <br>&nbsp;&nbsp;&nbsp;&nbsp;Translation: "Skill: arrow (Cooldown 100)" <==> {name: "arrow", cooldown: "100"}
 * </p>
 *
 * @author ustc_zzzz
 * @see LoreStat
 */
@NonnullByDefault
public final class LoreTemplate
{
    /**
     * Construct a LoreTemplate
     * <p>
     * An arg in the template string has an openArg and a closeArg.
     * For example:
     * <br>&nbsp;&nbsp;&nbsp;&nbsp;Template: "Skill: {{name}} (Cooldown {{cooldown}})"
     * <br>&nbsp;&nbsp;&nbsp;&nbsp;Arg names: "name", "cooldown"
     * <br>&nbsp;&nbsp;&nbsp;&nbsp;The openArg: "{{"
     * <br>&nbsp;&nbsp;&nbsp;&nbsp;The closeArg: "}}"
     * <p>
     *
     * @param template the template
     * @param openArg  the openArg
     * @param closeArg the closeArg
     * @return an instance of LoreTemplate
     */
    public static LoreTemplate of(String template, String openArg, String closeArg)
    {
        return new LoreTemplate(template, openArg, closeArg);
    }

    /**
     * Translate a text to a data container
     *
     * @param text text to be translated
     * @return the data container if the template matches the text, otherwise an empty
     */
    public Optional<DataContainer> translate(TextRepresentable text)
    {
        String firstPart = this.templateParts.get(0);
        String textString = TextSerializers.FORMATTING_CODE.serialize(text.toText());
        if (!textString.startsWith(firstPart)) return Optional.empty();
        return this.deserializeString(textString, firstPart.length(), 1);
    }

    /**
     * Translate a data container to a text
     *
     * @param data data container to be translated
     * @return the text if all the parameters are available in the data container, otherwise an empty
     */
    public Optional<Text> translate(DataView data)
    {
        boolean isArg = false;
        StringBuilder stringBuilder = new StringBuilder();
        for (String part : this.templateParts)
        {
            if (isArg)
            {
                Optional<String> stringOptional = data.getString(DataQuery.of('.', part));
                if (!stringOptional.isPresent()) return Optional.empty();
                stringBuilder.append(stringOptional.get());
                isArg = false;
            }
            else
            {
                stringBuilder.append(part);
                isArg = true;
            }
        }
        return Optional.of(TextSerializers.FORMATTING_CODE.deserialize(stringBuilder.toString()));
    }

    /**
     * Fetch all the template args, in which there may be duplicate args whose names are the same.
     *
     * @return all the template args
     */
    public List<String> getTemplateArgs()
    {
        ImmutableList.Builder<String> builder = ImmutableList.builder();
        for (int i = 1; i < this.templateParts.size(); i += 2)
        {
            builder.add(this.templateParts.get(i));
        }
        return builder.build();
    }

    @Override
    public String toString()
    {
        return this.rawTemplate;
    }

    @Override
    public int hashCode()
    {
        return Objects.hashCode(this.templateParts);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (!(obj instanceof LoreTemplate)) return false;
        LoreTemplate that = (LoreTemplate) obj;
        return Objects.equals(this.templateParts, that.templateParts);
    }

    private final String rawTemplate;
    private final List<String> templateParts; // [String, Arg, String, Arg, ..., Arg, String], String may be empty

    private LoreTemplate(String template, String openArg, String closeArg)
    {
        int startIndex = 0;
        this.rawTemplate = Objects.requireNonNull(template, "The template should not be null");
        Objects.requireNonNull(Strings.emptyToNull(openArg), "The openArg should not be empty or null");
        Objects.requireNonNull(Strings.emptyToNull(closeArg), "The closeArg should not be empty or null");
        ImmutableList.Builder<String> builder = ImmutableList.builder();
        while (true)
        {
            int openIndex = template.indexOf(openArg, startIndex);
            int closeIndex = template.indexOf(closeArg, startIndex);
            if (openIndex + openArg.length() > closeIndex || 0 > openIndex) break;
            builder.add(template.substring(startIndex, openIndex));
            builder.add(template.substring(openIndex + openArg.length(), closeIndex));
            startIndex = closeIndex + closeArg.length();
        }
        builder.add(template.substring(startIndex));
        this.templateParts = builder.build();
    }

    // match an Arg with a String
    private Optional<DataContainer> deserializeString(String string, int indexFromString, int indexFromTemplate)
    {
        if (indexFromTemplate < this.templateParts.size())
        {
            int oldIndex = indexFromString;
            String argName = this.templateParts.get(indexFromTemplate);
            while (true)
            {
                String newPart = this.templateParts.get(indexFromTemplate + 1);
                oldIndex = string.indexOf(newPart, oldIndex);
                if (0 > oldIndex) return Optional.empty();
                int index = oldIndex + newPart.length();
                Optional<DataContainer> data = this.deserializeString(string, index, indexFromTemplate + 2);
                if (data.isPresent())
                {
                    String arg = string.substring(indexFromString, oldIndex);
                    return Optional.of(data.get().set(DataQuery.of('.', argName), arg));
                }
                ++oldIndex;
            }
        }
        return indexFromString < string.length() ? Optional.empty() : Optional.of(new MemoryDataContainer());
    }
}
