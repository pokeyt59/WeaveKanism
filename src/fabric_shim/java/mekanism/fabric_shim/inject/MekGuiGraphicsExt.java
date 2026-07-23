package mekanism.fabric_shim.inject;

import java.util.List;
import java.util.Optional;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;

/**
 * NeoForge's GuiGraphics patch surface (the float-position drawString slice Mekanism uses),
 * attached to {@link GuiGraphics} via Loom interface injection + GuiGraphicsMixin.
 * <p>
 * Semantics: identical to vanilla's int-position
 * {@code drawString(Font, FormattedCharSequence, int, int, int, boolean)} — a single
 * {@link Font#drawInBatch} at full brightness followed by {@code flushIfUnmanaged()} (AW'd
 * accessible) — but without truncating the caller's float coordinates. Mekanism's own
 * {@code GuiUtils#drawStringNoFlush} documents the same body minus the flush.
 */
public interface MekGuiGraphicsExt {

    private GuiGraphics self() {
        return (GuiGraphics) this;
    }

    default int drawString(Font font, FormattedCharSequence text, float x, float y, int color, boolean dropShadow) {
        GuiGraphics self = self();
        int width = font.drawInBatch(text, x, y, color, dropShadow, self.pose().last().pose(), self.bufferSource(),
              Font.DisplayMode.NORMAL, 0, LightTexture.FULL_BRIGHT);
        self.flushIfUnmanaged();
        return width;
    }

    /**
     * NeoForge's stack-aware tooltip overload; the stack parameter only feeds NeoForge's tooltip
     * events, which have no consumers in the port — vanilla's stack-less form renders identically.
     */
    default void renderTooltip(Font font, List<Component> tooltipLines, Optional<TooltipComponent> visualTooltipComponent, ItemStack stack, int mouseX, int mouseY) {
        self().renderTooltip(font, tooltipLines, visualTooltipComponent, mouseX, mouseY);
    }
}
