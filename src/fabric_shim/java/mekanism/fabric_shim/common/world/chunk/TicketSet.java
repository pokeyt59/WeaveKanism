package mekanism.fabric_shim.common.world.chunk;

import it.unimi.dsi.fastutil.longs.LongSet;

/**
 * Same surface as net.neoforged.neoforge.common.world.chunk.TicketSet.
 */
public record TicketSet(LongSet nonTicking, LongSet ticking) {
}
