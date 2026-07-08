package mekanism.fabric_shim.client.gui.element.bar;

import net.minecraft.network.chat.Component;

/**
 * Server-safe stand-in for {@code mekanism.client.gui.element.bar.GuiBar.IBarInfoHandler} (a GUI
 * functional interface reached via RecipeViewerUtils). Full GUI wiring is Phase 4.
 */
public interface IBarInfoHandler {

    default Component getTooltip() {
        return Component.empty();
    }

    double getLevel();
}
