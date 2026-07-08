package mekanism.fabric_shim.event;

import mekanism.fabric_shim.fml.event.IModBusEvent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.Event;

/**
 * Stand-in for NeoForge's {@code BuildCreativeModeTabContentsEvent} (mod bus), implementing vanilla's
 * {@link CreativeModeTab.Output} so Mekanism's tab builders accept entries against it.
 *
 * <p>Compile-only: the Fabric equivalent populates tabs through {@code ItemGroupEvents}, wired in
 * Phase 3, so {@link #accept} currently discards entries. Tracked in the hook-wiring checklist.
 */
public class BuildCreativeModeTabContentsEvent extends Event implements IModBusEvent, CreativeModeTab.Output {

    private final ResourceKey<CreativeModeTab> tabKey;
    private final CreativeModeTab.ItemDisplayParameters parameters;

    public BuildCreativeModeTabContentsEvent(ResourceKey<CreativeModeTab> tabKey, CreativeModeTab.ItemDisplayParameters parameters) {
        this.tabKey = tabKey;
        this.parameters = parameters;
    }

    public ResourceKey<CreativeModeTab> getTabKey() {
        return this.tabKey;
    }

    public CreativeModeTab.ItemDisplayParameters getParameters() {
        return this.parameters;
    }

    @Override
    public void accept(ItemStack newEntry, CreativeModeTab.TabVisibility visibility) {
        //TODO(fabric-port, Phase 3): forward to the tab's ItemGroupEvents entries
    }
}
