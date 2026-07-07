package mekanism.fabric_shim.server.permission.nodes;

/**
 * Same surface as net.neoforged.neoforge.server.permission.nodes.PermissionDynamicContext.
 */
public final class PermissionDynamicContext<T> {

    private final PermissionDynamicContextKey<T> dynamic;
    private final T value;

    PermissionDynamicContext(PermissionDynamicContextKey<T> dynamic, T value) {
        this.dynamic = dynamic;
        this.value = value;
    }

    public PermissionDynamicContextKey<T> getDynamic() {
        return dynamic;
    }

    public T getValue() {
        return value;
    }

    public String getSerializedValue() {
        return dynamic.serializer().apply(value);
    }
}
