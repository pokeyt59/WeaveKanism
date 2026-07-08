package mekanism.fabric_shim.client;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Server-safe stand-in for {@code mekanism.client.SparkleAnimation} (multiblock-formation particles).
 * Common tiles construct and {@link #run} one when a multiblock forms; a no-op on the server. Phase 4
 * plays the animation.
 */
public final class SparkleAnimation {

    public SparkleAnimation(BlockEntity tile, BlockPos corner1, BlockPos corner2) {
    }

    public SparkleAnimation(BlockEntity tile, BlockPos renderLoc, int length, int width, int height) {
    }

    public void run() {
    }
}
