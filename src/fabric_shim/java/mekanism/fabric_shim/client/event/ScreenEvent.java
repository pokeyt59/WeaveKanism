package mekanism.fabric_shim.client.event;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

/**
 * Stand-in for NeoForge's {@code ScreenEvent} (game bus). Render.Post lets Mekanism push/pop the pose
 * around its gui draws (bridged off Fabric's ScreenEvents.afterRender); Opening lets it react to a
 * screen swap (only wired when a recipe viewer is present — inert on the port). Only the slice
 * Mekanism uses. Fresh implementation.
 */
public abstract class ScreenEvent extends Event {

    private final Screen screen;

    protected ScreenEvent(Screen screen) {
        this.screen = screen;
    }

    public Screen getScreen() {
        return screen;
    }

    public static class Opening extends ScreenEvent implements ICancellableEvent {

        private final Screen newScreen;

        public Opening(Screen currentScreen, Screen newScreen) {
            super(currentScreen);
            this.newScreen = newScreen;
        }

        public Screen getCurrentScreen() {
            return getScreen();
        }

        public Screen getNewScreen() {
            return newScreen;
        }
    }

    public abstract static class Render extends ScreenEvent {

        private final GuiGraphics guiGraphics;

        protected Render(Screen screen, GuiGraphics guiGraphics) {
            super(screen);
            this.guiGraphics = guiGraphics;
        }

        public GuiGraphics getGuiGraphics() {
            return guiGraphics;
        }

        public static class Post extends Render {

            public Post(Screen screen, GuiGraphics guiGraphics) {
                super(screen, guiGraphics);
            }
        }
    }
}
