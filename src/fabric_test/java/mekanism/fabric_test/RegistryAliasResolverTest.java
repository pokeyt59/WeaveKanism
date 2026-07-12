package mekanism.fabric_test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.HashSet;
import java.util.Set;
import mekanism.fabric_shim.registries.RegistryAliasResolver;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Guards the NeoForge alias semantics behind DeferredRegister#addAlias (mekanism:gases →
 * attached_chemicals, upgrade_gas → upgrade_chemical): registered ids always win over aliases,
 * chains follow to a registered id, and NeoForge's addAlias validation is preserved. The lookup
 * wiring itself (MappedRegistryMixin retry-on-miss) is boot-verified — this pins the resolution
 * rules it delegates to.
 */
class RegistryAliasResolverTest {

    private static final ResourceLocation GASES = rl("gases");
    private static final ResourceLocation CHEMICALS = rl("attached_chemicals");

    private final Set<ResourceLocation> registered = new HashSet<>();
    private final RegistryAliasResolver resolver = new RegistryAliasResolver(registered::contains);

    private static ResourceLocation rl(String path) {
        return ResourceLocation.fromNamespaceAndPath("mekanism", path);
    }

    @Test
    @DisplayName("alias resolves to its target; unaliased and registered names pass through unchanged")
    void basicResolution() {
        registered.add(CHEMICALS);
        resolver.addAlias(GASES, CHEMICALS);
        assertEquals(CHEMICALS, resolver.resolve(GASES));
        //Pass-through returns the same instance — the mixin uses identity to detect "no alias applied"
        ResourceLocation unrelated = rl("unrelated");
        assertSame(unrelated, resolver.resolve(unrelated));
        assertSame(CHEMICALS, resolver.resolve(CHEMICALS));
    }

    @Test
    @DisplayName("a registered id shadows its own alias (NeoForge: resolve short-circuits on containsKey)")
    void registeredNameWinsOverAlias() {
        resolver.addAlias(GASES, CHEMICALS);
        //Later something registers under the old name: lookups must now see the real entry
        registered.add(GASES);
        assertSame(GASES, resolver.resolve(GASES));
    }

    @Test
    @DisplayName("chains follow through unregistered intermediates and stop at a registered id")
    void chainResolution() {
        ResourceLocation infuseTypes = rl("infuse_types");
        registered.add(CHEMICALS);
        resolver.addAlias(infuseTypes, GASES);
        resolver.addAlias(GASES, CHEMICALS);
        assertEquals(CHEMICALS, resolver.resolve(infuseTypes));
    }

    @Test
    @DisplayName("a dangling chain returns the chain end (which then simply misses, like NeoForge)")
    void danglingChainReturnsEnd() {
        resolver.addAlias(GASES, CHEMICALS);
        assertEquals(CHEMICALS, resolver.resolve(GASES));
    }

    @Test
    @DisplayName("addAlias validation: self-alias no-ops, remapping an existing alias throws, re-adding an edge trips the loop detector")
    void addAliasValidation() {
        resolver.addAlias(GASES, GASES); //no-op
        assertSame(GASES, resolver.resolve(GASES));

        resolver.addAlias(GASES, CHEMICALS);
        assertThrows(IllegalStateException.class, () -> resolver.addAlias(GASES, rl("elsewhere")));
        //NeoForge quirk preserved: re-adding the same edge throws too, because resolve(from)
        //already lands on to (their "Infinite alias loop" check fires before the map put)
        assertThrows(IllegalStateException.class, () -> resolver.addAlias(GASES, CHEMICALS));
    }

    @Test
    @DisplayName("a cycle NeoForge's insertion check cannot see degrades to a miss instead of hanging")
    void cycleDegradesToMiss() {
        //resolve(c) == c while c is merely the END of a chain, so addAlias(c, a) passes NeoForge's
        //check and closes a ring through never-registered ids; NeoForge would recurse forever on
        //lookup — resolution here must still terminate
        ResourceLocation a = rl("a");
        ResourceLocation b = rl("b");
        ResourceLocation c = rl("c");
        resolver.addAlias(a, b);
        resolver.addAlias(b, c);
        resolver.addAlias(c, a);
        ResourceLocation resolved = resolver.resolve(a);
        //Terminates on the hop limit somewhere inside the ring — any of the three is "a miss"
        assertEquals("mekanism", resolved.getNamespace());
    }
}
