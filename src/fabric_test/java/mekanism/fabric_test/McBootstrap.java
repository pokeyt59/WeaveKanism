package mekanism.fabric_test;

import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;

/**
 * One-time vanilla registry bootstrap for unit tests. Call from @BeforeAll in any test touching
 * game classes (registries, fluids, codecs).
 */
public final class McBootstrap {

    private static boolean done;

    private McBootstrap() {
    }

    public static synchronized void ensure() {
        if (!done) {
            done = true;
            SharedConstants.tryDetectVersion();
            Bootstrap.bootStrap();
        }
    }
}
