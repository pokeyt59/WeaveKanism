package mekanism.fabric_shim.inject;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.SignalGetter;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

/**
 * NeoForge's {@code IBlockExtension} surface (the slice Mekanism blocks call via {@code super}),
 * injected onto vanilla {@code Block} + BlockMixin. Only {@code onDestroyedByPlayer} is reached; the
 * default drains the block to its fluid state (or air), matching the vanilla break path.
 */
public interface MekBlockExt {

    default boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, boolean willHarvest, FluidState fluid) {
        return level.setBlock(pos, fluid.createLegacyBlock(), level.isClientSide ? 11 : 3);
    }

    default boolean shouldCheckWeakPower(BlockState state, SignalGetter level, BlockPos pos, Direction side) {
        return state.isRedstoneConductor(level, pos);
    }
}
