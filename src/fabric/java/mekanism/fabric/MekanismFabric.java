package mekanism.fabric;

import mekanism.fabric_shim.common.NeoForgeMod;
import mekanism.fabric_shim.common.crafting.CustomIngredients;
import mekanism.fabric_shim.common.world.chunk.ShimChunkManager;
import mekanism.fabric_shim.fml.event.lifecycle.FMLCommonSetupEvent;
import mekanism.fabric_shim.fml.event.lifecycle.InterModEnqueueEvent;
import mekanism.fabric_shim.fml.event.lifecycle.InterModProcessEvent;
import mekanism.fabric_shim.internal.ShimBuses;
import mekanism.fabric_shim.internal.ShimGameEvents;
import mekanism.fabric_shim.registries.NeoForgeRegistries;
import mekanism.fabric_shim.registries.ShimRegistryEvents;
import mekanism.fabric_shim.server.ServerLifecycleHooks;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Fabric bootstrap entry point. Mirrors what NeoForge's FML does around the {@code @Mod}
 * constructor: construct the mod (which subscribes its DeferredRegisters and lifecycle listeners to
 * the mod bus), then drive the lifecycle events in FML's order — registry events, ticket
 * controllers, common setup, IMC enqueue, IMC process.
 */
public class MekanismFabric implements ModInitializer {

    public static final String MODID = "mekanism";
    public static final Logger LOGGER = LoggerFactory.getLogger("Mekanism");

    @Override
    public void onInitialize() {
        LOGGER.info("Mekanism Fabric bootstrap starting (Phase 1)");
        //Harmless if already started; buses only begin shut down when built with startShutdown()
        ShimBuses.MOD_BUS.start();
        ServerLifecycleHooks.init();
        ShimGameEvents.init();
        //NeoForge's built-in custom ingredient types (compound/difference/components), bridged into
        // Fabric's custom-ingredient system under NeoForge's ids
        CustomIngredients.registerBuiltins();
        //NeoForge-owned registries (attachment types, condition codecs, entity data serializers)
        // recreated under their neoforge: ids, before any RegisterEvent fires for them
        NeoForgeRegistries.init();
        //neoforge:swim_speed / neoforge:creative_flight attributes (see NeoForgeMod shim notes)
        NeoForgeMod.init(ShimBuses.MOD_BUS);

        //Mod construction, mirroring FML's @Mod constructor call (subscribes Mekanism's
        //DeferredRegisters and lifecycle listeners to the mod bus before registration fires)
        mekanism.fabric_shim.fml.ModContainer container = new mekanism.fabric_shim.fml.ModContainer(MODID);
        container.bridgeConfigEvents();
        new mekanism.common.Mekanism(container, ShimBuses.MOD_BUS);
        //@EventBusSubscriber classes (FML's annotation scan on NeoForge; an explicit list here)
        MekanismEventSubscribers.registerCommon();
        //Gameplay event glue: tick/entity/living/block/chunk families over Fabric API callbacks
        mekanism.fabric_shim.internal.ShimGameplayEvents.init();
        //Configuration-phase glue: RegisterConfigurationTasksEvent per connecting client
        mekanism.fabric_shim.internal.ShimConfigurationTasks.init();

        //Registration lifecycle: NewRegistryEvent, then RegisterEvent per registry in NeoForge's order.
        //Must happen inside onInitialize while Fabric still permits Registry.register.
        ShimRegistryEvents.fire(ShimBuses.MOD_BUS);
        ShimChunkManager.fireRegistration(ShimBuses.MOD_BUS);
        //Capability provider registration (on Fabric this registers straight into the API lookups)
        ShimBuses.MOD_BUS.post(new mekanism.fabric_shim.capabilities.RegisterCapabilitiesEvent());
        //Cross-ecosystem consume bridging: shim lookups fall back to Fabric-standard storages
        mekanism.fabric_shim.transfer.TransferFallbacks.init();
        //Payload (packet) channel + codec registration; serverbound receivers go live here, clientbound
        //receivers are parked for the Phase 4 client entry point (see PendingClientReceivers)
        ShimBuses.MOD_BUS.post(new mekanism.fabric_shim.network.event.RegisterPayloadHandlersEvent());
        //Entity attributes + spawn placements, in NeoForge's post-registration order; collected
        //attributes apply through Fabric's default-attribute registry
        mekanism.fabric_shim.event.entity.EntityAttributeCreationEvent attributeEvent = new mekanism.fabric_shim.event.entity.EntityAttributeCreationEvent();
        ShimBuses.MOD_BUS.post(attributeEvent);
        attributeEvent.getAttributes().forEach(net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry::register);
        ShimBuses.MOD_BUS.post(new mekanism.fabric_shim.event.entity.RegisterSpawnPlacementsEvent());

        //FML lifecycle order after registration; single-threaded, so enqueueWork runs inline
        ShimBuses.MOD_BUS.post(new FMLCommonSetupEvent());
        ShimBuses.MOD_BUS.post(new InterModEnqueueEvent());
        ShimBuses.MOD_BUS.post(new InterModProcessEvent(MODID));
        LOGGER.info("Mekanism Fabric bootstrap complete (registry + lifecycle events fired)");
    }
}
