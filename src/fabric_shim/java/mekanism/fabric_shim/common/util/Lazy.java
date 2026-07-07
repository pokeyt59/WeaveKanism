package mekanism.fabric_shim.common.util;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * Same surface as net.neoforged.neoforge.common.util.Lazy (memoizing supplier).
 */
public interface Lazy<T> extends Supplier<T> {

    static <T> Lazy<T> of(Supplier<T> supplier) {
        Objects.requireNonNull(supplier);
        return new Lazy<>() {
            private T value;
            private boolean resolved;

            @Override
            public synchronized T get() {
                if (!resolved) {
                    value = supplier.get();
                    resolved = true;
                }
                return value;
            }
        };
    }
}
