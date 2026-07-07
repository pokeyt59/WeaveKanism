package mekanism.fabric_shim.client.model.data;

import java.util.function.Predicate;

/**
 * Same surface as net.neoforged.neoforge.client.model.data.ModelProperty. Loader-agnostic typed
 * key; identity-based like NeoForge's.
 */
public class ModelProperty<T> implements Predicate<T> {

    private final Predicate<T> predicate;

    public ModelProperty() {
        this(t -> true);
    }

    public ModelProperty(Predicate<T> predicate) {
        this.predicate = predicate;
    }

    @Override
    public boolean test(T t) {
        return predicate.test(t);
    }
}
