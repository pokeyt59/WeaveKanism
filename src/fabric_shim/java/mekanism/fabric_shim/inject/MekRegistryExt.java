package mekanism.fabric_shim.inject;

import java.util.Collections;
import java.util.Map;
import mekanism.fabric_shim.registries.datamaps.DataMapType;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

/**
 * NeoForge patches vanilla {@code Registry} with {@code getDataMap(DataMapType)} (all entries carrying
 * a data map). Injected onto Registry + RegistryMixin (interface target). Served from the shared
 * {@code DataMaps} store (per-type, not per-registry-instance — a type is bound to one registry
 * anyway, so the same values come back no matter which registry object is asked).
 */
public interface MekRegistryExt {

    default <R, T> Map<ResourceKey<R>, T> getDataMap(DataMapType<R, T> type) {
        return mekanism.fabric_shim.registries.datamaps.DataMaps.getValues(type);
    }

    //NeoForge IRegistryExtension.getKeyOrNull is vanilla getKey(value) under a nullable-explicit name.
    @Nullable
    @SuppressWarnings("unchecked")
    default ResourceLocation getKeyOrNull(Object element) {
        return ((Registry<Object>) this).getKey(element);
    }
}
