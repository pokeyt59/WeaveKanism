package mekanism.fabric_shim.client.model;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import mekanism.fabric_shim.client.model.geometry.IGeometryLoader;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;

/**
 * Registry behind the client model-registration shim surface: the geometry loaders registered via
 * {@code ModelEvent.RegisterGeometryLoaders} and the extra standalone models requested via
 * {@code ModelEvent.RegisterAdditional}. Populated during client init (single-threaded); the step-4
 * model bake pipeline (client-models.md) feeds these into Fabric's ModelLoadingPlugin. Insertion
 * order is preserved so replay matches NeoForge's registration order.
 */
public final class ClientModelHooks {

    private static final Map<ResourceLocation, IGeometryLoader<?>> LOADERS = new LinkedHashMap<>();
    private static final Set<ModelResourceLocation> ADDITIONAL_MODELS = new LinkedHashSet<>();

    private ClientModelHooks() {
    }

    public static void registerLoader(ResourceLocation id, IGeometryLoader<?> loader) {
        LOADERS.put(id, loader);
    }

    public static void registerAdditional(ModelResourceLocation model) {
        ADDITIONAL_MODELS.add(model);
    }

    public static Map<ResourceLocation, IGeometryLoader<?>> loaders() {
        return LOADERS;
    }

    public static Set<ModelResourceLocation> additionalModels() {
        return ADDITIONAL_MODELS;
    }
}
