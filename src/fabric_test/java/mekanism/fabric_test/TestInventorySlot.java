package mekanism.fabric_test;

import mekanism.api.SerializationConstants;
import mekanism.api.SerializerHelper;
import mekanism.api.inventory.IInventorySlot;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Minimal in-memory IInventorySlot for bridge tests — the abstract subset only; insertItem /
 * extractItem come from the interface defaults, i.e. the exact logic Mekanism slots run in
 * production. Mirrors BasicInventorySlot's copy-on-set behavior and its unchecked setter.
 */
public class TestInventorySlot implements IInventorySlot {

    private final int limit;
    private ItemStack current = ItemStack.EMPTY;
    public int contentsChangedCount;

    public TestInventorySlot(int limit) {
        this.limit = limit;
    }

    @NotNull
    @Override
    public ItemStack getStack() {
        return current;
    }

    @Override
    public void setStack(ItemStack stack) {
        setStackUnchecked(stack);
    }

    public void setStackUnchecked(ItemStack stack) {
        current = stack.isEmpty() ? ItemStack.EMPTY : stack.copy();
        onContentsChanged();
    }

    @Override
    public int getLimit(ItemStack stack) {
        return stack.isEmpty() ? limit : Math.min(limit, stack.getMaxStackSize());
    }

    @Override
    public boolean isItemValid(ItemStack stack) {
        return true;
    }

    @Override
    public void onContentsChanged() {
        contentsChangedCount++;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
        //Mirrors BasicInventorySlot: restore through the unchecked setter, never re-validated
        setStackUnchecked(SerializerHelper.parseOversizedOptional(provider, nbt.getCompound(SerializationConstants.ITEM)));
    }
}
