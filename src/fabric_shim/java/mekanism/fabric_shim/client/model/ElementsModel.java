package mekanism.fabric_shim.client.model;

import java.util.List;
import java.util.function.Function;
import mekanism.fabric_shim.client.model.geometry.IGeometryBakingContext;
import mekanism.fabric_shim.client.model.geometry.IUnbakedGeometry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BlockElement;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;

/**
 * Stub for net.neoforged.neoforge.client.model.ElementsModel (a vanilla block-elements geometry).
 * RobitModel extends it; the robit entity is outside the Phase 4 milestone, so {@code bake()} returns
 * the missing model (robit renders missing) until faithful element baking lands (PORTING.md deviation).
 * The parsed {@link BlockElement} list is retained so a real implementation can bake it later.
 */
public class ElementsModel implements IUnbakedGeometry<ElementsModel> {

    protected final List<BlockElement> elements;

    public ElementsModel(List<BlockElement> elements) {
        this.elements = elements;
    }

    @Override
    public BakedModel bake(IGeometryBakingContext context, ModelBaker baker, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState, ItemOverrides overrides) {
        return Minecraft.getInstance().getModelManager().getMissingModel();
    }
}
