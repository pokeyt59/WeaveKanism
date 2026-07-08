package mekanism.fabric_shim.event.entity.player;

import mekanism.fabric_shim.event.entity.living.LivingEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.ICancellableEvent;
import java.util.Optional;

/**
 * Same surface (the slice Mekanism uses) as net.neoforged.neoforge.event.entity.player.PlayerEvent.
 */
public abstract class PlayerEvent extends LivingEvent {

    public PlayerEvent(Player player) {
        super(player);
    }

    @Override
    public Player getEntity() {
        return (Player) super.getEntity();
    }

    public static class PlayerLoggedInEvent extends PlayerEvent {

        public PlayerLoggedInEvent(Player player) {
            super(player);
        }
    }

    public static class PlayerLoggedOutEvent extends PlayerEvent {

        public PlayerLoggedOutEvent(Player player) {
            super(player);
        }
    }

    public static class PlayerChangedDimensionEvent extends PlayerEvent {

        private final ResourceKey<Level> from;
        private final ResourceKey<Level> to;

        public PlayerChangedDimensionEvent(Player player, ResourceKey<Level> from, ResourceKey<Level> to) {
            super(player);
            this.from = from;
            this.to = to;
        }

        public ResourceKey<Level> getFrom() {
            return from;
        }

        public ResourceKey<Level> getTo() {
            return to;
        }
    }

    public static class ItemCraftedEvent extends PlayerEvent {

        private final ItemStack crafting;
        private final Container craftMatrix;

        public ItemCraftedEvent(Player player, ItemStack crafting, Container craftMatrix) {
            super(player);
            this.crafting = crafting;
            this.craftMatrix = craftMatrix;
        }

        public ItemStack getCrafting() {
            return crafting;
        }

        public Container getInventory() {
            return craftMatrix;
        }
    }

    public static class StartTracking extends PlayerEvent {

        private final net.minecraft.world.entity.Entity target;

        public StartTracking(Player player, net.minecraft.world.entity.Entity target) {
            super(player);
            this.target = target;
        }

        public net.minecraft.world.entity.Entity getTarget() {
            return this.target;
        }
    }

    public static class StopTracking extends PlayerEvent {

        private final net.minecraft.world.entity.Entity target;

        public StopTracking(Player player, net.minecraft.world.entity.Entity target) {
            super(player);
            this.target = target;
        }

        public net.minecraft.world.entity.Entity getTarget() {
            return this.target;
        }
    }

    public static class PlayerRespawnEvent extends PlayerEvent {

        private final boolean endConquered;

        public PlayerRespawnEvent(Player player, boolean endConquered) {
            super(player);
            this.endConquered = endConquered;
        }

        public boolean isEndConquered() {
            return this.endConquered;
        }
    }

    public static class BreakSpeed extends PlayerEvent implements ICancellableEvent {

        private final BlockState state;
        private final Optional<BlockPos> pos;
        private final float originalSpeed;
        private float newSpeed;

        public BreakSpeed(Player player, BlockState state, float original, Optional<BlockPos> pos) {
            super(player);
            this.state = state;
            this.originalSpeed = original;
            this.newSpeed = original;
            this.pos = pos;
        }

        public BlockState getState() {
            return state;
        }

        public Optional<BlockPos> getPosition() {
            return pos;
        }

        public float getOriginalSpeed() {
            return originalSpeed;
        }

        public float getNewSpeed() {
            return newSpeed;
        }

        public void setNewSpeed(float speed) {
            this.newSpeed = speed;
        }
    }
}
