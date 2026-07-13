package mekanism.fabric_shim.client.event;

import net.neoforged.bus.api.Event;

/**
 * Stand-in for NeoForge's {@code RecipesUpdatedEvent} (game bus): fired after the client receives an
 * updated recipe set, so Mekanism can clear its recipe caches. Marker only (Mekanism reads nothing
 * off it). Posted from a ClientPacketListener#handleUpdateRecipes tail mixin (step 3b). Fresh
 * implementation.
 */
public class RecipesUpdatedEvent extends Event {
}
