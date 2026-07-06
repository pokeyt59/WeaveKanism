package mekanism.fabric_shim.fml.event.lifecycle;

import org.jetbrains.annotations.ApiStatus;

/**
 * Common (both sides) setup lifecycle event (stand-in for FML's FMLCommonSetupEvent). Fired on the
 * mod bus by the Fabric bootstrap after registration completes.
 */
public class FMLCommonSetupEvent extends ParallelDispatchEvent {

    @ApiStatus.Internal
    public FMLCommonSetupEvent() {
    }
}
