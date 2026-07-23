package mekanism.fabric_shim.client.model;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import mekanism.fabric_shim.client.model.geometry.IGeometryLoader;
import mekanism.fabric_shim.client.model.geometry.IUnbakedGeometry;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

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

    //NeoForge attaches parsed custom geometry ("loader" JSON key) to BlockModel via its customData
    // patch; this association map replaces the field. Weak keys: BlockModels are discarded on
    // resource reload (identity equals, so weak-identity semantics).
    private static final Map<BlockModel, IUnbakedGeometry<?>> CUSTOM_GEOMETRY = Collections.synchronizedMap(new WeakHashMap<>());

    /**
     * Null until the step-4-loader bridge parses loader JSON and records the geometry — callers
     * (BaseModelCache) already treat null as a plain JSON model.
     */
    @Nullable
    public static IUnbakedGeometry<?> getCustomGeometry(BlockModel model) {
        return CUSTOM_GEOMETRY.get(model);
    }

    public static void setCustomGeometry(BlockModel model, IUnbakedGeometry<?> geometry) {
        CUSTOM_GEOMETRY.put(model, geometry);
    }
}
