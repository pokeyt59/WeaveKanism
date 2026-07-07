package mekanism.fabric_shim.fml;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import net.fabricmc.loader.api.FabricLoader;

/**
 * Stand-in for net.neoforged.fml.ModList, backed by Fabric Loader.
 *
 * <p>Only the surface Mekanism uses at runtime is provided. FML's annotation scan data
 * (getAllScanData) has no Fabric equivalent and is handled separately where it is consumed
 * (see MekAnnotationScanner porting notes in PORTING.md).
 */
public final class ModList {

    private static final ModList INSTANCE = new ModList();

    private final Map<String, ModContainer> containers = new ConcurrentHashMap<>();

    private ModList() {
    }

    public static ModList get() {
        return INSTANCE;
    }

    public boolean isLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }

    /**
     * FML annotation scan data has no Fabric equivalent — always empty, which makes
     * MekAnnotationScanner-driven features inert (deferred with the computer integrations).
     */
    public java.util.List<mekanism.fabric_shim.spi.ModFileScanData> getAllScanData() {
        return java.util.List.of();
    }

    /**
     * Note: containers returned here are metadata views only — config events are deliberately not
     * bridged for them (see ModContainer#bridgeConfigEvents).
     */
    public Optional<? extends ModContainer> getModContainerById(String modId) {
        if (!isLoaded(modId)) {
            return Optional.empty();
        }
        return Optional.of(containers.computeIfAbsent(modId, ModContainer::new));
    }
}
