package mekanism.fabric_shim.inject;

import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * NeoForge's ItemStack data-component patch surface (Supplier overloads), attached to
 * {@link ItemStack} via Loom interface injection + ItemStackMixin.
 */
public interface MekItemStackExt {

    private ItemStack self() {
        return (ItemStack) this;
    }

    @Nullable
    default <T> T set(Supplier<? extends DataComponentType<T>> type, @Nullable T value) {
        return self().set(type.get(), value);
    }

    @Nullable
    default <T> T remove(Supplier<? extends DataComponentType<? extends T>> type) {
        return self().remove(type.get());
    }

    @Nullable
    default <T> T update(Supplier<? extends DataComponentType<T>> type, T defaultValue, UnaryOperator<T> updater) {
        return self().update(type.get(), defaultValue, updater);
    }
}
