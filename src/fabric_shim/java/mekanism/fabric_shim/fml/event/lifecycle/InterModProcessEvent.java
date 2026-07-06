package mekanism.fabric_shim.fml.event.lifecycle;

import java.util.function.Predicate;
import java.util.stream.Stream;
import mekanism.fabric_shim.fml.InterModComms;
import org.jetbrains.annotations.ApiStatus;

/**
 * Lifecycle event during which received inter-mod messages are processed (stand-in for FML's
 * InterModProcessEvent).
 */
public class InterModProcessEvent extends ParallelDispatchEvent {

    private final String modId;

    @ApiStatus.Internal
    public InterModProcessEvent(String modId) {
        this.modId = modId;
    }

    public Stream<InterModComms.IMCMessage> getIMCStream() {
        return InterModComms.getMessages(modId);
    }

    public Stream<InterModComms.IMCMessage> getIMCStream(Predicate<String> methodFilter) {
        return InterModComms.getMessages(modId, methodFilter);
    }
}
