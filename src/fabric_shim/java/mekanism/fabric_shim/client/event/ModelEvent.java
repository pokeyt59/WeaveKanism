package mekanism.fabric_shim.client.event;

import java.util.Map;
import mekanism.fabric_shim.client.model.ClientModelHooks;
import mekanism.fabric_shim.client.model.geometry.IGeometryLoader;
import mekanism.fabric_shim.fml.event.IModBusEvent;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.Event;

/**
 * Stand-in for NeoForge's {@code ModelEvent} family (the slice Mekanism uses). RegisterGeometryLoaders
 * and RegisterAdditional record into {@link ClientModelHooks}; the step-4 bake pipeline consumes them
 * (client-models.md). ModifyBakingResult and BakingCompleted are data-carrying views over the freshly
 * baked model map, constructed by the client bootstrap after model baking (step 7 wiring).
 */
public abstract class ModelEvent extends Event {

    public static class RegisterGeometryLoaders extends ModelEvent implements IModBusEvent {

        public void register(ResourceLocation id, IGeometryLoader<?> loader) {
            ClientModelHooks.registerLoader(id, loader);
        }
    }

    public static class RegisterAdditional extends ModelEvent implements IModBusEvent {

        public void register(ModelResourceLocation model) {
            ClientModelHooks.registerAdditional(model);
        }
    }

    public static class ModifyBakingResult extends ModelEvent implements IModBusEvent {

        private final Map<ModelResourceLocation, BakedModel> models;

        public ModifyBakingResult(Map<ModelResourceLocation, BakedModel> models) {
            this.models = models;
        }

        public Map<ModelResourceLocation, BakedModel> getModels() {
            return models;
        }
    }

    public static class BakingCompleted extends ModelEvent implements IModBusEvent {

        private final Map<ModelResourceLocation, BakedModel> models;
        private final ModelManager modelManager;
        private final ModelBakery modelBakery;

        public BakingCompleted(Map<ModelResourceLocation, BakedModel> models, ModelManager modelManager, ModelBakery modelBakery) {
            this.models = models;
            this.modelManager = modelManager;
            this.modelBakery = modelBakery;
        }

        public Map<ModelResourceLocation, BakedModel> getModels() {
            return models;
        }

        public ModelManager getModelManager() {
            return modelManager;
        }

        public ModelBakery getModelBakery() {
            return modelBakery;
        }
    }
}
