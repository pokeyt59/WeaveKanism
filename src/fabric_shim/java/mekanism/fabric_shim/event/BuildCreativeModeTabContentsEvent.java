package mekanism.fabric_shim.event;

import mekanism.fabric_shim.fml.event.IModBusEvent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.Event;

/**
 * Stand-in for NeoForge's {@code BuildCreativeModeTabContentsEvent} (mod bus), implementing vanilla's
 * {@link CreativeModeTab.Output} so Mekanism's tab builders accept entries against it. Fired per tab
 * from the {@code ItemGroupEvents.MODIFY_ENTRIES_ALL} bridge; entries forward to the Fabric entries
 * collector (itself a {@link CreativeModeTab.Output}).
 */
public class BuildCreativeModeTabContentsEvent extends Event implements IModBusEvent, CreativeModeTab.Output {

    private final ResourceKey<CreativeModeTab> tabKey;
    private final CreativeModeTab.ItemDisplayParameters parameters;
    private final CreativeModeTab.Output sink;

    public BuildCreativeModeTabContentsEvent(ResourceKey<CreativeModeTab> tabKey, CreativeModeTab.ItemDisplayParameters parameters, CreativeModeTab.Output sink) {
        this.tabKey = tabKey;
        this.parameters = parameters;
        this.sink = sink;
    }

    public ResourceKey<CreativeModeTab> getTabKey() {
        return this.tabKey;
    }

    public CreativeModeTab.ItemDisplayParameters getParameters() {
        return this.parameters;
    }

    @Override
    public void accept(ItemStack newEntry, CreativeModeTab.TabVisibility visibility) {
        sink.accept(newEntry, visibility);
    }
}
