package mekanism.fabric.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import java.util.Map;
import java.util.Optional;
import mekanism.fabric_shim.inject.MekAliasedRegistry;
import mekanism.fabric_shim.registries.RegistryAliasResolver;
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * The alias half of NeoForge's MappedRegistry patch: name lookups that miss retry once with the
 * alias-resolved id (registered entries always shadow aliases — see {@link RegistryAliasResolver}).
 * Covers the six name-addressed lookups; everything else vanilla builds on top of them
 * ({@code Registry#getOptional} defaults to {@code get}, {@code DefaultedMappedRegistry}
 * substitutes its default only after these bodies return, {@code byNameCodec}/
 * {@code holderByNameCodec} decode through {@code getHolder}) inherits resolution for free.
 * Registries with no aliases (all but items and data components) pay one null check on the miss
 * path only.
 */
@Mixin(MappedRegistry.class)
public abstract class MappedRegistryMixin<T> implements MekAliasedRegistry {

    @Shadow
    @Final
    ResourceKey<? extends Registry<T>> key;
    @Shadow
    @Final
    private Map<ResourceLocation, Holder.Reference<T>> byLocation;

    @Unique
    @Nullable
    private RegistryAliasResolver mekanism$aliases;

    @Override
    public void mekanism$addAlias(ResourceLocation from, ResourceLocation to) {
        if (this.mekanism$aliases == null) {
            //Raw map containment, deliberately not the (wrapped) containsKey — resolution must not recurse
            this.mekanism$aliases = new RegistryAliasResolver(this.byLocation::containsKey);
        }
        this.mekanism$aliases.addAlias(from, to);
    }

    @Unique
    private ResourceLocation mekanism$resolve(ResourceLocation name) {
        RegistryAliasResolver aliases = this.mekanism$aliases;
        return aliases == null || aliases.isEmpty() ? name : aliases.resolve(name);
    }

    /**
     * Identity comparison is intentional: {@code resolve} returns the input instance untouched
     * whenever no alias applied, so {@code !=} means "an alias actually rewrote the name".
     */
    @Unique
    @Nullable
    private ResourceLocation mekanism$retryName(ResourceLocation name) {
        ResourceLocation resolved = mekanism$resolve(name);
        return resolved == name ? null : resolved;
    }

    @Unique
    @Nullable
    private ResourceKey<T> mekanism$retryKey(ResourceKey<T> original) {
        ResourceLocation resolved = mekanism$retryName(original.location());
        return resolved == null ? null : ResourceKey.create(this.key, resolved);
    }

    @WrapMethod(method = "get(Lnet/minecraft/resources/ResourceLocation;)Ljava/lang/Object;")
    private T mekanism$aliasedGetByName(ResourceLocation name, Operation<T> original) {
        T result = original.call(name);
        if (result == null) {
            ResourceLocation retry = mekanism$retryName(name);
            if (retry != null) {
                return original.call(retry);
            }
        }
        return result;
    }

    @WrapMethod(method = "get(Lnet/minecraft/resources/ResourceKey;)Ljava/lang/Object;")
    private T mekanism$aliasedGetByKey(ResourceKey<T> key, Operation<T> original) {
        T result = original.call(key);
        if (result == null) {
            ResourceKey<T> retry = mekanism$retryKey(key);
            if (retry != null) {
                return original.call(retry);
            }
        }
        return result;
    }

    @WrapMethod(method = "getHolder(Lnet/minecraft/resources/ResourceLocation;)Ljava/util/Optional;")
    private Optional<Holder.Reference<T>> mekanism$aliasedHolderByName(ResourceLocation name, Operation<Optional<Holder.Reference<T>>> original) {
        Optional<Holder.Reference<T>> result = original.call(name);
        if (result.isEmpty()) {
            ResourceLocation retry = mekanism$retryName(name);
            if (retry != null) {
                return original.call(retry);
            }
        }
        return result;
    }

    @WrapMethod(method = "getHolder(Lnet/minecraft/resources/ResourceKey;)Ljava/util/Optional;")
    private Optional<Holder.Reference<T>> mekanism$aliasedHolderByKey(ResourceKey<T> key, Operation<Optional<Holder.Reference<T>>> original) {
        Optional<Holder.Reference<T>> result = original.call(key);
        if (result.isEmpty()) {
            ResourceKey<T> retry = mekanism$retryKey(key);
            if (retry != null) {
                return original.call(retry);
            }
        }
        return result;
    }

    @WrapMethod(method = "containsKey(Lnet/minecraft/resources/ResourceLocation;)Z")
    private boolean mekanism$aliasedContainsName(ResourceLocation name, Operation<Boolean> original) {
        if (original.call(name)) {
            return true;
        }
        ResourceLocation retry = mekanism$retryName(name);
        return retry != null && original.call(retry);
    }

    @WrapMethod(method = "containsKey(Lnet/minecraft/resources/ResourceKey;)Z")
    private boolean mekanism$aliasedContainsKey(ResourceKey<T> key, Operation<Boolean> original) {
        if (original.call(key)) {
            return true;
        }
        ResourceKey<T> retry = mekanism$retryKey(key);
        return retry != null && original.call(retry);
    }
}
