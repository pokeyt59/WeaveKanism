package mekanism.fabric_shim.inject;

import java.util.function.Supplier;
import net.minecraft.core.component.DataComponentHolder;
import net.minecraft.core.component.DataComponentType;
import org.jetbrains.annotations.Nullable;

/**
 * NeoForge's IDataComponentHolderExtension surface (Supplier overloads taking DeferredHolders),
 * attached to vanilla {@link DataComponentHolder} via Loom interface injection (compile time) +
 * DataComponentHolderMixin (runtime). Keeps ~100 upstream call sites textually unchanged.
 */
public interface MekDataComponentHolderExt {

    private DataComponentHolder self() {
        return (DataComponentHolder) this;
    }

    @Nullable
    default <T> T get(Supplier<? extends DataComponentType<? extends T>> type) {
        return self().get(type.get());
    }

    default <T> T getOrDefault(Supplier<? extends DataComponentType<? extends T>> type, T defaultValue) {
        return self().getOrDefault(type.get(), defaultValue);
    }

    default boolean has(Supplier<? extends DataComponentType<?>> type) {
        return self().has(type.get());
    }
}
