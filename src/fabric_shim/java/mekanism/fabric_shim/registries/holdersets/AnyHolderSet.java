package mekanism.fabric_shim.registries.holdersets;

import com.mojang.datafixers.util.Either;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.tags.TagKey;

/**
 * Stand-in for NeoForge's {@code AnyHolderSet}: a {@link HolderSet} containing every element of a
 * registry (Mekanism uses it for the "works on any block" tool rule). Backed by the lookup's full
 * element list; the custom-holderset codec/network machinery NeoForge attaches is not needed here.
 */
public final class AnyHolderSet<T> extends HolderSet.ListBacked<T> {

    private final HolderLookup.RegistryLookup<T> registryLookup;
    private List<Holder<T>> contents;

    public AnyHolderSet(HolderLookup.RegistryLookup<T> registryLookup) {
        this.registryLookup = registryLookup;
    }

    @Override
    protected List<Holder<T>> contents() {
        if (this.contents == null) {
            this.contents = this.registryLookup.listElements().collect(Collectors.toList());
        }
        return this.contents;
    }

    @Override
    public boolean contains(Holder<T> holder) {
        return holder.canSerializeIn(this.registryLookup);
    }

    @Override
    public Either<TagKey<T>, List<Holder<T>>> unwrap() {
        return Either.right(contents());
    }

    @Override
    public Optional<TagKey<T>> unwrapKey() {
        return Optional.empty();
    }
}
