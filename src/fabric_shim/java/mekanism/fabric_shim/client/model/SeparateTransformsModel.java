package mekanism.fabric_shim.client.model;

import com.google.common.collect.ImmutableMap;
import java.util.List;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Stand-in for NeoForge's {@code SeparateTransformsModel} (the {@code neoforge:separate_transforms}
 * item-model loader) — only the {@link Baked} slice Mekanism touches: ClientRegistration re-lights
 * the {@code baseModel}/{@code perspectives} sub-models reflectively (FieldReflectionHelper — the
 * FIELD NAMES are API surface here, keep them). Nothing constructs instances until the JSON loader
 * bridge lands (client-compile-plan.md step 4-loader): until then the {@code instanceof} in
 * ClientRegistration#lightBakedModel simply never matches. Fresh implementation.
 */
public class SeparateTransformsModel {

    public static class Baked implements BakedModel {

        private BakedModel baseModel;
        private ImmutableMap<ItemDisplayContext, BakedModel> perspectives;

        public Baked(BakedModel baseModel, ImmutableMap<ItemDisplayContext, BakedModel> perspectives) {
            this.baseModel = baseModel;
            this.perspectives = perspectives;
        }

        public BakedModel getPerspective(ItemDisplayContext displayContext) {
            return perspectives.getOrDefault(displayContext, baseModel);
        }

        @Override
        public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction direction, RandomSource random) {
            return baseModel.getQuads(state, direction, random);
        }

        @Override
        public boolean useAmbientOcclusion() {
            return baseModel.useAmbientOcclusion();
        }

        @Override
        public boolean isGui3d() {
            return baseModel.isGui3d();
        }

        @Override
        public boolean usesBlockLight() {
            return baseModel.usesBlockLight();
        }

        @Override
        public boolean isCustomRenderer() {
            return baseModel.isCustomRenderer();
        }

        @Override
        public TextureAtlasSprite getParticleIcon() {
            return baseModel.getParticleIcon();
        }

        @Override
        public ItemTransforms getTransforms() {
            return baseModel.getTransforms();
        }

        @Override
        public ItemOverrides getOverrides() {
            return baseModel.getOverrides();
        }
    }
}
