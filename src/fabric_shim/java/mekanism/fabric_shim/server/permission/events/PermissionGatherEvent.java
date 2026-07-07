package mekanism.fabric_shim.server.permission.events;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import mekanism.fabric_shim.server.permission.nodes.PermissionNode;
import net.neoforged.bus.api.Event;

/**
 * Same surface (the slice Mekanism uses) as
 * net.neoforged.neoforge.server.permission.events.PermissionGatherEvent. Fired on the game bus by
 * the shim glue when permission handling initializes (Phase 3/5 — currently collected nodes are
 * simply retained).
 */
public abstract class PermissionGatherEvent extends Event {

    public static class Nodes extends PermissionGatherEvent {

        private final List<PermissionNode<?>> nodes = new ArrayList<>();

        public Collection<PermissionNode<?>> getNodes() {
            return nodes;
        }

        public void addNodes(PermissionNode<?>... nodes) {
            this.nodes.addAll(Arrays.asList(nodes));
        }

        public void addNodes(Collection<PermissionNode<?>> nodes) {
            this.nodes.addAll(nodes);
        }
    }
}
