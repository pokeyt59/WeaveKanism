package mekanism.fabric_shim.capabilities;

/**
 * Same surface as net.neoforged.neoforge.capabilities.ICapabilityInvalidationListener. On Fabric
 * there is no push invalidation; listeners are retained and consulted by the Phase 2/3 cache
 * eviction glue.
 */
@FunctionalInterface
public interface ICapabilityInvalidationListener {

    boolean onInvalidate();
}
