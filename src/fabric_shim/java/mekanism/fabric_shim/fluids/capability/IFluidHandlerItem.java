package mekanism.fabric_shim.fluids.capability;

import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Same surface as net.neoforged.neoforge.fluids.capability.IFluidHandlerItem.
 */
public interface IFluidHandlerItem extends IFluidHandler {

    @NotNull
    ItemStack getContainer();
}
