package mekanism.fabric_shim.entity;

import net.minecraft.network.RegistryFriendlyByteBuf;

/**
 * Stand-in for NeoForge's {@code IEntityWithComplexSpawn}: an entity that writes/reads extra data in
 * its add-entity packet. Compile surface only; the actual spawn-packet plumbing (via
 * {@code fabric-networking} entity spawn) is Phase 3.
 */
public interface IEntityWithComplexSpawn {

    void writeSpawnData(RegistryFriendlyByteBuf buffer);

    void readSpawnData(RegistryFriendlyByteBuf buffer);
}
