package mekanism.fabric_shim.capabilities;

import java.util.Objects;
import java.util.function.BooleanSupplier;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiCache;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.Nullable;

/**
 * Same surface as net.neoforged.neoforge.capabilities.BlockCapabilityCache, backed by Fabric's
 * {@link BlockApiCache}: the block-entity lookup is cached and kept current by Fabric on BE
 * load/unload, so repeat neighbor queries skip the chunk/BE resolution. Fabric caches are
 * pull-based — the validity/invalidation parameters are retained for source compatibility but
 * never fire; NeoForge callers use them to drop the cache object itself, which is unnecessary
 * here (see transfer-bridge.md; benchmark transmitter networks before optimizing further).
 */
public final class BlockCapabilityCache<T, C> {

    private final BlockApiCache<T, C> cache;
    private final BlockCapability<T, C> capability;
    private final C context;

    private BlockCapabilityCache(BlockCapability<T, C> capability, ServerLevel level, BlockPos pos, C context) {
        this.capability = Objects.requireNonNull(capability);
        this.cache = BlockApiCache.create(capability.lookup(), Objects.requireNonNull(level), pos);
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
        return cache.getWorld();
    }

    public BlockPos pos() {
        return cache.getPos();
    }

    public C context() {
        return context;
    }

    public BlockCapability<T, C> capability() {
        return capability;
    }

    @Nullable
    public T getCapability() {
        return cache.find(context);
    }
}
