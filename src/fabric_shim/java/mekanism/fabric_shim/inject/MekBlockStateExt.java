package mekanism.fabric_shim.inject;

import mekanism.fabric_shim.common.ItemAbility;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import org.jetbrains.annotations.Nullable;

/**
 * NeoForge's {@code IBlockStateExtension} surface (the slice Mekanism calls), injected onto vanilla
 * {@code BlockState} + BlockStateMixin. Compile-only defaults: tool-modified state and neighbor-change
 * hooks are inert (Phase 3/4); explosion resistance delegates to the block's vanilla value.
 */
public interface MekBlockStateExt {

    private BlockState self() {
        return (BlockState) this;
    }

    @Nullable
    default BlockState getToolModifiedState(UseOnContext context, ItemAbility itemAbility, boolean simulate) {
        return null;
    }

    default void onNeighborChange(LevelReader level, BlockPos pos, BlockPos neighbor) {
    }

    default float getExplosionResistance(BlockGetter level, BlockPos pos, Explosion explosion) {
        return self().getBlock().getExplosionResistance();
    }

    default boolean isFlammable(BlockGetter level, BlockPos pos, Direction face) {
        return false;
    }

    default void onCaughtFire(Level level, BlockPos pos, @Nullable Direction face, @Nullable LivingEntity igniter) {
    }

    default boolean onDestroyedByPlayer(Level level, BlockPos pos, Player player, boolean willHarvest, FluidState fluid) {
        return level.setBlock(pos, fluid.createLegacyBlock(), level.isClientSide ? 11 : 3);
    }

    default int getLightEmission(BlockGetter level, BlockPos pos) {
        return self().getLightEmission();
    }
}
