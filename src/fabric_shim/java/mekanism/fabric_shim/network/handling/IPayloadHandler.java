package mekanism.fabric_shim.network.handling;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Functional handler for a received payload; same shape as NeoForge's {@code IPayloadHandler}.
 * Mekanism supplies these as {@code IMekanismPacket::handle} method references.
 */
@FunctionalInterface
public interface IPayloadHandler<T extends CustomPacketPayload> {

    void handle(T payload, IPayloadContext context);
}
