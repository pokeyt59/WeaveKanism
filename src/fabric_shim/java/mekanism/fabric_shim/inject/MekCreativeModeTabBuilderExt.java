package mekanism.fabric_shim.inject;

import java.util.function.Function;
import net.minecraft.world.item.CreativeModeTab;

/**
 * NeoForge's {@code CreativeModeTab.Builder} patch surface (the slice Mekanism calls), injected via
 * Loom interface injection + CreativeModeTabBuilderMixin. Compile-only 1f defaults: the search bar
 * and custom tab factory are client presentation concerns picked back up in Phase 4 (documented
 * deviation in PORTING.md — tabs are plain {@link CreativeModeTab} instances until then).
 */
public interface MekCreativeModeTabBuilderExt {

    default CreativeModeTab.Builder withSearchBar() {
        return (CreativeModeTab.Builder) this;
    }

    default CreativeModeTab.Builder withTabFactory(Function<CreativeModeTab.Builder, CreativeModeTab> tabFactory) {
        return (CreativeModeTab.Builder) this;
    }
}
