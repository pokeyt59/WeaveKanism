package mekanism.fabric_shim.client.model.obj;

import java.util.Set;
import java.util.function.Function;
import mekanism.fabric_shim.client.model.geometry.IGeometryBakingContext;
import mekanism.fabric_shim.client.model.geometry.IUnbakedGeometry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

/**
 * Stub for net.neoforged.neoforge.client.model.obj.ObjModel. OBJ parsing is deferred — Porting Lib was
 * declined (client-models.md §7 decision A) and transmitters, the only OBJ users, are outside the Phase 4
 * milestone: {@code bake()} returns the missing model and {@code getRootComponentNames()} is empty, so
 * transmitters render as the missing model. Replace with a real OBJ loader in a later phase (PORTING.md
 * deviation).
 */
public class ObjModel implements IUnbakedGeometry<ObjModel> {

    @Override
    public BakedModel bake(IGeometryBakingContext context, ModelBaker baker, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState, ItemOverrides overrides) {
        return Minecraft.getInstance().getModelManager().getMissingModel();
    }

    public Set<String> getRootComponentNames() {
        return Set.of();
    }

    public record ModelSettings(ResourceLocation modelLocation, boolean automaticCulling, boolean shadeQuads, boolean flipV, boolean emissiveAmbient, @Nullable String mtlOverride) {
    }
}
