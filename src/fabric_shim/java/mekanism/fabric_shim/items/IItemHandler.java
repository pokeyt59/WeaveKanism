package mekanism.fabric_shim.items;

import net.minecraft.world.item.ItemStack;

/**
 * Simulate-based slotted item handler contract (stand-in for NeoForge's IItemHandler; same
 * surface). Mekanism's internals keep these semantics; adapters bridge to Fabric's
 * transaction-based Storage&lt;ItemVariant&gt; at the mod boundary (Phase 2).
 */
public interface IItemHandler {

    int getSlots();

    ItemStack getStackInSlot(int slot);

    /**
     * @return the remainder that was not inserted (or would not be, if simulated)
     */
    ItemStack insertItem(int slot, ItemStack stack, boolean simulate);

    /**
     * @return the stack that was extracted (or would be, if simulated)
     */
    ItemStack extractItem(int slot, int amount, boolean simulate);

    int getSlotLimit(int slot);

    boolean isItemValid(int slot, ItemStack stack);
}
