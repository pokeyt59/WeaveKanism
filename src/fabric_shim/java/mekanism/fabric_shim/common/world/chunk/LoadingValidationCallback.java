package mekanism.fabric_shim.common.world.chunk;

import net.minecraft.server.level.ServerLevel;

/**
 * Same surface as net.neoforged.neoforge.common.world.chunk.LoadingValidationCallback. Not invoked
 * in Phase 1 (the shim does not yet track ticket owners across restarts — see TicketController).
 */
@FunctionalInterface
public interface LoadingValidationCallback {

    void validateTickets(ServerLevel level, TicketHelper ticketHelper);
}
