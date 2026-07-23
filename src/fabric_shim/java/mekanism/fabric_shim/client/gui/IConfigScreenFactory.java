package mekanism.fabric_shim.client.gui;

import mekanism.fabric_shim.fml.ModContainer;
import net.minecraft.client.gui.screens.Screen;

/**
 * Stand-in for NeoForge's {@code IConfigScreenFactory} extension point: builds the mod's config
 * screen for a mod-list style UI. Registered through
 * {@link ModContainer#registerExtensionPoint}; the Phase 5 ModMenu bridge reads it back via
 * {@link ModContainer#getCustomExtension}.
 */
@FunctionalInterface
public interface IConfigScreenFactory {

    Screen createScreen(ModContainer container, Screen modListScreen);
}
