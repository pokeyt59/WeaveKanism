package mekanism.fabric_shim.fml.event.config;

import mekanism.fabric_shim.fml.event.IModBusEvent;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.config.ModConfig;

/**
 * Stand-in for net.neoforged.fml.event.config.ModConfigEvent. Forge Config API Port exposes config
 * load/reload/unload as per-mod Fabric callbacks instead of bus events; the ModContainer shim
 * bridges those callbacks onto the shim mod bus as instances of this hierarchy, so upstream
 * listeners (e.g. MekanismConfig::onConfigLoad) work unchanged.
 *
 * <p>Concrete like FML's (the neoforged bus rejects listeners typed to abstract event classes,
 * and Mekanism listens to this base type directly).
 */
public class ModConfigEvent extends Event implements IModBusEvent {

    private final ModConfig config;

    ModConfigEvent(ModConfig config) {
        this.config = config;
    }

    public ModConfig getConfig() {
        return config;
    }

    public static class Loading extends ModConfigEvent {

        public Loading(ModConfig config) {
            super(config);
        }
    }

    public static class Reloading extends ModConfigEvent {

        public Reloading(ModConfig config) {
            super(config);
        }
    }

    public static class Unloading extends ModConfigEvent {

        public Unloading(ModConfig config) {
            super(config);
        }
    }
}
