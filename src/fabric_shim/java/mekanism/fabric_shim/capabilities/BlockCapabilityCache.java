package mekanism.fabric_shim.capabilities;

import java.util.Objects;
import java.util.function.BooleanSupplier;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.Nullable;

/**
 * Same surface as net.neoforged.neoforge.capabilities.BlockCapabilityCache.
 *
 * <p>Phase 1f implementation: every {@link #getCapability()} re-queries the lookup (Fabric caches
 * are pull-based; there is no push invalidation). The validity/invalidation parameters are
 * retained for source compatibility. Phase 2 replaces the body with a Fabric BlockApiCache +
 * neighbor-changed eviction and benchmarks transmitter networks — see transfer-bridge.md.
 */
public final class BlockCapabilityCache<T, C> {

    private final BlockCapability<T, C> capability;
    private final ServerLevel level;
    private final BlockPos pos;
    private final C context;

    private BlockCapabilityCache(BlockCapability<T, C> capability, ServerLevel level, BlockPos pos, C context) {
        this.capability = Objects.requireNonNull(capability);
        this.level = Objects.requireNonNull(level);
        this.pos = pos.immutable();
        this.context = context;
    }

    public static <T, C> BlockCapabilityCache<T, C> create(BlockCapability<T, C> capability, ServerLevel level, BlockPos pos, C context) {
        return new BlockCapabilityCache<>(capability, level, pos, context);
    }

    public static <T, C> BlockCapabilityCache<T, C> create(BlockCapability<T, C> capability, ServerLevel level, BlockPos pos, C context,
          BooleanSupplier isValid, Runnable invalidationListener) {
        return new BlockCapabilityCache<>(capability, level, pos, context);
    }

    public ServerLevel level() {
        return level;
    }

    public BlockPos pos() {
        return pos;
    }

    public C context() {
        return context;
    }

    @Nullable
    public T getCapability() {
        return capability.getCapability(level, pos, null, null, context);
    }
}
