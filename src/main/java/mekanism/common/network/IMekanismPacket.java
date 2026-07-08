package mekanism.common.network;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import mekanism.fabric_shim.network.handling.IPayloadContext;

public interface IMekanismPacket extends CustomPacketPayload {

    void handle(IPayloadContext context);
}