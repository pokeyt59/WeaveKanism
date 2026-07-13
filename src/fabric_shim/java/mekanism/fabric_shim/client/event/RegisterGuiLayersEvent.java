package mekanism.fabric_shim.client.event;

import mekanism.fabric_shim.client.gui.GuiLayerHooks;
import mekanism.fabric_shim.fml.event.IModBusEvent;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.Event;

/**
 * Stand-in for NeoForge's {@code RegisterGuiLayersEvent} (the slice Mekanism uses): overlays land in
 * the {@link GuiLayerHooks} ordered store, dispatched from a HudRenderCallback at client-bootstrap
 * wiring time. NeoForge's above/below-a-named-vanilla-layer positioning has no Fabric equivalent, so
 * it degrades to registration order (see GuiLayerHooks / PORTING.md deviation) — the {@code relativeTo}
 * id is accepted and ignored.
 */
public class RegisterGuiLayersEvent extends Event implements IModBusEvent {

    public void registerBelowAll(ResourceLocation id, LayeredDraw.Layer layer) {
        GuiLayerHooks.register(id, layer);
    }

    public void registerAbove(ResourceLocation relativeTo, ResourceLocation id, LayeredDraw.Layer layer) {
        GuiLayerHooks.register(id, layer);
    }
}
