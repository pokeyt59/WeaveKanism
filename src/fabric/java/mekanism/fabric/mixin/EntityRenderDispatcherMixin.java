package mekanism.fabric.mixin;

import java.util.Map;
import mekanism.fabric_shim.client.event.EntityRenderersEvent;
import mekanism.fabric_shim.internal.ShimBuses;
import net.minecraft.client.gui.Font;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Posts {@link EntityRenderersEvent.AddLayers} after the entity renderers are (re)built — where
 * NeoForge fires it — with a Context assembled from the same dispatcher state vanilla's reload
 * builds its own from. Mekanism's handler decorates every LivingEntityRenderer with the mekasuit
 * armor/elytra layers.
 */
@Mixin(EntityRenderDispatcher.class)
public abstract class EntityRenderDispatcherMixin {

    @Shadow
    private Map<EntityType<?>, EntityRenderer<?>> renderers;
    @Shadow
    private Map<PlayerSkin.Model, EntityRenderer<? extends Player>> playerRenderers;
    @Shadow
    @Final
    private ItemRenderer itemRenderer;
    @Shadow
    @Final
    private BlockRenderDispatcher blockRenderDispatcher;
    @Shadow
    @Final
    private ItemInHandRenderer itemInHandRenderer;
    @Shadow
    @Final
    private Font font;
    @Shadow
    @Final
    private EntityModelSet entityModels;

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Inject(method = "onResourceManagerReload", at = @At("RETURN"))
    private void mek$postAddLayers(ResourceManager resourceManager, CallbackInfo ci) {
        EntityRendererProvider.Context context = new EntityRendererProvider.Context((EntityRenderDispatcher) (Object) this,
              itemRenderer, blockRenderDispatcher, itemInHandRenderer, resourceManager, entityModels, font);
        ShimBuses.MOD_BUS.post(new EntityRenderersEvent.AddLayers(renderers, (Map) playerRenderers, context));
    }
}
