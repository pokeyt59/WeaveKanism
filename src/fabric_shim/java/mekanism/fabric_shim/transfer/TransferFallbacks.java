package mekanism.fabric_shim.transfer;

import mekanism.fabric_shim.capabilities.Capabilities;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import team.reborn.energy.api.EnergyStorage;

/**
 * Cross-ecosystem consume bridging: fallback providers on the shim {@code neoforge:*} block
 * capability lookups that resolve against the Fabric-standard lookups and wrap the result in the
 * transfer-bridge adapters. This is how Mekanism machines/transmitters see OTHER Fabric mods'
 * tanks, inventories, and batteries — Mekanism's own blocks register real providers on the shim
 * lookups, which Fabric consults before any fallback.
 *
 * <p>Energy deliberately hands out the raw FE-shaped {@link mekanism.fabric_shim.energy.IEnergyStorage}
 * (team-reborn units are FE-equivalent 1:1): Mekanism's own ForgeEnergyCompat wraps it in
 * ForgeStrictEnergyHandler, so the configured J↔FE rate and its clamping are Mekanism's own code.
 *
 * <p>When the expose direction lands (Mekanism containers registered on the Fabric-standard
 * lookups), these fallbacks must skip Mekanism's own exposed storages to preserve NeoForge's
 * null-semantics for sides Mekanism declines — guard to be added with that work.
 */
public final class TransferFallbacks {

    private TransferFallbacks() {
    }

    public static void init() {
        Capabilities.FluidHandler.BLOCK.lookup().registerFallback((level, pos, state, blockEntity, direction) -> {
            Storage<FluidVariant> storage = FluidStorage.SIDED.find(level, pos, state, blockEntity, direction);
            return storage == null ? null : new StorageFluidHandler(storage);
        });
        Capabilities.ItemHandler.BLOCK.lookup().registerFallback((level, pos, state, blockEntity, direction) -> {
            Storage<ItemVariant> storage = ItemStorage.SIDED.find(level, pos, state, blockEntity, direction);
            return storage == null ? null : new StorageItemHandler(storage);
        });
        Capabilities.EnergyStorage.BLOCK.lookup().registerFallback((level, pos, state, blockEntity, direction) -> {
            EnergyStorage storage = EnergyStorage.SIDED.find(level, pos, state, blockEntity, direction);
            return storage == null ? null : new StorageEnergyHandler(storage);
        });
    }
}
