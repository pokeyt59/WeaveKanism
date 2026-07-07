package mekanism.fabric_shim.common;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import mekanism.fabric_shim.server.ServerLifecycleHooks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.players.GameProfileCache;
import org.jetbrains.annotations.Nullable;

/**
 * Same surface (the slice Mekanism uses) as net.neoforged.neoforge.common.UsernameCache. Backed by
 * names observed this session plus the server's profile cache; NeoForge's disk-persisted cache
 * file is not replicated.
 */
public final class UsernameCache {

    private static final Map<UUID, String> SEEN = new ConcurrentHashMap<>();

    private UsernameCache() {
    }

    public static void setUsername(UUID uuid, String username) {
        SEEN.put(uuid, username);
    }

    @Nullable
    public static String getLastKnownUsername(UUID uuid) {
        String known = SEEN.get(uuid);
        if (known != null) {
            return known;
        }
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            GameProfileCache cache = server.getProfileCache();
            if (cache != null) {
                return cache.get(uuid).map(profile -> {
                    SEEN.put(uuid, profile.getName());
                    return profile.getName();
                }).orElse(null);
            }
        }
        return null;
    }
}
