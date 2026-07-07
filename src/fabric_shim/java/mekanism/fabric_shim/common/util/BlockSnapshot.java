package mekanism.fabric_shim.common.util;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Same surface (the slice Mekanism uses) as net.neoforged.neoforge.common.util.BlockSnapshot:
 * captures a block state for potential restoration.
 */
public final class BlockSnapshot {

    private final ResourceKey<Level> dimension;
    private final LevelAccessor level;
    private final BlockPos pos;
    private final BlockState state;

    private BlockSnapshot(ResourceKey<Level> dimension, LevelAccessor level, BlockPos pos, BlockState state) {
        this.dimension = dimension;
        this.level = level;
        this.pos = pos.immutable();
        this.state = state;
    }

    public static BlockSnapshot create(ResourceKey<Level> dimension, LevelAccessor level, BlockPos pos) {
        return new BlockSnapshot(dimension, level, pos, level.getBlockState(pos));
    }

    public ResourceKey<Level> getDimension() {
        return dimension;
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

    /**
     * Restores the captured state (NeoForge's restoreToLocation with default flags).
     */
    public boolean restore() {
        return level.setBlock(pos, state, net.minecraft.world.level.block.Block.UPDATE_ALL);
    }
}
