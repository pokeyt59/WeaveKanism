package mekanism.fabric_shim.internal;

import mekanism.fabric_shim.common.NeoForge;
import mekanism.fabric_shim.event.AddReloadListenerEvent;
import mekanism.fabric_shim.event.RegisterCommandsEvent;
import mekanism.fabric_shim.event.TagsUpdatedEvent;
import mekanism.fabric_shim.event.level.LevelEvent;
import mekanism.fabric_shim.event.server.ServerAboutToStartEvent;
import mekanism.fabric_shim.event.server.ServerStartedEvent;
import mekanism.fabric_shim.event.server.ServerStartingEvent;
import mekanism.fabric_shim.event.server.ServerStoppedEvent;
import mekanism.fabric_shim.event.server.ServerStoppingEvent;
import mekanism.fabric_shim.server.ServerLifecycleHooks;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.CommonLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Bridges Fabric lifecycle callbacks onto the shim game bus (NeoForge.EVENT_BUS) as their NeoForge
 * event equivalents. Registered once from the Fabric bootstrap, before any mod code subscribes.
 *
 * <p>Ordering note: NeoForge fires ServerStartingEvent after levels load; Fabric has no hook there,
 * so it is posted immediately before ServerStartedEvent (from SERVER_STARTED) — the "levels are
 * loaded" invariant holds for listeners of both.
 */
public final class ShimGameEvents {

    private static final Logger LOGGER = LoggerFactory.getLogger("MekanismShim");
    private static boolean initialized;

    private ShimGameEvents() {
    }

    public static void init() {
        if (initialized) {
            return;
        }
        initialized = true;

        ServerLifecycleEvents.SERVER_STARTING.register(server -> NeoForge.EVENT_BUS.post(new ServerAboutToStartEvent(server)));
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            NeoForge.EVENT_BUS.post(new ServerStartingEvent(server));
            NeoForge.EVENT_BUS.post(new ServerStartedEvent(server));
        });
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> NeoForge.EVENT_BUS.post(new ServerStoppingEvent(server)));
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> NeoForge.EVENT_BUS.post(new ServerStoppedEvent(server)));

        ServerWorldEvents.LOAD.register((server, level) -> NeoForge.EVENT_BUS.post(new LevelEvent.Load(level)));
        ServerWorldEvents.UNLOAD.register((server, level) -> NeoForge.EVENT_BUS.post(new LevelEvent.Unload(level)));

        CommonLifecycleEvents.TAGS_LOADED.register((registries, client) ->
              NeoForge.EVENT_BUS.post(new TagsUpdatedEvent(registries, client, client && ServerLifecycleHooks.getCurrentServer() != null)));

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
              NeoForge.EVENT_BUS.post(new RegisterCommandsEvent(dispatcher, environment, registryAccess)));

        //One permanent Fabric reload listener re-fires AddReloadListenerEvent per datapack (re)load,
        // matching NeoForge's collect-on-each-reload semantics. Fabric appends mod listeners after
        // vanilla ones (recipes/tags), which is what Mekanism's LOWEST-priority listener wants.
        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
            @Override
            public ResourceLocation getFabricId() {
                return ResourceLocation.fromNamespaceAndPath("mekanism", "neoforge_reload_listeners");
            }

            @Override
            public void onResourceManagerReload(ResourceManager resourceManager) {
                MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
                AddReloadListenerEvent event = new AddReloadListenerEvent(server == null ? null : server.registryAccess());
                NeoForge.EVENT_BUS.post(event);
                for (PreparableReloadListener listener : event.getListeners()) {
                    if (listener instanceof ResourceManagerReloadListener sync) {
                        sync.onResourceManagerReload(resourceManager);
                    } else {
                        LOGGER.warn("Skipping reload listener {} — the Phase 1 shim only supports synchronous listeners", listener);
                    }
                }
            }
        });
    }
}
