package mekanism.fabric_shim.event;

import java.util.stream.Stream;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.neoforged.bus.api.Event;
import org.jetbrains.annotations.Nullable;

/**
 * Stand-in for NeoForge's {@code OnDatapackSyncEvent}. Compile-only; firing (off Fabric's
 * {@code ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS} / player join) is Phase 3.
 */
public class OnDatapackSyncEvent extends Event {

    private final PlayerList playerList;
    @Nullable
    private final ServerPlayer player;

    public OnDatapackSyncEvent(PlayerList playerList, @Nullable ServerPlayer player) {
        this.playerList = playerList;
        this.player = player;
    }

    public PlayerList getPlayerList() {
        return this.playerList;
    }

    @Nullable
    public ServerPlayer getPlayer() {
        return this.player;
    }

    public Stream<ServerPlayer> getRelevantPlayers() {
        return this.player != null ? Stream.of(this.player) : this.playerList.getPlayers().stream();
    }
}
