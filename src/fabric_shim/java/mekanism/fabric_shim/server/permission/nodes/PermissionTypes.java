package mekanism.fabric_shim.server.permission.nodes;

import net.minecraft.network.chat.Component;

/**
 * Same constants as net.neoforged.neoforge.server.permission.nodes.PermissionTypes.
 */
public final class PermissionTypes {

    private PermissionTypes() {
    }

    public static final PermissionType<Boolean> BOOLEAN = new PermissionType<>(Boolean.class, "boolean");
    public static final PermissionType<Integer> INTEGER = new PermissionType<>(Integer.class, "integer");
    public static final PermissionType<String> STRING = new PermissionType<>(String.class, "string");
    public static final PermissionType<Component> COMPONENT = new PermissionType<>(Component.class, "component");
}
