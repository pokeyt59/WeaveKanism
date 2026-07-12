package mekanism.fabric_shim.common.world.chunk;

import java.util.Objects;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import org.jetbrains.annotations.Nullable;

/**
 * Same surface as net.neoforged.neoforge.common.world.chunk.TicketController.
 *
 * <p>Phase 3 backing: {@link ForcedChunksSavedData} — per-owner tickets persisted per level, the
 * ticking flag honored (entity-ticking vs block-ticking region tickets), release refcounted across
 * owners, and {@link #callback()} invoked on level load with the persisted tickets (see
 * {@link ShimChunkManager}).
 */
public record TicketController(ResourceLocation id, @Nullable LoadingValidationCallback callback) {

    public TicketController {
        Objects.requireNonNull(id, "id must not be null");
    }

    public TicketController(ResourceLocation id) {
        this(id, null);
    }

    public boolean forceChunk(ServerLevel level, BlockPos owner, int chunkX, int chunkZ, boolean add, boolean ticking) {
        return ForcedChunksSavedData.get(level).forceBlock(level, id, owner, ChunkPos.asLong(chunkX, chunkZ), add, ticking);
    }

    public boolean forceChunk(ServerLevel level, Entity owner, int chunkX, int chunkZ, boolean add, boolean ticking) {
        return forceChunk(level, owner.getUUID(), chunkX, chunkZ, add, ticking);
    }

    public boolean forceChunk(ServerLevel level, UUID owner, int chunkX, int chunkZ, boolean add, boolean ticking) {
        return ForcedChunksSavedData.get(level).forceEntity(level, id, owner, ChunkPos.asLong(chunkX, chunkZ), add, ticking);
    }
}
