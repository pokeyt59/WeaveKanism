package mekanism.fabric_shim.inject;

import mekanism.fabric_shim.capabilities.BlockCapability;
import mekanism.fabric_shim.capabilities.ICapabilityInvalidationListener;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * NeoForge's {@code ILevelExtension} capability accessors, injected onto vanilla {@link Level} +
 * LevelMixin. Delegates to the shim {@link BlockCapability} token's lookup.
 * {@code invalidateCapabilities} is a no-op for 1f (Phase 2 turns it into cache eviction).
 */
public interface MekLevelExt {

    private Level self() {
        return (Level) this;
    }

    default <T, C> T getCapability(BlockCapability<T, C> cap, BlockPos pos, C context) {
        return cap.getCapability(self(), pos, null, null, context);
    }

    default <T, C> T getCapability(BlockCapability<T, C> cap, BlockPos pos, @Nullable BlockState state, @Nullable BlockEntity blockEntity, C context) {
        return cap.getCapability(self(), pos, state, blockEntity, context);
    }

    default <T> T getCapability(BlockCapability<T, @Nullable Void> cap, BlockPos pos) {
        return cap.getCapability(self(), pos, null, null, null);
    }

    default <T> T getCapability(BlockCapability<T, @Nullable Void> cap, BlockPos pos, @Nullable BlockState state, @Nullable BlockEntity blockEntity) {
        return cap.getCapability(self(), pos, state, blockEntity, null);
    }

    default void invalidateCapabilities(BlockPos pos) {
    }

    default void invalidateCapabilities(ChunkPos pos) {
    }

    default void registerCapabilityListener(BlockPos pos, ICapabilityInvalidationListener listener) {
    }

    default <T> java.util.Optional<net.minecraft.core.Holder.Reference<T>> holder(net.minecraft.resources.ResourceKey<T> key) {
        net.minecraft.resources.ResourceKey<? extends net.minecraft.core.Registry<? extends T>> registryKey =
              net.minecraft.resources.ResourceKey.createRegistryKey(key.registry());
        return self().registryAccess().lookup(registryKey).flatMap(lookup -> lookup.get(key));
    }
}
