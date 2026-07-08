package mekanism.fabric_shim.client.model.data;

import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import java.util.Map;
import java.util.Set;
import org.jetbrains.annotations.Nullable;

/**
 * Same surface as net.neoforged.neoforge.client.model.data.ModelData (immutable typed map).
 * Loader-agnostic data holder; tiles keep their getModelData() methods and Phase 4 bridges this to
 * Fabric's rendering data attachment.
 */
public final class ModelData {

    public static final ModelData EMPTY = ModelData.builder().build();

    private final Map<ModelProperty<?>, Object> properties;

    private ModelData(Map<ModelProperty<?>, Object> properties) {
        this.properties = properties;
    }

    public Set<ModelProperty<?>> getProperties() {
        return properties.keySet();
    }

    public boolean has(ModelProperty<?> property) {
        return properties.containsKey(property);
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public <T> T get(ModelProperty<T> property) {
        return (T) properties.get(property);
    }

    public Builder derive() {
        return new Builder(this);
    }

    public static Builder builder() {
        return new Builder(null);
    }

    /** Convenience for a single-property model data (NeoForge parity); accepts a null value. */
    public static <T> ModelData of(ModelProperty<T> property, @Nullable T value) {
        Map<ModelProperty<?>, Object> map = new Reference2ObjectArrayMap<>();
        map.put(property, value);
        return new ModelData(map);
    }

    public static final class Builder {

        private final Map<ModelProperty<?>, Object> properties = new Reference2ObjectArrayMap<>();

        private Builder(@Nullable ModelData parent) {
            if (parent != null) {
                properties.putAll(parent.properties);
            }
        }

        public <T> Builder with(ModelProperty<T> property, T value) {
            if (!property.test(value)) {
                throw new IllegalArgumentException("Value is invalid for this property");
            }
            properties.put(property, value);
            return this;
        }

        public ModelData build() {
            return new ModelData(properties);
        }
    }
}
