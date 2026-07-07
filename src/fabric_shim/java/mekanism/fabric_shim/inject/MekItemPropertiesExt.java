package mekanism.fabric_shim.inject;

import java.util.function.Supplier;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.Item;

/**
 * NeoForge's Item.Properties patch surface, attached via Loom interface injection +
 * ItemPropertiesMixin. {@link #setNoRepair()} needs state, so the mixin implements it; the
 * repair-blocking BEHAVIOR is not wired yet — tracked in the hook-wiring checklist (Phase 3).
 */
public interface MekItemPropertiesExt {

    default <T> Item.Properties component(Supplier<? extends DataComponentType<T>> type, T value) {
        return ((Item.Properties) this).component(type.get(), value);
    }

    Item.Properties setNoRepair();

    boolean mek_isNoRepair();
}
