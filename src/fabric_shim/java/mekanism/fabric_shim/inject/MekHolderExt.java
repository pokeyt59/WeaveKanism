package mekanism.fabric_shim.inject;

import mekanism.fabric_shim.registries.datamaps.DataMapType;
import mekanism.fabric_shim.registries.datamaps.DataMaps;
import net.minecraft.core.Holder;
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
}
