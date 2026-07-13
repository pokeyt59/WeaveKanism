package mekanism.fabric_shim.client.gui;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.resources.ResourceLocation;

/**
 * Ordered store behind the {@code RegisterGuiLayersEvent} shim: Mekanism's HUD overlays, captured
 * during client init and dispatched from a HudRenderCallback at client-bootstrap wiring time
 * (client-compile-plan.md step 7). NeoForge positions each layer above/below a named vanilla layer;
 * Fabric's HudRenderCallback has no vanilla-relative slot, so ordering degrades to registration
 * order (PORTING.md deviation).
 */
public final class GuiLayerHooks {

    public record NamedLayer(ResourceLocation id, LayeredDraw.Layer layer) {}

    private static final List<NamedLayer> LAYERS = new ArrayList<>();

    private GuiLayerHooks() {
    }

    public static void register(ResourceLocation id, LayeredDraw.Layer layer) {
        LAYERS.add(new NamedLayer(id, layer));
    }

    public static List<NamedLayer> layers() {
        return LAYERS;
    }
}
