package mekanism.fabric_shim.registries;

import com.mojang.serialization.MapCodec;
import mekanism.fabric_shim.attachment.AttachmentType;
import mekanism.fabric_shim.common.conditions.ICondition;
import mekanism.fabric_shim.fluids.FluidType;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.minecraft.core.Registry;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

/**
 * Stand-in for net.neoforged.neoforge.registries.NeoForgeRegistries: the NeoForge-owned registries
 * Mekanism registers into, recreated as Fabric custom registries under the same {@code neoforge:}
 * ids so registry names (and thus serialized data) match upstream. {@link #init()} is called once
 * by the Fabric bootstrap before the registration lifecycle fires.
 *
 * <p>Deliberately absent for now: the biome/structure modifier keys (worldgen goes through Fabric's
 * BiomeModifications in Phase 3 — DatapackDeferredRegister is a hand-edit site).
 */
public final class NeoForgeRegistries {

    private static boolean initialized;

    private NeoForgeRegistries() {
    }

    public static final class Keys {

        private Keys() {
        }

        public static final ResourceKey<Registry<EntityDataSerializer<?>>> ENTITY_DATA_SERIALIZERS = key("entity_data_serializers");
        public static final ResourceKey<Registry<MapCodec<? extends ICondition>>> CONDITION_CODECS = key("condition_codecs");
        public static final ResourceKey<Registry<AttachmentType<?>>> ATTACHMENT_TYPES = key("attachment_types");
        public static final ResourceKey<Registry<FluidType>> FLUID_TYPES = key("fluid_type");

        private static <T> ResourceKey<Registry<T>> key(String name) {
            return ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath("neoforge", name));
        }
    }

    public static synchronized void init() {
        if (initialized) {
            return;
        }
        initialized = true;
        FabricRegistryBuilder.createSimple(Keys.ENTITY_DATA_SERIALIZERS).buildAndRegister();
        FabricRegistryBuilder.createSimple(Keys.CONDITION_CODECS).buildAndRegister();
        FabricRegistryBuilder.createSimple(Keys.ATTACHMENT_TYPES).buildAndRegister();
        FabricRegistryBuilder.createSimple(Keys.FLUID_TYPES).buildAndRegister();
    }
}
