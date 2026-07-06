package mekanism.fabric_shim.fml.event.lifecycle;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import mekanism.fabric_shim.fml.event.IModBusEvent;
import net.neoforged.bus.api.Event;

/**
 * Base class for mod lifecycle events that accept deferred work (stand-in for FML's
 * ParallelDispatchEvent). On NeoForge these dispatch in parallel across mods and enqueued work runs
 * on the main thread afterwards; the Fabric bootstrap is single-threaded, so enqueued work simply
 * runs inline — the same ordering guarantees hold for a single mod.
 */
public class ParallelDispatchEvent extends Event implements IModBusEvent {

    public CompletableFuture<Void> enqueueWork(Runnable work) {
        work.run();
        return CompletableFuture.completedFuture(null);
    }

    public <T> CompletableFuture<T> enqueueWork(Supplier<T> work) {
        return CompletableFuture.completedFuture(work.get());
    }
}
