package mekanism.fabric_shim.inject;

import java.util.function.Supplier;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import org.jetbrains.annotations.Nullable;

/**
 * NeoForge's DataComponentMap.Builder patch surface, attached via Loom interface injection +
 * DataComponentMapBuilderMixin.
 */
public interface MekDataComponentMapBuilderExt {

    default <T> DataComponentMap.Builder set(Supplier<? extends DataComponentType<T>> type, @Nullable T value) {
        return ((DataComponentMap.Builder) this).set(type.get(), value);
    }
}
