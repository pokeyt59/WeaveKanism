package mekanism.fabric_shim.network.registration;

import mekanism.fabric_shim.network.handling.IPayloadHandler;
import mekanism.fabric_shim.network.handling.ServerPayloadContext;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Stand-in for NeoForge's {@code PayloadRegistrar}. Registers each payload's wire codec with Fabric's
 * {@link PayloadTypeRegistry} (both directions, common-safe) so serialization matches upstream, then:
 * <ul>
 *   <li>serverbound play packets get a live {@link ServerPlayNetworking} receiver (the common runtime
 *       path — a client's input is handled on the server);</li>
 *   <li>clientbound + configuration handlers are parked in {@link PendingClientReceivers} for the
 *       Phase 4 client entry point / Phase 3 configuration wiring.</li>
 * </ul>
 *
 * <p>The {@code version} is accepted for NeoForge source-compatibility; Fabric has no per-channel
 * protocol versioning, so it is unused.
 */
public class PayloadRegistrar {

    @SuppressWarnings("unused")
    private final String version;

    public PayloadRegistrar(String version) {
        this.version = version;
    }

    public <T extends CustomPacketPayload> PayloadRegistrar playToServer(CustomPacketPayload.Type<T> type,
          StreamCodec<? super RegistryFriendlyByteBuf, T> reader, IPayloadHandler<T> handler) {
        PayloadTypeRegistry.playC2S().register(type, reader);
        ServerPlayNetworking.registerGlobalReceiver(type, (payload, context) -> handler.handle(payload, new ServerPayloadContext(context)));
        return this;
    }

    public <T extends CustomPacketPayload> PayloadRegistrar playToClient(CustomPacketPayload.Type<T> type,
          StreamCodec<? super RegistryFriendlyByteBuf, T> reader, IPayloadHandler<T> handler) {
        PayloadTypeRegistry.playS2C().register(type, reader);
        PendingClientReceivers.stashClientboundPlay(type, handler);
        return this;
    }

    public <T extends CustomPacketPayload> PayloadRegistrar configurationToServer(CustomPacketPayload.Type<T> type,
          StreamCodec<? super FriendlyByteBuf, T> reader, IPayloadHandler<T> handler) {
        PayloadTypeRegistry.configurationC2S().register(type, reader);
        PendingClientReceivers.stashServerboundConfig(type, handler);
        return this;
    }

    public <T extends CustomPacketPayload> PayloadRegistrar configurationToClient(CustomPacketPayload.Type<T> type,
          StreamCodec<? super FriendlyByteBuf, T> reader, IPayloadHandler<T> handler) {
        PayloadTypeRegistry.configurationS2C().register(type, reader);
        PendingClientReceivers.stashClientboundConfig(type, handler);
        return this;
    }
}
