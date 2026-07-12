package mekanism.fabric_shim.common.world.chunk;

import net.minecraft.server.level.ServerLevel;

/**
 * Same surface as net.neoforged.neoforge.common.world.chunk.LoadingValidationCallback. Invoked per
 * level load by {@link ShimChunkManager} with the persisted per-owner tickets, before they are
 * re-applied as region tickets.
 */
@FunctionalInterface
public interface LoadingValidationCallback {

    void validateTickets(ServerLevel level, TicketHelper ticketHelper);
}
