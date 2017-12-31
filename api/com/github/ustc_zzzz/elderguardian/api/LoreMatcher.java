package com.github.ustc_zzzz.elderguardian.api;

import com.google.common.collect.ImmutableList;
import org.spongepowered.api.data.*;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.util.*;

/**
 * @author ustc_zzzz
 */
@NonnullByDefault
public final class LoreMatcher implements DataSerializable
{
    public static final DataQuery OPEN_ARG = DataQuery.of("OpenArg");
    public static final DataQuery CLOSE_ARG = DataQuery.of("CloseArg");
    public static final DataQuery TEMPLATES = DataQuery.of("Templates");

    public static final String DEFAULT_OPEN_ARG = "{{";
    public static final String DEFAULT_CLOSE_ARG = "}}";

    public static LoreMatcher fromContainer(DataView data) throws InvalidDataException
    {
        String openArg = data.getString(OPEN_ARG).orElse(DEFAULT_OPEN_ARG);
        String closeArg = data.getString(CLOSE_ARG).orElse(DEFAULT_CLOSE_ARG);
        List<String> templates = data.getStringList(TEMPLATES).orElse(ImmutableList.of());
        if (templates.isEmpty()) throw new InvalidDataException("The templates should not be empty");

        return new LoreMatcher(openArg, closeArg, templates);
    }

    private final String openArg;
    private final String closeArg;
    private final List<String> rawTemplates;
    private final List<LoreTemplate> loreTemplates;

    private final int loreTemplateSize;

    private LoreMatcher(String openArg, String closeArg, Collection<String> templates)
    {
        this.openArg = Objects.requireNonNull(openArg);
        this.closeArg = Objects.requireNonNull(closeArg);

        ImmutableList.Builder<String> rawTemplatesBuilder = ImmutableList.builder();
        ImmutableList.Builder<LoreTemplate> loreTemplatesBuilder = ImmutableList.builder();

        for (String template : Objects.requireNonNull(templates))
        {
            rawTemplatesBuilder.add(template);
            loreTemplatesBuilder.add(LoreTemplate.of(template, openArg, closeArg));
        }

        this.rawTemplates = rawTemplatesBuilder.build();
        this.loreTemplates = loreTemplatesBuilder.build();

        this.loreTemplateSize = this.loreTemplates.size();
        if (this.loreTemplateSize == 0) throw new IllegalArgumentException("The templates should not be empty");
    }

    private Optional<DataContainer> matchWithOffset(List<Text> lores, int offset, DataContainer presets)
    {
        DataContainer container = presets.copy();
        for (LoreTemplate template : this.loreTemplates)
        {
            Optional<DataContainer> containerOptional = template.translate(lores.get(offset++));

            if (!containerOptional.isPresent()) return Optional.empty();
            Map<DataQuery, Object> values = containerOptional.get().getValues(true);
            for (Map.Entry<DataQuery, Object> e : values.entrySet())
            {
                Object value = e.getValue();
                if (!value.toString().isEmpty()) container.set(e.getKey(), value);
            }
        }
        return Optional.of(container);
    }

    public List<DataContainer> match(List<Text> lores, LoreMatcherContext context, DataContainer presets)
    {
        int loreSize = lores.size();
        int maxOffsetAvailable = loreSize - this.loreTemplateSize;

        ImmutableList.Builder<DataContainer> builder = ImmutableList.builder();
        for (int i = 0; i <= maxOffsetAvailable; ++i) this.matchWithOffset(lores, i, presets).ifPresent(builder::add);

        return builder.build();
    }

    public String getOpenArg()
    {
        return this.openArg;
    }

    public String getCloseArg()
    {
        return this.closeArg;
    }

    public List<String> getTemplates()
    {
        return this.rawTemplates;
    }

    public List<LoreTemplate> getLoreTemplates()
    {
        return this.loreTemplates;
    }

    @Override
    public DataContainer toContainer()
    {
        MemoryDataContainer container = new MemoryDataContainer();
        return container.set(OPEN_ARG, this.openArg).set(CLOSE_ARG, this.closeArg).set(TEMPLATES, this.rawTemplates);
    }

    @Override
    public int getContentVersion()
    {
        return 0;
    }
}
