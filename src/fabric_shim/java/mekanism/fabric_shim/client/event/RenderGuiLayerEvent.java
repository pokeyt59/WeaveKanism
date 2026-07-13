package mekanism.fabric_shim.client.event;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

/**
 * Stand-in for NeoForge's {@code RenderGuiLayerEvent} (game bus): Pre lets Mekanism suppress a named
 * vanilla HUD layer (it hides the crosshair behind a radial menu). Posted per layer from a HUD-layer
 * render mixin keyed by the layer id (step 3b). Only the Pre slice Mekanism uses. Fresh implementation.
 */
public abstract class RenderGuiLayerEvent extends Event {

    private final ResourceLocation name;

    protected RenderGuiLayerEvent(ResourceLocation name) {
        this.name = name;
    }

    public ResourceLocation getName() {
        return name;
    }

    public static class Pre extends RenderGuiLayerEvent implements ICancellableEvent {

        public Pre(ResourceLocation name) {
            super(name);
        }
    }
}
