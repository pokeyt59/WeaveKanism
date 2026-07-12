package mekanism.fabric_shim.transfer;

import java.util.List;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.energy.IMekanismStrictEnergyHandler;
import mekanism.api.fluid.IExtendedFluidHandler;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.inventory.IMekanismInventory;
import mekanism.fabric_shim.capabilities.Capabilities;
import mekanism.fabric_shim.energy.IEnergyStorage;
import mekanism.fabric_shim.fluids.capability.IFluidHandler;
import mekanism.fabric_shim.items.IItemHandler;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import team.reborn.energy.api.EnergyStorage;

/**
 * Cross-ecosystem bridging between the shim {@code neoforge:*} block capability lookups and the
 * Fabric-standard lookups, both directions, registered as fallbacks (Fabric consults real
 * providers first).
 *
 * <p><b>Consume</b> (Mekanism sees other mods): the shim lookups fall back to
 * {@code FluidStorage.SIDED} / {@code ItemStorage.SIDED} / team-reborn {@code EnergyStorage.SIDED}
 * wrapped in the Storage*Handler adapters. Guard: Mekanism's own block entities are skipped —
 * when a Mekanism tile declines a side, NeoForge semantics say {@code null}, and skipping also
 * breaks the fallback↔fallback query cycle with the expose direction.
 *
 * <p><b>Expose</b> (other mods see Mekanism): the Fabric-standard lookups fall back to the
 * Proxied*Storage adapters — operations through the tile's own side-aware capability proxy
 * (side-config permissions and the configured J↔FE conversion are Mekanism's own code), rollback
 * through the side's containers. Guard: only engages for Mekanism block entities (the api
 * handler interfaces), and only when the tile actually offers the capability on that side.
 */
public final class TransferFallbacks {

    private TransferFallbacks() {
    }

    public static void init() {
        //--- Consume: Mekanism-shaped queries resolving against Fabric-native neighbors ---
        Capabilities.FluidHandler.BLOCK.lookup().registerFallback((level, pos, state, blockEntity, direction) -> {
            if (blockEntity instanceof mekanism.api.fluid.IMekanismFluidHandler) {
                return null;
            }
            Storage<FluidVariant> storage = FluidStorage.SIDED.find(level, pos, state, blockEntity, direction);
            return storage == null ? null : new StorageFluidHandler(storage);
        });
        Capabilities.ItemHandler.BLOCK.lookup().registerFallback((level, pos, state, blockEntity, direction) -> {
            if (blockEntity instanceof IMekanismInventory) {
                return null;
            }
            Storage<ItemVariant> storage = ItemStorage.SIDED.find(level, pos, state, blockEntity, direction);
            return storage == null ? null : new StorageItemHandler(storage);
        });
        Capabilities.EnergyStorage.BLOCK.lookup().registerFallback((level, pos, state, blockEntity, direction) -> {
            if (blockEntity instanceof IMekanismStrictEnergyHandler) {
                return null;
            }
            EnergyStorage storage = EnergyStorage.SIDED.find(level, pos, state, blockEntity, direction);
            return storage == null ? null : new StorageEnergyHandler(storage);
        });

        //--- Expose: Fabric-native queries resolving against Mekanism tiles ---
        FluidStorage.SIDED.registerFallback((level, pos, state, blockEntity, direction) -> {
            if (!(blockEntity instanceof mekanism.api.fluid.IMekanismFluidHandler sided)) {
                return null;
            }
            IFluidHandler found = Capabilities.FluidHandler.BLOCK.lookup().find(level, pos, state, blockEntity, direction);
            if (!(found instanceof IExtendedFluidHandler proxy)) {
                return null;
            }
            List<IExtendedFluidTank> tanks = sided.getFluidTanks(direction);
            return tanks.isEmpty() ? null : new ProxiedFluidStorage(proxy, tanks);
        });
        ItemStorage.SIDED.registerFallback((level, pos, state, blockEntity, direction) -> {
            if (!(blockEntity instanceof IMekanismInventory sided)) {
                return null;
            }
            IItemHandler proxy = Capabilities.ItemHandler.BLOCK.lookup().find(level, pos, state, blockEntity, direction);
            if (proxy == null) {
                return null;
            }
            List<IInventorySlot> slots = sided.getInventorySlots(direction);
            return slots.isEmpty() ? null : new ProxiedItemStorage(proxy, slots, level.registryAccess());
        });
        EnergyStorage.SIDED.registerFallback((level, pos, state, blockEntity, direction) -> {
            if (!(blockEntity instanceof IMekanismStrictEnergyHandler sided)) {
                return null;
            }
            IEnergyStorage proxy = Capabilities.EnergyStorage.BLOCK.lookup().find(level, pos, state, blockEntity, direction);
            if (proxy == null) {
                return null;
            }
            List<IEnergyContainer> containers = sided.getEnergyContainers(direction);
            return containers.isEmpty() ? null : new ProxiedEnergyStorage(proxy, containers);
        });
    }
}
