package mekanism.fabric_shim.event.entity.player;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.bus.api.ICancellableEvent;
import org.jetbrains.annotations.Nullable;

/**
 * Stand-in for NeoForge's {@code PlayerInteractEvent}. Only the {@link RightClickBlock} sub-event and
 * the accessors Mekanism reads are provided. Compile-only; firing (off Fabric's {@code UseBlockCallback}
 * etc.) is Phase 3.
 */
public abstract class PlayerInteractEvent extends PlayerEvent {

    private final InteractionHand hand;
    private final BlockPos pos;
    @Nullable
    private final net.minecraft.core.Direction face;

    protected PlayerInteractEvent(Player player, InteractionHand hand, BlockPos pos, @Nullable net.minecraft.core.Direction face) {
        super(player);
        this.hand = hand;
        this.pos = pos;
        this.face = face;
    }

    public InteractionHand getHand() {
        return this.hand;
    }

    public BlockPos getPos() {
        return this.pos;
    }

    @Nullable
    public net.minecraft.core.Direction getFace() {
        return this.face;
    }

    public Level getLevel() {
        return getEntity().level();
    }

    public ItemStack getItemStack() {
        return getEntity().getItemInHand(this.hand);
    }

    public static class RightClickBlock extends PlayerInteractEvent implements ICancellableEvent {

        private final BlockHitResult hitVec;

        public RightClickBlock(Player player, InteractionHand hand, BlockPos pos, BlockHitResult hitVec) {
            super(player, hand, pos, hitVec.getDirection());
            this.hitVec = hitVec;
        }

        public BlockHitResult getHitVec() {
            return this.hitVec;
        }
    }
}
