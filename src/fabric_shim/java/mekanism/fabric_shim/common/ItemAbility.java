package mekanism.fabric_shim.common;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Named token describing an ability of a tool (stand-in for NeoForge's ItemAbility; same interned
 * get/name surface). Fabric has no item-ability system; call sites checking abilities are adapted
 * to conventional tags or direct checks in Phase 3, but the tokens themselves are loader-neutral.
 */
public final class ItemAbility {

    private static final Map<String, ItemAbility> ABILITIES = new ConcurrentHashMap<>();

    /**
     * Gets or creates the interned ability with the given name.
     */
    public static ItemAbility get(String name) {
        return ABILITIES.computeIfAbsent(name, ItemAbility::new);
    }

    private final String name;

    private ItemAbility(String name) {
        this.name = name;
    }

    public String name() {
        return name;
    }

    @Override
    public String toString() {
        return "ItemAbility[" + name + "]";
    }
}
