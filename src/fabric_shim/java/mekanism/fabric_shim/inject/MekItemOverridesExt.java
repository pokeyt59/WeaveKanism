package mekanism.fabric_shim.inject;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemOverrides.BakedOverride;

/**
 * NeoForge's ItemOverrides patch surface: a public view of the baked override list (vanilla keeps a
 * private array — AW'd; the copy preserves its order, vanilla's reversed bake order). Injected onto
 * {@link ItemOverrides} + ItemOverridesMixin; ExtensionOverrideBakedModel chains through it.
 */
public interface MekItemOverridesExt {

    default ImmutableList<BakedOverride> getOverrides() {
        return ImmutableList.copyOf(((ItemOverrides) this).overrides);
    }
}
