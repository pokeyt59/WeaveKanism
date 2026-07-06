package mekanism.fabric_shim.registries.datamaps;

import org.jetbrains.annotations.Nullable;

/**
 * An object that can have data-map values attached (stand-in for NeoForge's IWithData; same
 * surface). On NeoForge, vanilla Holder implements this via a class patch — on this port, plain
 * Holder call sites go through {@link DataMaps#getData} instead.
 */
public interface IWithData<R> {

    @Nullable
    default <T> T getData(DataMapType<R, T> type) {
        return null;
    }
}
