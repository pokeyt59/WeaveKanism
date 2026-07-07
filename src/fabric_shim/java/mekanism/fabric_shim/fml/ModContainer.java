package mekanism.fabric_shim.fml;

import fuzs.forgeconfigapiport.fabric.api.neoforge.v4.NeoForgeConfigRegistry;
import fuzs.forgeconfigapiport.fabric.api.neoforge.v4.NeoForgeModConfigEvents;
import java.util.Objects;
import mekanism.fabric_shim.fml.event.config.ModConfigEvent;
import mekanism.fabric_shim.internal.ShimBuses;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.neoforged.fml.config.IConfigSpec;
import net.neoforged.fml.config.ModConfig;

/**
 * Stand-in for net.neoforged.fml.ModContainer, backed by Fabric Loader metadata and Forge Config
 * API Port. {@link #registerConfig} delegates to FCAP's NeoForgeConfigRegistry (ModConfig,
 * IConfigSpec and ModConfigSpec are FCAP's real classes under their original NeoForge package
 * names, so config classes need no remap). The Fabric bootstrap calls
 * {@link #bridgeConfigEvents()} on the mod's own container (and only that one) before the mod
 * registers configs, so FCAP's per-mod config callbacks reach the shim mod bus as
 * {@link ModConfigEvent}s — matching where NeoForge fires them. Containers obtained through
 * ModList for other mods must NOT be bridged (their events don't belong on Mekanism's mod bus).
 */
public final class ModContainer {

    private final String modId;
    private final IModInfo modInfo;

    public ModContainer(String modId) {
        this.modId = Objects.requireNonNull(modId);
        ModMetadata metadata = FabricLoader.getInstance().getModContainer(modId)
              .orElseThrow(() -> new IllegalArgumentException("Unknown mod id: " + modId))
              .getMetadata();
        ArtifactVersion version = new ArtifactVersion(metadata.getVersion().getFriendlyString());
        this.modInfo = new IModInfo() {
            @Override
            public String getModId() {
                return metadata.getId();
            }

            @Override
            public String getDisplayName() {
                return metadata.getName();
            }

            @Override
            public ArtifactVersion getVersion() {
                return version;
            }
        };
    }

    /**
     * Bridges FCAP's config load/reload/unload callbacks for this mod onto the shim mod bus. Call
     * once, before configs are registered (FCAP may fire the initial load during registration).
     */
    public void bridgeConfigEvents() {
        NeoForgeModConfigEvents.loading(modId).register(config -> ShimBuses.MOD_BUS.post(new ModConfigEvent.Loading(config)));
        NeoForgeModConfigEvents.reloading(modId).register(config -> ShimBuses.MOD_BUS.post(new ModConfigEvent.Reloading(config)));
        NeoForgeModConfigEvents.unloading(modId).register(config -> ShimBuses.MOD_BUS.post(new ModConfigEvent.Unloading(config)));
    }

    public String getModId() {
        return modId;
    }

    public IModInfo getModInfo() {
        return modInfo;
    }

    public ModConfig registerConfig(ModConfig.Type type, IConfigSpec spec) {
        return NeoForgeConfigRegistry.INSTANCE.register(modId, type, spec);
    }

    public ModConfig registerConfig(ModConfig.Type type, IConfigSpec spec, String fileName) {
        return NeoForgeConfigRegistry.INSTANCE.register(modId, type, spec, fileName);
    }
}
