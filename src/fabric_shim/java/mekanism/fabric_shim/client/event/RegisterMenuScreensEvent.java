package mekanism.fabric_shim.client.event;

import java.util.Map;
import mekanism.fabric_shim.fml.event.IModBusEvent;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.MenuScreens.ScreenConstructor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.Event;

/**
 * Stand-in for NeoForge's {@code RegisterMenuScreensEvent}: registrations go straight into vanilla's
 * {@link MenuScreens} SCREENS map. Vanilla's {@code register} is private, so the port access-widens the
 * SCREENS field (fabric-port/extra.aw) and puts directly; the raw-typed put mirrors the map's wildcard
 * value type. Posted from the client bootstrap in NeoForge's client-init order.
 */
public class RegisterMenuScreensEvent extends Event implements IModBusEvent {

    @SuppressWarnings({"rawtypes", "unchecked"})
    public <M extends AbstractContainerMenu, U extends Screen & MenuAccess<M>> void register(MenuType<? extends M> menuType, ScreenConstructor<M, U> screenConstructor) {
        ((Map) MenuScreens.SCREENS).put(menuType, screenConstructor);
    }
}
