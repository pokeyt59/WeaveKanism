package mekanism.fabric_shim.inject;

import mekanism.fabric_shim.client.model.data.ModelData;

/**
 * NeoForge {@code IBlockEntityExtension} surface Mekanism tiles call, injected onto vanilla
 * {@code BlockEntity} + BlockEntityMixin. {@code invalidateCapabilities} is a no-op for 1f (Phase 2 =
 * cache eviction); {@code getModelData} defaults to empty (tiles override it; Phase 4 renders it);
 * the load/unload hooks are inert until Phase 3 fires them.
 */
public interface MekBlockEntityExt {

    default void invalidateCapabilities() {
    }

    default ModelData getModelData() {
        return ModelData.EMPTY;
    }

    default void requestModelDataUpdate() {
    }

    default void onLoad() {
    }

    default void onChunkUnloaded() {
    }
}
