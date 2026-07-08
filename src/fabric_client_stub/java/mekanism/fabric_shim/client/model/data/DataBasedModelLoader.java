package mekanism.fabric_shim.client.model.data;

/**
 * Server-safe stand-in for {@code mekanism.client.model.data.DataBasedModelLoader}. Common code only
 * references the {@link #EMITTING} model property token (attached to tiles' model data); the loader
 * itself is Phase 4.
 */
public final class DataBasedModelLoader {

    public static final ModelProperty<Void> EMITTING = new ModelProperty<>();

    private DataBasedModelLoader() {
    }
}
