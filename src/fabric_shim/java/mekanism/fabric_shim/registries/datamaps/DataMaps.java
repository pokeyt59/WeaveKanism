package mekanism.fabric_shim.registries.datamaps;

import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

/**
 * Internal to the Fabric port (no direct NeoForge counterpart): central store for registered
 * {@link DataMapType data map types} and their loaded values.
 *
 * <p>On NeoForge, {@code Holder#getData(DataMapType)} is a patched-in vanilla method; on this port
 * those call sites are hand-edited to {@link #getData(Holder, DataMapType)}.
 *
 * <p>Values are populated by {@link DataMapLoader} (datapack reload listener + TAGS_LOADED
 * commit) reading NeoForge's {@code data/<ns>/data_maps/<registry>/<id>.json} format; before the
 * first load, lookups return {@code null}, matching "no data attached".
 */
public final class DataMaps {

    private DataMaps() {
    }

    private static final Map<ResourceKey<? extends Registry<?>>, Map<ResourceLocation, DataMapType<?, ?>>> TYPES = new HashMap<>();
    private static final Map<DataMapType<?, ?>, Map<ResourceKey<?>, Object>> VALUES = new IdentityHashMap<>();

    static synchronized void registerType(DataMapType<?, ?> type) {
        Map<ResourceLocation, DataMapType<?, ?>> byId = TYPES.computeIfAbsent(type.registryKey(), k -> new HashMap<>());
        if (byId.putIfAbsent(type.id(), type) != null) {
            throw new IllegalArgumentException("Duplicate data map type registration: " + type.id() + " for registry " + type.registryKey().location());
        }
    }

    public static Map<ResourceLocation, DataMapType<?, ?>> getTypes(ResourceKey<? extends Registry<?>> registryKey) {
        return Collections.unmodifiableMap(TYPES.getOrDefault(registryKey, Map.of()));
    }

    static java.util.Set<ResourceKey<? extends Registry<?>>> typedRegistries() {
        return Collections.unmodifiableSet(TYPES.keySet());
    }

    /**
     * The full loaded map for a data map type (empty until the loader commits). Registry-wide view
     * behind {@code Registry#getDataMap} (via {@code MekRegistryExt}).
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <R, T> Map<ResourceKey<R>, T> getValues(DataMapType<R, T> type) {
        Map<ResourceKey<?>, Object> values = VALUES.get(type);
        return values == null ? Map.of() : (Map) Collections.unmodifiableMap(values);
    }

    /**
     * Looks up the data-map value attached to the holder's registry entry, or null if none.
     * Replacement for NeoForge's {@code Holder#getData(DataMapType)}.
     */
    @Nullable
    @SuppressWarnings("unchecked")
    public static <R, T> T getData(Holder<R> holder, DataMapType<R, T> type) {
        Map<ResourceKey<?>, Object> values = VALUES.get(type);
        if (values == null) {
            return null;
        }
        return holder.unwrapKey().map(key -> (T) values.get(key)).orElse(null);
    }

    /**
     * Replaces the loaded values for a data map type (called by the reload listener).
     */
    static synchronized void setValues(DataMapType<?, ?> type, Map<ResourceKey<?>, Object> values) {
        VALUES.put(type, values);
    }
}
