package mekanism.fabric_shim.items;

import net.minecraft.world.item.ItemStack;

/**
 * An {@link IItemHandler} whose slots can be set directly (stand-in for NeoForge's
 * IItemHandlerModifiable; same surface).
 */
public interface IItemHandlerModifiable extends IItemHandler {

    void setStackInSlot(int slot, ItemStack stack);
}
