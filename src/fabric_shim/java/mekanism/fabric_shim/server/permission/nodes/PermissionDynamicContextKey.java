package mekanism.fabric_shim.server.permission.nodes;

import java.util.function.Function;

/**
 * Same surface as net.neoforged.neoforge.server.permission.nodes.PermissionDynamicContextKey.
 */
public record PermissionDynamicContextKey<T>(Class<T> typeToken, String name, Function<T, String> serializer) {

    public PermissionDynamicContext<T> createContext(T value) {
        return new PermissionDynamicContext<>(this, value);
    }
}
