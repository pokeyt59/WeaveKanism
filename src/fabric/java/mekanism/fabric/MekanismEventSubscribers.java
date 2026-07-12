package mekanism.fabric;

import mekanism.common.lib.multiblock.MultiblockManager;
import mekanism.common.lib.radiation.MeltdownLevelData;
import mekanism.common.lib.radiation.PlayerExposure;
import mekanism.common.lib.transmitter.TransmitterNetworkRegistry;
import mekanism.fabric_shim.common.NeoForge;

/**
 * Explicit registration of Mekanism's {@code @EventBusSubscriber} classes. On NeoForge, FML's
 * annotation scan auto-registers them; Fabric has no scan data, so this list is maintained by
 * hand — <b>new upstream {@code @EventBusSubscriber} classes must be added here</b> (upstream-merge
 * residual checklist item; client-dist subscribers join via the Phase 4 client entry point).
 */
public final class MekanismEventSubscribers {

    private MekanismEventSubscribers() {
    }

    public static void registerCommon() {
        NeoForge.EVENT_BUS.register(MultiblockManager.class);
        NeoForge.EVENT_BUS.register(MeltdownLevelData.class);
        NeoForge.EVENT_BUS.register(PlayerExposure.class);
        NeoForge.EVENT_BUS.register(TransmitterNetworkRegistry.class);
    }
}
