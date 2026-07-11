package mekanism.fabric_shim.common.conditions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.util.function.Function;
import mekanism.fabric_shim.registries.NeoForgeRegistries;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;

/**
 * A loading condition for data files (stand-in for NeoForge's ICondition; minimal surface).
 * On Fabric the equivalent system is Fabric API's resource conditions; Mekanism's own condition
 * implementations get wired to it in Phase 3 (recipes/datagen).
 */
public interface ICondition {

    /**
     * Type-dispatch codec over the {@code neoforge:condition_codecs} registry, like upstream.
     * Lazy (unlike NeoForge's eager constant) because the backing Fabric registry is created in
     * {@link NeoForgeRegistries#init()} — resolution happens on first use, after bootstrap.
     */
    Codec<ICondition> CODEC = Codec.lazyInitialized(() -> {
        @SuppressWarnings("unchecked")
        Registry<MapCodec<? extends ICondition>> registry =
              (Registry<MapCodec<? extends ICondition>>) BuiltInRegistries.REGISTRY.get(NeoForgeRegistries.Keys.CONDITION_CODECS.location());
        if (registry == null) {
            throw new IllegalStateException("Condition codec registry requested before NeoForgeRegistries.init()");
        }
        return registry.byNameCodec().dispatch(ICondition::codec, Function.identity());
    });

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
