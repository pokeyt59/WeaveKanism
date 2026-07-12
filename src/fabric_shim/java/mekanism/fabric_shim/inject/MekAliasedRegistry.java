package mekanism.fabric_shim.inject;

import net.minecraft.resources.ResourceLocation;

/**
 * Duck for the alias half of NeoForge's registry patch ({@code IRegistryExtension#addAlias}),
 * implemented by {@code MappedRegistryMixin}. Only {@code DeferredRegister} calls this (at
 * RegisterEvent time, before its entries fill — NeoForge's order); Mekanism itself never touches
 * the registry-level API, so this is not compile-time interface-injected onto Registry.
 */
public interface MekAliasedRegistry {

    void mekanism$addAlias(ResourceLocation from, ResourceLocation to);
}
