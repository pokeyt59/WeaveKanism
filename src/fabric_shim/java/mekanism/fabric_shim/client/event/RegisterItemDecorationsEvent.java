package mekanism.fabric_shim.client.event;

import mekanism.fabric_shim.client.IItemDecorator;
import mekanism.fabric_shim.client.extensions.ClientExtensionsHooks;
import mekanism.fabric_shim.fml.event.IModBusEvent;
import net.minecraft.world.level.ItemLike;
import net.neoforged.bus.api.Event;

/**
 * Stand-in for NeoForge's {@code RegisterItemDecorationsEvent}: registrations land in the
 * {@link ClientExtensionsHooks} decorator table. Posted from the client bootstrap in NeoForge's
 * client-init order.
 */
public class RegisterItemDecorationsEvent extends Event implements IModBusEvent {

    public void register(ItemLike item, IItemDecorator decorator) {
        ClientExtensionsHooks.registerDecorator(item.asItem(), decorator);
    }
}
