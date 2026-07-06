package mekanism.fabric_shim.fml.event.lifecycle;

import org.jetbrains.annotations.ApiStatus;

/**
 * Lifecycle event during which inter-mod messages are sent via InterModComms (stand-in for FML's
 * InterModEnqueueEvent).
 */
public class InterModEnqueueEvent extends ParallelDispatchEvent {

    @ApiStatus.Internal
    public InterModEnqueueEvent() {
    }
}
