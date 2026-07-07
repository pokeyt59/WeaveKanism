package mekanism.fabric_shim.event.level;

import mekanism.fabric_shim.common.util.BlockSnapshot;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import org.jetbrains.annotations.Nullable;

/**
 * Same surface (the slice Mekanism uses) as net.neoforged.neoforge.event.level.BlockEvent.
 */
public abstract class BlockEvent extends Event {

    private final LevelAccessor level;
    private final BlockPos pos;
    private final BlockState state;

    public BlockEvent(LevelAccessor level, BlockPos pos, BlockState state) {
        this.level = level;
        this.pos = pos;
        this.state = state;
    }

    public LevelAccessor getLevel() {
        return level;
    }

    public BlockPos getPos() {
        return pos;
    }

    public BlockState getState() {
        return state;
    }

    public static class BreakEvent extends BlockEvent implements ICancellableEvent {

        private final Player player;

        public BreakEvent(LevelAccessor level, BlockPos pos, BlockState state, Player player) {
            super(level, pos, state);
            this.player = player;
        }

        public Player getPlayer() {
            return player;
        }
    }

    public static class EntityPlaceEvent extends BlockEvent implements ICancellableEvent {

        @Nullable
        private final Entity entity;
        private final BlockSnapshot blockSnapshot;
        private final BlockState placedBlock;
        private final BlockState placedAgainst;

        public EntityPlaceEvent(BlockSnapshot blockSnapshot, BlockState placedAgainst, @Nullable Entity entity) {
            super(blockSnapshot.getLevel(), blockSnapshot.getPos(), blockSnapshot.getState());
            this.entity = entity;
            this.blockSnapshot = blockSnapshot;
            this.placedBlock = blockSnapshot.getLevel().getBlockState(blockSnapshot.getPos());
            this.placedAgainst = placedAgainst;
        }

        @Nullable
        public Entity getEntity() {
            return entity;
        }

        public BlockSnapshot getBlockSnapshot() {
            return blockSnapshot;
        }

        public BlockState getPlacedBlock() {
            return placedBlock;
        }

        public BlockState getPlacedAgainst() {
            return placedAgainst;
        }
    }

    /**
     * Direction helper kept for source compatibility with hook call shapes.
     */
    public static Direction sanitize(@Nullable Direction direction) {
        return direction == null ? Direction.UP : direction;
    }
}
