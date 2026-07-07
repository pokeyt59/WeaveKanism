package mekanism.fabric_shim.capabilities;

import mekanism.fabric_shim.energy.IEnergyStorage;
import mekanism.fabric_shim.fluids.capability.IFluidHandler;
import mekanism.fabric_shim.fluids.capability.IFluidHandlerItem;
import mekanism.fabric_shim.items.IItemHandler;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

/**
 * Same tokens as net.neoforged.neoforge.capabilities.Capabilities, under the same {@code
 * neoforge:} ids. Fabric mods exposing/consuming these standard capabilities interoperate at the
 * transfer-bridge level (Phase 2), not through these lookups.
 */
public final class Capabilities {

    private Capabilities() {
    }

    private static ResourceLocation create(String path) {
        return ResourceLocation.fromNamespaceAndPath("neoforge", path);
    }

    public static final class EnergyStorage {

        private EnergyStorage() {
        }

        public static final BlockCapability<IEnergyStorage, @Nullable Direction> BLOCK = BlockCapability.createSided(create("energy"), IEnergyStorage.class);
        public static final EntityCapability<IEnergyStorage, @Nullable Direction> ENTITY = EntityCapability.createSided(create("energy"), IEnergyStorage.class);
        public static final ItemCapability<IEnergyStorage, @Nullable Void> ITEM = ItemCapability.createVoid(create("energy"), IEnergyStorage.class);
    }

    public static final class FluidHandler {

        private FluidHandler() {
        }

        public static final BlockCapability<IFluidHandler, @Nullable Direction> BLOCK = BlockCapability.createSided(create("fluid_handler"), IFluidHandler.class);
        public static final EntityCapability<IFluidHandler, @Nullable Direction> ENTITY = EntityCapability.createSided(create("fluid_handler"), IFluidHandler.class);
        public static final ItemCapability<IFluidHandlerItem, @Nullable Void> ITEM = ItemCapability.createVoid(create("fluid_handler"), IFluidHandlerItem.class);
    }

    public static final class ItemHandler {

        private ItemHandler() {
        }

        public static final BlockCapability<IItemHandler, @Nullable Direction> BLOCK = BlockCapability.createSided(create("item_handler"), IItemHandler.class);
        public static final EntityCapability<IItemHandler, @Nullable Void> ENTITY = EntityCapability.createVoid(create("item_handler"), IItemHandler.class);
        public static final EntityCapability<IItemHandler, @Nullable Direction> ENTITY_AUTOMATION = EntityCapability.createSided(create("item_handler_automation"), IItemHandler.class);
        public static final ItemCapability<IItemHandler, @Nullable Void> ITEM = ItemCapability.createVoid(create("item_handler"), IItemHandler.class);
    }
}
