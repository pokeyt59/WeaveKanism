package mekanism.fabric_shim.integration.curios;

import net.minecraft.world.item.ItemStack;

/**
 * Stand-in for Curios' {@code top.theillusivec4.curios.api.SlotResult} record (same component
 * shape). See {@link SlotContext}.
 */
public record SlotResult(SlotContext slotContext, ItemStack stack) {
}
