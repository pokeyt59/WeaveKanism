package mekanism.fabric_shim.inject;

import net.minecraft.resources.ResourceLocation;

/**
 * NeoForge patches vanilla {@code Slot} with {@code setBackground(atlas, sprite)} (the empty-slot
 * icon). Injected onto Slot + SlotMixin; a no-op for 1f (slot background rendering is Phase 4).
 */
public interface MekSlotExt {

    default void setBackground(ResourceLocation atlas, ResourceLocation sprite) {
    }
}
