package mekanism.fabric_shim.client.event;

import mekanism.fabric_shim.fml.event.IModBusEvent;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.neoforged.bus.api.Event;

/**
 * Stand-in for NeoForge's {@code TextureAtlasStitchedEvent}: a data-carrying view over a freshly
 * stitched {@link TextureAtlas}, posted on the mod bus from an atlas-upload tail mixin at
 * client-bootstrap wiring time (client-compile-plan.md step 3b/7). MekanismRenderer reads its sprites
 * off the atlas here.
 */
public class TextureAtlasStitchedEvent extends Event implements IModBusEvent {

    private final TextureAtlas atlas;

    public TextureAtlasStitchedEvent(TextureAtlas atlas) {
        this.atlas = atlas;
    }

    public TextureAtlas getAtlas() {
        return atlas;
    }
}
