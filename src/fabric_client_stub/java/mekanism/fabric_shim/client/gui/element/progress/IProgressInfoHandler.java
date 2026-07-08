package mekanism.fabric_shim.client.gui.element.progress;

/**
 * Server-safe stand-in for {@code mekanism.client.gui.element.progress.IProgressInfoHandler} (a GUI
 * functional interface reached via RecipeViewerUtils). Full GUI wiring is Phase 4.
 */
public interface IProgressInfoHandler {

    double getProgress();

    default boolean isActive() {
        return true;
    }
}
