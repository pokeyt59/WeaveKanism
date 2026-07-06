package mekanism.fabric_shim.common.conditions;

import com.mojang.serialization.MapCodec;

/**
 * A loading condition for data files (stand-in for NeoForge's ICondition; minimal surface).
 * On Fabric the equivalent system is Fabric API's resource conditions; Mekanism's own condition
 * implementations get wired to it in Phase 3 (recipes/datagen).
 */
public interface ICondition {

    boolean test(IContext context);

    MapCodec<? extends ICondition> codec();

    /**
     * Context available during condition evaluation. Minimal for now; grows if Mekanism's
     * conditions need tag access.
     */
    interface IContext {

        IContext EMPTY = new IContext() {
        };
    }
}
