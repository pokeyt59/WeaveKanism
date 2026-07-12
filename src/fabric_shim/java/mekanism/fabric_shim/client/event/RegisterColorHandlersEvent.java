package mekanism.fabric_shim.client.event;

import mekanism.fabric_shim.fml.event.IModBusEvent;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.world.level.ItemLike;
import net.neoforged.bus.api.Event;

/**
 * Stand-in for NeoForge's {@code RegisterColorHandlersEvent} (Block/Item slices): registrations
 * apply straight into Fabric's {@link ColorProviderRegistry}. Posted from the client bootstrap in
 * NeoForge's client-init order.
 */
public abstract class RegisterColorHandlersEvent extends Event implements IModBusEvent {

    public static class Block extends RegisterColorHandlersEvent {

        public void register(BlockColor blockColor, net.minecraft.world.level.block.Block... blocks) {
            ColorProviderRegistry.BLOCK.register(blockColor::getColor, blocks);
        }
    }

    public static class Item extends RegisterColorHandlersEvent {

        public void register(ItemColor itemColor, ItemLike... items) {
            ColorProviderRegistry.ITEM.register(itemColor::getColor, items);
        }
    }
}
