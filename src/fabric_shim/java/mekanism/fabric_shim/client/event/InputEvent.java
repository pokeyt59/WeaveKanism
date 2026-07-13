package mekanism.fabric_shim.client.event;

import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

/**
 * Stand-in for NeoForge's {@code InputEvent} (game bus) — the MouseScrollingEvent slice Mekanism uses
 * for mode-scroll on held items. Posted from a MouseHandler#onScroll mixin (step 3b); cancelling it
 * swallows the vanilla scroll. Fresh implementation.
 */
public abstract class InputEvent extends Event {

    public static class MouseScrollingEvent extends InputEvent implements ICancellableEvent {

        private final double scrollDeltaY;

        public MouseScrollingEvent(double scrollDeltaY) {
            this.scrollDeltaY = scrollDeltaY;
        }

        public double getScrollDeltaY() {
            return scrollDeltaY;
        }
    }
}
