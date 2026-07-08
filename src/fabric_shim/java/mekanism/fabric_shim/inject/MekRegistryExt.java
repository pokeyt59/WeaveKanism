package mekanism.fabric_shim.inject;

import java.util.Collections;
import java.util.Map;
import mekanism.fabric_shim.registries.datamaps.DataMapType;
import net.minecraft.resources.ResourceKey;

/**
 * NeoForge patches vanilla {@code Registry} with {@code getDataMap(DataMapType)} (all entries carrying
 * a data map). Injected onto Registry + RegistryMixin (interface target). Returns empty for 1f — the
 * data-map JSON loader runs in Phase 3 (see PORTING.md); the only current caller is a recipe-viewer
 * display helper (Phase 5).
 */
public interface MekRegistryExt {

    default <R, T> Map<ResourceKey<R>, T> getDataMap(DataMapType<R, T> type) {
        return Collections.emptyMap();
    }
}
