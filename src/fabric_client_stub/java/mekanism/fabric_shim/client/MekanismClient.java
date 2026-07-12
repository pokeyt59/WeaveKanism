package mekanism.fabric_shim.client;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import mekanism.common.Mekanism;
import mekanism.common.lib.security.SecurityData;
import mekanism.common.network.PacketUtils;
import mekanism.common.network.to_server.PacketKey;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/**
 * Server-safe stand-in for {@code mekanism.client.MekanismClient} (the real one is client-only, Phase
 * 4). Common code reads the client security/username caches and the current client player/level
 * through here; on a server these stay empty / null. Replaced by the real class when client rendering
 * lands. Lives in the portMain source set because it references src/main common types.
 */
public final class MekanismClient {

    public static final Map<UUID, SecurityData> clientSecurityMap = new HashMap<>();
    public static final Map<UUID, String> clientUUIDMap = new HashMap<>();

    private MekanismClient() {
    }

    @Nullable
    public static Player tryGetClientPlayer() {
        return null;
    }

    @Nullable
    public static Level tryGetClientWorld() {
        return null;
    }

    //Keybind → server key-state sync, mirroring the real MekanismClient (client-only call sites)
    public static void updateKey(KeyMapping key, int type) {
        updateKey(key.isDown(), type);
    }

    public static void updateKey(boolean pressed, int type) {
        Player player = Minecraft.getInstance().player;
        if (player != null) {
            UUID playerUUID = player.getUUID();
            boolean down = Minecraft.getInstance().screen == null && pressed;
            if (down != Mekanism.keyMap.has(playerUUID, type)) {
                PacketUtils.sendToServer(new PacketKey(type, down));
                Mekanism.keyMap.update(playerUUID, type, down);
            }
        }
    }
}
