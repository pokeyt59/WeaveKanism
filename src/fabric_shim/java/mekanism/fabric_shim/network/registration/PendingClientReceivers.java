package mekanism.fabric_shim.network.registration;

import java.util.ArrayList;
import java.util.List;
import mekanism.fabric_shim.network.handling.IPayloadHandler;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Holds the payload handlers whose receivers can't be registered from common code during mod init:
 * clientbound play/configuration handlers (need {@code ClientPlayNetworking}/
 * {@code ClientConfigurationNetworking}, client-only) and the unused serverbound-configuration
 * handlers. {@link PayloadRegistrar} registers the wire codecs immediately (both directions, via
 * {@code PayloadTypeRegistry}) and parks the handlers here, split by protocol phase — each phase
 * has its own client receiver registry, and registering against the wrong one throws.
 * {@code MekanismFabricClient} drains the two clientbound lists at client init.
 */
public final class PendingClientReceivers {

    public record Entry<T extends CustomPacketPayload>(CustomPacketPayload.Type<T> type, IPayloadHandler<T> handler) {
    }

    private static final List<Entry<?>> CLIENTBOUND_PLAY = new ArrayList<>();
    private static final List<Entry<?>> CLIENTBOUND_CONFIG = new ArrayList<>();
    private static final List<Entry<?>> SERVERBOUND_CONFIG = new ArrayList<>();

    private PendingClientReceivers() {
    }

    public static <T extends CustomPacketPayload> void stashClientboundPlay(CustomPacketPayload.Type<T> type, IPayloadHandler<T> handler) {
        CLIENTBOUND_PLAY.add(new Entry<>(type, handler));
    }

    public static <T extends CustomPacketPayload> void stashClientboundConfig(CustomPacketPayload.Type<T> type, IPayloadHandler<T> handler) {
        CLIENTBOUND_CONFIG.add(new Entry<>(type, handler));
    }

    public static <T extends CustomPacketPayload> void stashServerboundConfig(CustomPacketPayload.Type<T> type, IPayloadHandler<T> handler) {
        SERVERBOUND_CONFIG.add(new Entry<>(type, handler));
    }

    /** Clientbound play handlers, registered into {@code ClientPlayNetworking} at client init. */
    public static List<Entry<?>> clientboundPlay() {
        return CLIENTBOUND_PLAY;
    }

    /** Clientbound configuration handlers, registered into {@code ClientConfigurationNetworking} at client init. */
    public static List<Entry<?>> clientboundConfig() {
        return CLIENTBOUND_CONFIG;
    }

    /** Serverbound configuration handlers (unused by Mekanism today; see hook-wiring checklist). */
    public static List<Entry<?>> serverboundConfig() {
        return SERVERBOUND_CONFIG;
    }
}
