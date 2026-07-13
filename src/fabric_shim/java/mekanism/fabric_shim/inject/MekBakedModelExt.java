package mekanism.fabric_shim.inject;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import mekanism.fabric_shim.client.ChunkRenderTypeSet;
import mekanism.fabric_shim.client.model.data.ModelData;
import mekanism.fabric_shim.common.util.TriState;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * NeoForge's IForgeBakedModel data-aware extensions, interface-injected onto vanilla {@link BakedModel}
 * (class_1087; fabric.mod.json loom:injected_interfaces) with the runtime target
 * {@code mekanism.fabric.mixin.BakedModelMixin}. Defaults forward to the vanilla model (ModelData /
 * renderType ignored, as NeoForge's do); Mekanism's own baked models override the slices they need. The
 * ModelData→render handoff runs through the FRAPI emit shim (client-models.md §5). Fresh implementation.
 */
public interface MekBakedModelExt {

    default List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand, ModelData data, @Nullable RenderType renderType) {
        return ((BakedModel) this).getQuads(state, side, rand);
    }

    default TriState useAmbientOcclusion(BlockState state, ModelData data, RenderType renderType) {
        return TriState.DEFAULT;
    }

    default ModelData getModelData(BlockAndTintGetter level, BlockPos pos, BlockState state, ModelData modelData) {
        return modelData;
    }

    default TextureAtlasSprite getParticleIcon(ModelData data) {
        return ((BakedModel) this).getParticleIcon();
    }

    default ChunkRenderTypeSet getRenderTypes(BlockState state, RandomSource rand, ModelData data) {
        return ChunkRenderTypeSet.of(ItemBlockRenderTypes.getChunkRenderType(state));
    }

    default List<RenderType> getRenderTypes(ItemStack stack, boolean fabulous) {
        return List.of(ItemBlockRenderTypes.getRenderType(stack, fabulous));
    }

    default List<BakedModel> getRenderPasses(ItemStack stack, boolean fabulous) {
        return List.of((BakedModel) this);
    }

    default BakedModel applyTransform(ItemDisplayContext displayContext, PoseStack poseStack, boolean applyLeftHandTransform) {
        ((BakedModel) this).getTransforms().getTransform(displayContext).apply(applyLeftHandTransform, poseStack);
        return (BakedModel) this;
    }
}
