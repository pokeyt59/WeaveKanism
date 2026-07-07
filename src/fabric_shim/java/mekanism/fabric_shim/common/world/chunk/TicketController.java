package mekanism.fabric_shim.common.world.chunk;

import java.util.Objects;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

/**
 * Same surface as net.neoforged.neoforge.common.world.chunk.TicketController.
 *
 * <p>Phase 1 backing: vanilla forced chunks ({@link ServerLevel#setChunkForced}), which persist in
 * the vanilla saved data and always fully tick. Deviations from NeoForge, to revisit in Phase 3
 * (see PORTING.md): the {@code ticking} flag is ignored (vanilla forced chunks tick), per-owner
 * ticket tracking is not persisted (so {@code callback} is never invoked for load-time
 * validation), and unforcing a chunk that two owners forced releases it for both.
 */
public record TicketController(ResourceLocation id, @Nullable LoadingValidationCallback callback) {

    public TicketController {
        Objects.requireNonNull(id, "id must not be null");
    }

    public TicketController(ResourceLocation id) {
        this(id, null);
    }

    public boolean forceChunk(ServerLevel level, BlockPos owner, int chunkX, int chunkZ, boolean add, boolean ticking) {
        return level.setChunkForced(chunkX, chunkZ, add);
    }

    public boolean forceChunk(ServerLevel level, Entity owner, int chunkX, int chunkZ, boolean add, boolean ticking) {
        return forceChunk(level, owner.getUUID(), chunkX, chunkZ, add, ticking);
    }

    public boolean forceChunk(ServerLevel level, UUID owner, int chunkX, int chunkZ, boolean add, boolean ticking) {
        return level.setChunkForced(chunkX, chunkZ, add);
    }
}
