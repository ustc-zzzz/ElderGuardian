package com.github.ustc_zzzz.elderguardian.api;

import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.util.Collection;
import java.util.Map;

/**
 * @author ustc_zzzz
 */
@NonnullByDefault
public interface LoreStatPresetsHandler
{
    Collection<String> getAvailableLoreStatPresets();

    Map<String, String> getLoreStatPresets(String id);

    default DataContainer getLoreStatPresetsContainer(String id)
    {
        DataContainer dataContainer = new MemoryDataContainer();
        for (Map.Entry<String, String> entry : this.getLoreStatPresets(id).entrySet())
        {
            dataContainer.set(DataQuery.of(entry.getKey()), entry.getValue());
        }
        return dataContainer;
    }

    void clearLoreStatPresets(String id);

    void addLoreStatPreset(String id, String key, String value);
}
