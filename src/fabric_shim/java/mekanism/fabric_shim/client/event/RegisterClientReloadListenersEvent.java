package mekanism.fabric_shim.client.event;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import mekanism.fabric_shim.fml.event.IModBusEvent;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.PreparableReloadListener.PreparationBarrier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.bus.api.Event;

/**
 * Stand-in for NeoForge's {@code RegisterClientReloadListenersEvent}: each listener is wrapped in a
 * Fabric {@link IdentifiableResourceReloadListener} (generated {@code mekanism:client_reload_<n>} id)
 * and registered permanently against the client resource manager — the async sibling of
 * ShimGameEvents' server-side wrapper. Posted from the client bootstrap in NeoForge's client-init order.
 */
public class RegisterClientReloadListenersEvent extends Event implements IModBusEvent {

    private static final AtomicInteger COUNTER = new AtomicInteger();

    public void registerReloadListener(PreparableReloadListener listener) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath("mekanism", "client_reload_" + COUNTER.getAndIncrement());
        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new IdentifiableResourceReloadListener() {
            @Override
            public ResourceLocation getFabricId() {
                return id;
            }

            @Override
            public CompletableFuture<Void> reload(PreparationBarrier barrier, ResourceManager manager, ProfilerFiller prepareProfiler,
                  ProfilerFiller applyProfiler, Executor prepareExecutor, Executor applyExecutor) {
                return listener.reload(barrier, manager, prepareProfiler, applyProfiler, prepareExecutor, applyExecutor);
            }
        });
    }
}
