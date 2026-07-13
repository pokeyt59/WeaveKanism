package mekanism.fabric_shim.client.model.geometry;

import java.util.function.Function;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;

/**
 * Same surface as net.neoforged.neoforge.client.model.geometry.IUnbakedGeometry: a model geometry that
 * bakes to a vanilla {@link BakedModel}. Mekanism's geometries (DataBasedGeometry, EnergyCubeGeometry,
 * TransmitterModel) implement it; the loader→ModelLoadingPlugin bridge invokes {@code bake()}
 * (client-models.md §4). Fresh implementation.
 */
public interface IUnbakedGeometry<T extends IUnbakedGeometry<T>> {

    BakedModel bake(IGeometryBakingContext context, ModelBaker baker, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState, ItemOverrides overrides);

    default void resolveParents(Function<ResourceLocation, UnbakedModel> modelGetter, IGeometryBakingContext context) {
    }
}
