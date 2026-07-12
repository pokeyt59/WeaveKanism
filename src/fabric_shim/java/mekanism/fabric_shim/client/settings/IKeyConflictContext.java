package mekanism.fabric_shim.client.settings;

/**
 * Same surface as net.neoforged.neoforge.client.settings.IKeyConflictContext (fresh
 * implementation): the context a key mapping is usable in; conflicts are only possible between
 * mappings whose contexts report conflicting.
 */
public interface IKeyConflictContext {

    /** true if conditions are met to activate key mappings with this context */
    boolean isActive();

    /** true if the other context can conflict with this one (checked on both contexts) */
    boolean conflicts(IKeyConflictContext other);
}
