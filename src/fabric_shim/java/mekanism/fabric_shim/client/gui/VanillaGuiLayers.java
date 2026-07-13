package mekanism.fabric_shim.client.gui;

import net.minecraft.resources.ResourceLocation;

/**
 * Constants shim for NeoForge's {@code VanillaGuiLayers} — the vanilla-relative layer ids Mekanism
 * positions its overlays against. On Fabric these are opaque ordering keys only ({@link GuiLayerHooks}
 * degrades above/below to registration order); the id strings mirror NeoForge's for fidelity. Only the
 * slice Mekanism references.
 */
public final class VanillaGuiLayers {

    public static final ResourceLocation CROSSHAIR = ResourceLocation.withDefaultNamespace("crosshair");
    public static final ResourceLocation ARMOR_LEVEL = ResourceLocation.withDefaultNamespace("armor_level");
    public static final ResourceLocation SELECTED_ITEM_NAME = ResourceLocation.withDefaultNamespace("selected_item_name");
    public static final ResourceLocation SUBTITLE_OVERLAY = ResourceLocation.withDefaultNamespace("subtitle_overlay");

    private VanillaGuiLayers() {
    }
}
