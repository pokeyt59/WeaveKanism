package mekanism.fabric_shim.network;

import mekanism.fabric_shim.network.client.ClientPacketHelper;
import mekanism.fabric_shim.server.ServerLifecycleHooks;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;

/**
 * Stand-in for NeoForge's {@code PacketDistributor} static senders, over Fabric's
 * {@link ServerPlayNetworking} + {@link PlayerLookup}. Serverbound sending ({@link #sendToServer}) is
 * a client operation, delegated to {@link ClientPacketHelper} so this common class never links
 * {@code ClientPlayNetworking} on a dedicated server.
 */
public final class PacketDistributor {

    private PacketDistributor() {
    }

    public static void sendToPlayer(ServerPlayer player, CustomPacketPayload payload, CustomPacketPayload... payloads) {
        ServerPlayNetworking.send(player, payload);
        for (CustomPacketPayload extra : payloads) {
            ServerPlayNetworking.send(player, extra);
        }
    }

    public static void sendToAllPlayers(CustomPacketPayload payload, CustomPacketPayload... payloads) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            return;
        }
        for (ServerPlayer player : PlayerLookup.all(server)) {
            sendToPlayer(player, payload, payloads);
        }
    }

    public static void sendToPlayersTrackingEntity(Entity entity, CustomPacketPayload payload, CustomPacketPayload... payloads) {
        for (ServerPlayer player : PlayerLookup.tracking(entity)) {
            sendToPlayer(player, payload, payloads);
        }
    }

    public static void sendToPlayersTrackingEntityAndSelf(Entity entity, CustomPacketPayload payload, CustomPacketPayload... payloads) {
        sendToPlayersTrackingEntity(entity, payload, payloads);
        if (entity instanceof ServerPlayer self) {
            sendToPlayer(self, payload, payloads);
        }
    }

    public static void sendToPlayersTrackingChunk(ServerLevel level, ChunkPos chunkPos, CustomPacketPayload payload, CustomPacketPayload... payloads) {
        for (ServerPlayer player : PlayerLookup.tracking(level, chunkPos)) {
            sendToPlayer(player, payload, payloads);
        }
    }

    public static void sendToServer(CustomPacketPayload payload, CustomPacketPayload... payloads) {
        ClientPacketHelper.sendToServer(payload);
        for (CustomPacketPayload extra : payloads) {
            ClientPacketHelper.sendToServer(extra);
        }
    }
}
