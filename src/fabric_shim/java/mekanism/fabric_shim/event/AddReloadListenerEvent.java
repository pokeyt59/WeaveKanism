package mekanism.fabric_shim.event;

import java.util.ArrayList;
import java.util.List;
import mekanism.fabric_shim.common.conditions.ICondition;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.neoforged.bus.api.Event;
import org.jetbrains.annotations.Nullable;

/**
 * Fired on each datapack (re)load to collect server-side reload listeners (stand-in for
 * net.neoforged.neoforge.event.AddReloadListenerEvent). The shim glue registers one Fabric reload
 * listener that posts this event and then runs the collected listeners — synchronous
 * ResourceManagerReloadListeners are fully supported; listeners needing the async
 * prepare/apply contract are logged and skipped until a Phase 3 need arises.
 */
public class AddReloadListenerEvent extends Event {

    private final List<PreparableReloadListener> listeners = new ArrayList<>();
    @Nullable
    private final RegistryAccess registryAccess;

    public AddReloadListenerEvent(@Nullable RegistryAccess registryAccess) {
        this.registryAccess = registryAccess;
    }

    public void addListener(PreparableReloadListener listener) {
        listeners.add(listener);
    }

    public List<PreparableReloadListener> getListeners() {
        return List.copyOf(listeners);
    }

    public ICondition.IContext getConditionContext() {
        return ICondition.IContext.EMPTY;
    }

    @Nullable
    public RegistryAccess getRegistryAccess() {
        return registryAccess;
    }
}
