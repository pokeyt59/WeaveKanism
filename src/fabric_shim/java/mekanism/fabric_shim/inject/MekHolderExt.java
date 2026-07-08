package mekanism.fabric_shim.inject;

import mekanism.fabric_shim.registries.datamaps.DataMapType;
import mekanism.fabric_shim.registries.datamaps.DataMaps;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import org.jetbrains.annotations.Nullable;

/**
 * NeoForge patches vanilla {@link Holder} with {@code getData(DataMapType)} (data-map lookup by
 * registry entry). Injected onto Holder + HolderMixin (interface target); routes to the shim
 * {@link DataMaps} store populated during the registration lifecycle.
 */
public interface MekHolderExt {

    @Nullable
    @SuppressWarnings("unchecked")
    default <R, T> T getData(DataMapType<R, T> type) {
        return DataMaps.getData((Holder<R>) this, type);
    }

    //NeoForge IHolderExtension: getKey() returns the reference key (null for non-reference holders).
    @Nullable
    @SuppressWarnings("unchecked")
    default <K> ResourceKey<K> getKey() {
        return (ResourceKey<K>) ((Holder<?>) this).unwrapKey().orElse(null);
    }

    @SuppressWarnings("unchecked")
    default <K> Holder<K> getDelegate() {
        return (Holder<K>) this;
    }
}
