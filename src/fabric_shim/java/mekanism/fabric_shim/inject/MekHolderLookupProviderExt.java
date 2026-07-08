package mekanism.fabric_shim.inject;

import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

/**
 * NeoForge patches {@code HolderLookup.Provider} (and thus {@code RegistryAccess}) with
 * {@code holder}/{@code holderOrThrow} convenience lookups by entry key. Injected + HolderLookup-
 * ProviderMixin; implemented over vanilla {@link HolderLookup.Provider#lookup}.
 */
public interface MekHolderLookupProviderExt {

    default <T> Optional<Holder.Reference<T>> holder(ResourceKey<T> key) {
        ResourceKey<? extends Registry<? extends T>> registryKey = ResourceKey.createRegistryKey(key.registry());
        return ((HolderLookup.Provider) this).lookup(registryKey).flatMap(lookup -> lookup.get(key));
    }

    default <T> Holder.Reference<T> holderOrThrow(ResourceKey<T> key) {
        return this.<T>holder(key).orElseThrow(() -> new IllegalStateException("Missing entry: " + key));
    }
}
