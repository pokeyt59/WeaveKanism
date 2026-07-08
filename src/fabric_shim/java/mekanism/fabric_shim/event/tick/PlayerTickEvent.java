package mekanism.fabric_shim.event.tick;

import mekanism.fabric_shim.event.entity.player.PlayerEvent;
import net.minecraft.world.entity.player.Player;

/**
 * Stand-in for NeoForge's {@code PlayerTickEvent}. Compile-only; firing is Phase 3.
 */
public abstract class PlayerTickEvent extends PlayerEvent {

    protected PlayerTickEvent(Player player) {
        super(player);
    }

    public static class Pre extends PlayerTickEvent {

        public Pre(Player player) {
            super(player);
        }
    }

    public static class Post extends PlayerTickEvent {

        public Post(Player player) {
            super(player);
        }
    }
}
