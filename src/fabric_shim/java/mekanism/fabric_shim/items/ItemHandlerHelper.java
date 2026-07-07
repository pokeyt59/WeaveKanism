package mekanism.fabric_shim.items;

import net.minecraft.world.item.ItemStack;

/**
 * Same surface (the slice Mekanism uses) as net.neoforged.neoforge.items.ItemHandlerHelper.
 */
public final class ItemHandlerHelper {

    private ItemHandlerHelper() {
    }

    /**
     * Inserts the stack into the handler, preferring slots that already contain the same item,
     * then empty slots. Matches NeoForge's contract: returns the remainder (EMPTY if fully
     * inserted); does not mutate the input stack.
     */
    public static ItemStack insertItemStacked(IItemHandler handler, ItemStack stack, boolean simulate) {
        if (handler == null || stack.isEmpty()) {
            return stack;
        }
        int slots = handler.getSlots();
        //First pass: top up existing stacks of the same item
        for (int slot = 0; slot < slots && !stack.isEmpty(); slot++) {
            if (ItemStack.isSameItemSameComponents(handler.getStackInSlot(slot), stack)) {
                stack = handler.insertItem(slot, stack, simulate);
            }
        }
        //Second pass: fill empty slots
        for (int slot = 0; slot < slots && !stack.isEmpty(); slot++) {
            if (handler.getStackInSlot(slot).isEmpty()) {
                stack = handler.insertItem(slot, stack, simulate);
            }
        }
        return stack;
    }
}
