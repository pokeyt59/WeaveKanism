package mekanism.fabric_shim.server.permission;

import java.util.UUID;
import mekanism.fabric_shim.server.permission.nodes.PermissionDynamicContext;
import mekanism.fabric_shim.server.permission.nodes.PermissionNode;
import net.minecraft.server.level.ServerPlayer;

/**
 * Same surface (the slice Mekanism uses) as net.neoforged.neoforge.server.permission.PermissionAPI.
 * No permission-handler mods are integrated yet, so every query resolves through the node's
 * default resolver (which for Mekanism's nodes checks vanilla op levels). A fabric-permissions-api
 * backed handler is a Phase 5 option.
 */
public final class PermissionAPI {

    private PermissionAPI() {
    }

    public static <T> T getPermission(ServerPlayer player, PermissionNode<T> node, PermissionDynamicContext<?>... context) {
        return node.getDefaultResolver().resolve(player, player.getUUID(), context);
    }

    public static <T> T getOfflinePermission(UUID player, PermissionNode<T> node, PermissionDynamicContext<?>... context) {
        return node.getDefaultResolver().resolve(null, player, context);
    }
}
