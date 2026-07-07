package mekanism.fabric_shim.inject;

import java.util.function.Supplier;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

/**
 * NeoForge's BlockEntity.DataComponentInput patch surface, attached via Loom interface injection
 * + DataComponentInputMixin.
 */
public interface MekDataComponentInputExt {

    private BlockEntity.DataComponentInput self() {
        return (BlockEntity.DataComponentInput) this;
    }

    @Nullable
    default <T> T get(Supplier<? extends DataComponentType<? extends T>> type) {
        return self().get(type.get());
    }

    default <T> T getOrDefault(Supplier<? extends DataComponentType<? extends T>> type, T defaultValue) {
        return self().getOrDefault(type.get(), defaultValue);
    }
}
