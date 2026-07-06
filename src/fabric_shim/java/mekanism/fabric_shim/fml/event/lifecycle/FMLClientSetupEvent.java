package mekanism.fabric_shim.fml.event.lifecycle;

import org.jetbrains.annotations.ApiStatus;

/**
 * Client-side setup lifecycle event (stand-in for FML's FMLClientSetupEvent). Fired on the mod bus
 * by the Fabric client bootstrap.
 */
public class FMLClientSetupEvent extends ParallelDispatchEvent {

    @ApiStatus.Internal
    public FMLClientSetupEvent() {
    }
}
