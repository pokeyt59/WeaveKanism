package mekanism.fabric_shim.capabilities;

import java.util.HashMap;
import java.util.Map;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Block capability token backed by Fabric's {@link BlockApiLookup} (stand-in for NeoForge's
 * BlockCapability; same factory surface). NeoForge's {@code Level#getCapability} call sites map to
 * {@link #getCapability(Level, BlockPos, BlockState, BlockEntity, Object)}, which has the exact
 * parameter shape of {@link BlockApiLookup#find}.
 */
public final class BlockCapability<T, C> {

    private static final Map<ResourceLocation, BlockCapability<?, ?>> ALL = new HashMap<>();

    @SuppressWarnings("unchecked")
    public static synchronized <T, C> BlockCapability<T, C> create(ResourceLocation name, Class<T> typeClass, Class<C> contextClass) {
        BlockCapability<?, ?> existing = ALL.get(name);
        if (existing != null) {
            if (existing.typeClass != typeClass || existing.contextClass != contextClass) {
                throw new IllegalStateException("Block capability " + name + " already created with different type/context classes");
            }
            return (BlockCapability<T, C>) existing;
        }
        BlockCapability<T, C> capability = new BlockCapability<>(name, typeClass, contextClass);
        ALL.put(name, capability);
        return capability;
    }

    public static <T> BlockCapability<T, Void> createVoid(ResourceLocation name, Class<T> typeClass) {
        return create(name, typeClass, void.class);
    }

    public static <T> BlockCapability<T, @Nullable Direction> createSided(ResourceLocation name, Class<T> typeClass) {
        return create(name, typeClass, Direction.class);
    }

    private final ResourceLocation name;
    private final Class<T> typeClass;
    private final Class<C> contextClass;
    private final BlockApiLookup<T, C> lookup;

    private BlockCapability(ResourceLocation name, Class<T> typeClass, Class<C> contextClass) {
        this.name = name;
        this.typeClass = typeClass;
        this.contextClass = contextClass;
        this.lookup = BlockApiLookup.get(name, typeClass, contextClass);
    }

    public ResourceLocation name() {
        return name;
    }

    public Class<T> typeClass() {
        return typeClass;
    }

    public Class<C> contextClass() {
        return contextClass;
    }

    /**
     * The backing Fabric lookup, for provider registration (Phase 2 capability wiring).
     */
    public BlockApiLookup<T, C> lookup() {
        return lookup;
    }

    /**
     * Replacement for NeoForge's {@code level.getCapability(cap, pos, state, blockEntity, context)}.
     */
    @Nullable
    public T getCapability(Level level, BlockPos pos, @Nullable BlockState state, @Nullable BlockEntity blockEntity, C context) {
        return lookup.find(level, pos, state, blockEntity, context);
    }
}
