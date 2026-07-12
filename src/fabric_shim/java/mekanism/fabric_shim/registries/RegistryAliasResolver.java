package mekanism.fabric_shim.registries;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import net.minecraft.resources.ResourceLocation;

/**
 * Alias table for one registry, with NeoForge's {@code addAlias}/{@code resolve} semantics
 * (fresh implementation): a name that is actually registered always wins over an alias; otherwise
 * aliases chain until they hit a registered id or run out. Adding a self-alias is a no-op,
 * re-mapping an existing alias to a different target throws, and an alias that immediately loops
 * back throws. Resolution is additionally hop-limited so a sneaky longer cycle degrades to
 * "unresolved" instead of hanging (NeoForge would recurse forever there).
 *
 * <p>The registry's live contents are supplied as a predicate so this stays a plain testable
 * class; {@code MappedRegistryMixin} feeds it the raw {@code byLocation} keyset (never the
 * alias-aware lookup methods, which would recurse).
 *
 * <p>Thread model matches NeoForge: writes only during single-threaded registration, lock-free
 * reads afterwards.
 */
public final class RegistryAliasResolver {

    private static final int MAX_HOPS = 64;

    private final Map<ResourceLocation, ResourceLocation> aliases = new HashMap<>();
    private final Predicate<ResourceLocation> contained;

    public RegistryAliasResolver(Predicate<ResourceLocation> contained) {
        this.contained = contained;
    }

    public boolean isEmpty() {
        return this.aliases.isEmpty();
    }

    public void addAlias(ResourceLocation from, ResourceLocation to) {
        if (from.equals(to)) {
            return;
        }
        ResourceLocation existing = this.aliases.get(from);
        if (existing != null && !existing.equals(to)) {
            throw new IllegalStateException("Duplicate alias with key \"" + from + "\" attempting to map to \"" + to
                                            + "\", found existing mapping \"" + existing + "\"");
        }
        if (resolve(from).equals(to)) {
            throw new IllegalStateException("Infinite alias loop detected: from " + from + " to " + to);
        }
        this.aliases.put(from, to);
    }

    /**
     * Follows the alias chain from {@code name}. At every step a registered id short-circuits, so
     * an alias can never shadow a real entry. Returns the last name reached — the input itself if
     * it is registered or unaliased, or the chain's end even when that end is not registered
     * (matching NeoForge, whose lookups then simply miss).
     */
    public ResourceLocation resolve(ResourceLocation name) {
        ResourceLocation current = name;
        for (int hops = 0; hops < MAX_HOPS; hops++) {
            if (this.contained.test(current)) {
                return current;
            }
            ResourceLocation next = this.aliases.get(current);
            if (next == null) {
                return current;
            }
            current = next;
        }
        return current;
    }
}
