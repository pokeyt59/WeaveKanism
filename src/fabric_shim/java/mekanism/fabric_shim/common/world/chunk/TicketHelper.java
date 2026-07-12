package mekanism.fabric_shim.common.world.chunk;

import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;

/**
 * Same surface as net.neoforged.neoforge.common.world.chunk.TicketHelper. Handed to
 * {@link LoadingValidationCallback}s on level load with deep copies of the persisted tickets
 * (callbacks iterate these while removing — copies keep that safe, like NeoForge's immutable
 * views); removals write through to the level's {@link ForcedChunksSavedData}.
 */
public class TicketHelper {

    private final ForcedChunksSavedData data;
    private final ServerLevel level;
    private final ResourceLocation controller;
    private final Map<BlockPos, TicketSet> blockTickets;
    private final Map<UUID, TicketSet> entityTickets;

    TicketHelper(ForcedChunksSavedData data, ServerLevel level, ResourceLocation controller,
          Map<BlockPos, TicketSet> blockTickets, Map<UUID, TicketSet> entityTickets) {
        this.data = data;
        this.level = level;
        this.controller = controller;
        this.blockTickets = blockTickets;
        this.entityTickets = entityTickets;
    }

    public Map<BlockPos, TicketSet> getBlockTickets() {
        return blockTickets;
    }

    public Map<UUID, TicketSet> getEntityTickets() {
        return entityTickets;
    }

    public void removeAllTickets(BlockPos owner) {
        data.removeAllTickets(level, controller, d -> d.blockTickets, owner.immutable());
    }

    public void removeAllTickets(UUID owner) {
        data.removeAllTickets(level, controller, d -> d.entityTickets, owner);
    }

    public void removeTicket(BlockPos owner, long chunk, boolean ticking) {
        data.forceBlock(level, controller, owner, chunk, false, ticking);
    }

    public void removeTicket(UUID owner, long chunk, boolean ticking) {
        data.forceEntity(level, controller, owner, chunk, false, ticking);
    }
}
