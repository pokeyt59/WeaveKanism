package mekanism.fabric_shim.client;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;

/**
 * Same surface as net.neoforged.neoforge.client.IItemDecorator (1.21.1 shape): called after
 * vanilla renders an item's slot decorations (count, durability bar). Return true if render state
 * was modified and must be reset for other decorators. Dispatch on Fabric happens from the
 * GuiGraphics decoration hook wired in the client mixin step (client-compile-plan.md).
 */
@FunctionalInterface
public interface IItemDecorator {

    boolean render(GuiGraphics guiGraphics, Font font, ItemStack stack, int xOffset, int yOffset);
}
