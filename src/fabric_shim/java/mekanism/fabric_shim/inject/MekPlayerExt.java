package mekanism.fabric_shim.inject;

import java.util.OptionalInt;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;

/**
 * NeoForge's {@code IPlayerExtension.openMenu} overloads that pass extra opening data. Injected onto
 * vanilla {@link Player} + PlayerMixin; delegates to vanilla {@code openMenu(MenuProvider)}. The extra
 * data writer is ignored for 1f (Phase 3 sends it via extended screen handlers), so menus open but
 * without their initial sync payload until then.
 */
public interface MekPlayerExt {

    default OptionalInt openMenu(MenuProvider menuProvider, Consumer<RegistryFriendlyByteBuf> extraDataWriter) {
        return ((Player) this).openMenu(menuProvider);
    }

    default OptionalInt openMenu(MenuProvider menuProvider, BlockPos pos) {
        return ((Player) this).openMenu(menuProvider);
    }
}
