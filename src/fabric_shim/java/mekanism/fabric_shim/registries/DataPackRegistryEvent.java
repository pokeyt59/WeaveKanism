package mekanism.fabric_shim.registries;

import com.mojang.serialization.Codec;
import java.util.function.Consumer;
import mekanism.fabric_shim.fml.event.IModBusEvent;
import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.neoforged.bus.api.Event;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Mod-bus event for registering datapack (dynamic) registries, backed by Fabric's
 * DynamicRegistries (stand-in for NeoForge's DataPackRegistryEvent; same surface).
 */
public abstract class DataPackRegistryEvent extends Event implements IModBusEvent {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataPackRegistryEvent.class);

    @ApiStatus.Internal
    public DataPackRegistryEvent() {
    }

    public static final class NewRegistry extends DataPackRegistryEvent {

        @ApiStatus.Internal
        public NewRegistry() {
        }

        public <T> void dataPackRegistry(ResourceKey<Registry<T>> registryKey, Codec<T> codec) {
            this.dataPackRegistry(registryKey, codec, null);
        }

        public <T> void dataPackRegistry(ResourceKey<Registry<T>> registryKey, Codec<T> codec, @Nullable Codec<T> networkCodec) {
            if (networkCodec == null) {
                DynamicRegistries.register(registryKey, codec);
            } else {
                DynamicRegistries.registerSynced(registryKey, codec, networkCodec);
            }
        }

        public <T> void dataPackRegistry(ResourceKey<Registry<T>> registryKey, Codec<T> codec, @Nullable Codec<T> networkCodec,
              Consumer<RegistryBuilder<T>> consumer) {
            //Fabric's dynamic registries take no builder configuration; nothing Mekanism relies on is lost,
            // but surface the call in the log in case upstream starts using builder options here
            LOGGER.warn("Datapack registry {} registered with a RegistryBuilder configurator; ignored on the Fabric port", registryKey.location());
            this.dataPackRegistry(registryKey, codec, networkCodec);
        }
    }
}
