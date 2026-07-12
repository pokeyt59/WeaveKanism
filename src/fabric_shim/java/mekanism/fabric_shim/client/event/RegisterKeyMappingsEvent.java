package mekanism.fabric_shim.client.event;

import mekanism.fabric_shim.fml.event.IModBusEvent;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.neoforged.bus.api.Event;

/**
 * Stand-in for NeoForge's {@code RegisterKeyMappingsEvent}: registrations apply straight into
 * Fabric's {@link KeyBindingHelper} (which adds them to the options screen and the vanilla
 * mapping array). Posted from the client bootstrap during client setup, like NeoForge's order.
 */
public class RegisterKeyMappingsEvent extends Event implements IModBusEvent {

    public void register(KeyMapping key) {
        KeyBindingHelper.registerKeyBinding(key);
    }
}
