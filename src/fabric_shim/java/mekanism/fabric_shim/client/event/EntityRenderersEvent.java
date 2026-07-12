package mekanism.fabric_shim.client.event;

import java.util.Collection;
import java.util.Map;
import java.util.function.Supplier;
import mekanism.fabric_shim.fml.event.IModBusEvent;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

/**
 * Stand-in for NeoForge's {@code EntityRenderersEvent} family (the slice Mekanism uses).
 * RegisterRenderers/RegisterLayerDefinitions apply straight into the vanilla/Fabric registries;
 * AddLayers is a data-carrying view over the built renderer maps, posted after renderers are
 * (re)created (client bootstrap wiring — see client-compile-plan.md step 7).
 */
public abstract class EntityRenderersEvent extends net.neoforged.bus.api.Event implements IModBusEvent {

    public static class RegisterRenderers extends EntityRenderersEvent {

        public <T extends Entity> void registerEntityRenderer(EntityType<? extends T> entityType, EntityRendererProvider<T> provider) {
            //Vanilla EntityRenderers.register is private; Fabric's registry is the public door to it
            EntityRendererRegistry.register(entityType, provider);
        }

        public <T extends BlockEntity> void registerBlockEntityRenderer(BlockEntityType<? extends T> blockEntityType, BlockEntityRendererProvider<T> provider) {
            BlockEntityRenderers.register(blockEntityType, provider);
        }
    }

    public static class RegisterLayerDefinitions extends EntityRenderersEvent {

        public void registerLayerDefinition(ModelLayerLocation layerLocation, Supplier<LayerDefinition> supplier) {
            EntityModelLayerRegistry.registerModelLayer(layerLocation, supplier::get);
        }
    }

    /**
     * View over the freshly built renderer maps so layer-adding handlers can decorate them.
     * Constructed by the client bootstrap after the entity render dispatcher (re)builds.
     */
    public static class AddLayers extends EntityRenderersEvent {

        private final Map<EntityType<?>, EntityRenderer<?>> renderers;
        private final Map<PlayerSkin.Model, EntityRenderer<?>> skinMap;
        private final EntityRendererProvider.Context context;

        public AddLayers(Map<EntityType<?>, EntityRenderer<?>> renderers, Map<PlayerSkin.Model, EntityRenderer<?>> skinMap,
              EntityRendererProvider.Context context) {
            this.renderers = renderers;
            this.skinMap = skinMap;
            this.context = context;
        }

        public Collection<PlayerSkin.Model> getSkins() {
            return skinMap.keySet();
        }

        @SuppressWarnings("unchecked")
        public <R extends EntityRenderer<?>> R getSkin(PlayerSkin.Model skin) {
            return (R) skinMap.get(skin);
        }

        public Collection<EntityType<?>> getEntityTypes() {
            return renderers.keySet();
        }

        @SuppressWarnings("unchecked")
        public <T extends Entity, R extends EntityRenderer<T>> R getRenderer(EntityType<? extends T> entityType) {
            return (R) renderers.get(entityType);
        }

        public EntityRendererProvider.Context getContext() {
            return context;
        }
    }
}
