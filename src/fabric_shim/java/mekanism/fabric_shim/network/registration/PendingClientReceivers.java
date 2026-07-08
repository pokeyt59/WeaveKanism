package mekanism.fabric_shim.network.registration;

import java.util.ArrayList;
import java.util.List;
import mekanism.fabric_shim.network.handling.IPayloadHandler;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Holds the payload handlers whose receivers can't be registered from common code during mod init:
 * clientbound play/configuration handlers (need {@code ClientPlayNetworking}, client-only) and the
 * unused serverbound-configuration handlers. {@link PayloadRegistrar} registers the wire codecs
 * immediately (both directions, via {@code PayloadTypeRegistry}) and parks the handlers here; the
 * Phase 4 client entry point drains {@link #clientbound()} into {@code ClientPlayNetworking}
 * receivers. Until then these payloads decode correctly but are not dispatched.
 */
public final class PendingClientReceivers {

    public record Entry<T extends CustomPacketPayload>(CustomPacketPayload.Type<T> type, IPayloadHandler<T> handler) {
    }

    private static final List<Entry<?>> CLIENTBOUND = new ArrayList<>();
    private static final List<Entry<?>> SERVERBOUND_CONFIG = new ArrayList<>();

    private PendingClientReceivers() {
    }

    public static <T extends CustomPacketPayload> void stashClientbound(CustomPacketPayload.Type<T> type, IPayloadHandler<T> handler) {
        CLIENTBOUND.add(new Entry<>(type, handler));
    }

    public static <T extends CustomPacketPayload> void stashServerboundConfig(CustomPacketPayload.Type<T> type, IPayloadHandler<T> handler) {
        SERVERBOUND_CONFIG.add(new Entry<>(type, handler));
    }

    /** Clientbound play + configuration handlers awaiting the Phase 4 client receiver registration. */
    public static List<Entry<?>> clientbound() {
        return CLIENTBOUND;
    }

    /** Serverbound configuration handlers (unused by Mekanism today) awaiting Phase 3 wiring. */
    public static List<Entry<?>> serverboundConfig() {
        return SERVERBOUND_CONFIG;
    }
}
