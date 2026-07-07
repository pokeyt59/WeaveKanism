package mekanism.fabric_shim.common.world.chunk;

import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;

/**
 * Same surface as net.neoforged.neoforge.common.world.chunk.TicketHelper. In Phase 1 the shim
 * never constructs one (loading validation callbacks are not invoked yet); the class exists so
 * upstream callback implementations compile unchanged.
 */
public class TicketHelper {

    private final Map<BlockPos, TicketSet> blockTickets;
    private final Map<UUID, TicketSet> entityTickets;

    TicketHelper(Map<BlockPos, TicketSet> blockTickets, Map<UUID, TicketSet> entityTickets) {
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
    }

    public void removeAllTickets(UUID owner) {
    }

    public void removeTicket(BlockPos owner, long chunk, boolean ticking) {
    }

    public void removeTicket(UUID owner, long chunk, boolean ticking) {
    }
}
