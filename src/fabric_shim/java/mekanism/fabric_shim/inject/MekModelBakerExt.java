package mekanism.fabric_shim.inject;

import java.util.function.Function;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

/**
 * NeoForge's ModelBaker patch surface (the slice Mekanism's model cache uses), attached to the
 * vanilla {@link ModelBaker} interface via Loom interface injection + the ModelBakerMixin
 * interface mixin.
 */
public interface MekModelBakerExt {

    private ModelBaker self() {
        return (ModelBaker) this;
    }

    /**
     * NeoForge overload taking an explicit sprite getter. Mekanism only calls it on bakers it
     * constructed with the same {@code Material::sprite} resolver, so delegating to the vanilla
     * two-arg form is identity behavior.
     */
    default BakedModel bake(ResourceLocation rl, ModelState state, Function<Material, TextureAtlasSprite> spriteGetter) {
        return self().bake(rl, state);
    }

    /**
     * NeoForge's top-level (variant) model lookup. Returning null routes BaseModelCache's
     * getUnbakedModel to its {@code ModelBakery#getModel} fallback — which is where the vanilla
     * pipeline this port bakes through keeps unbaked models.
     */
    @Nullable
    default UnbakedModel getTopLevelModel(ModelResourceLocation mrl) {
        return null;
    }
}
