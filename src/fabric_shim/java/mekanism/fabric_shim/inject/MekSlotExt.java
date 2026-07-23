package mekanism.fabric_shim.inject;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.Slot;

/**
 * NeoForge patches vanilla {@code Slot} with {@code setBackground(atlas, sprite)} (the empty-slot
 * icon). Injected onto Slot + SlotMixin; a no-op for 1f (slot background rendering is Phase 4).
 */
public interface MekSlotExt {

    default void setBackground(ResourceLocation atlas, ResourceLocation sprite) {
    }

    /** NeoForge's accessor for the index within the backing container — vanilla's getContainerSlot. */
    default int getSlotIndex() {
        return ((Slot) this).getContainerSlot();
    }
}
