package mekanism.fabric_shim.client.extensions;

import java.util.Arrays;
import mekanism.fabric_shim.fluids.FluidType;
import mekanism.fabric_shim.fml.event.IModBusEvent;
import net.minecraft.core.Holder;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.Event;

/**
 * Stand-in for NeoForge's {@code RegisterClientExtensionsEvent} (same surface for the overloads
 * Mekanism uses): registrations land in {@link ClientExtensionsHooks}. Posted from the client
 * bootstrap in NeoForge's client-init order.
 */
public class RegisterClientExtensionsEvent extends Event implements IModBusEvent {

    public void registerBlock(IClientBlockExtensions extensions, Block... blocks) {
        for (Block block : blocks) {
            ClientExtensionsHooks.registerBlock(extensions, block);
        }
    }

    @SafeVarargs
    public final void registerBlock(IClientBlockExtensions extensions, Holder<Block>... blocks) {
        registerBlock(extensions, Arrays.stream(blocks).map(Holder::value).toArray(Block[]::new));
    }

    public void registerItem(IClientItemExtensions extensions, Item... items) {
        for (Item item : items) {
            ClientExtensionsHooks.registerItem(extensions, item);
        }
    }

    @SafeVarargs
    public final void registerItem(IClientItemExtensions extensions, Holder<Item>... items) {
        registerItem(extensions, Arrays.stream(items).map(Holder::value).toArray(Item[]::new));
    }

    public void registerFluidType(IClientFluidTypeExtensions extensions, FluidType... fluidTypes) {
        for (FluidType fluidType : fluidTypes) {
            ClientExtensionsHooks.registerFluidType(extensions, fluidType);
        }
    }
}
