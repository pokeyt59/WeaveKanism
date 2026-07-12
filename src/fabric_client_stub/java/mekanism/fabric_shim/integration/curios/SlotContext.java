package mekanism.fabric_shim.integration.curios;

import net.minecraft.world.entity.LivingEntity;

/**
 * Stand-in for Curios' {@code top.theillusivec4.curios.api.SlotContext} record (same component
 * shape). Only ever observed through {@link CuriosIntegration#findFirstCurioAsResult}, which
 * returns empty on the port — Phase 5 maps this surface onto Trinkets/Accessories.
 */
public record SlotContext(String identifier, LivingEntity entity, int index, boolean cosmetic, boolean visible) {
}
