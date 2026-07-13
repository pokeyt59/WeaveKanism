package mekanism.fabric_shim.client.model.geometry;

import com.mojang.math.Transformation;
import mekanism.fabric_shim.client.RenderTypeGroup;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

/**
 * Same surface as net.neoforged.neoforge.client.model.geometry.IGeometryBakingContext — the model-loader
 * bake config. Implemented by Mekanism's WrapperModelConfiguration / StandaloneGeometryBakingContext /
 * MekaSuitArmor and read by the geometry bake methods. Fresh implementation.
 */
public interface IGeometryBakingContext {

    String getModelName();

    boolean hasMaterial(String name);

    Material getMaterial(String name);

    boolean isGui3d();

    boolean useBlockLight();

    boolean useAmbientOcclusion();

    ItemTransforms getTransforms();

    Transformation getRootTransform();

    boolean isComponentVisible(String component, boolean fallback);

    @Nullable
    ResourceLocation getRenderTypeHint();

    //Default (NeoForge resolves via NamedRenderTypeManager); the port registers no named render types,
    // so every lookup is the empty group. Impls that carry render types (WrapperModelConfiguration) override.
    default RenderTypeGroup getRenderType(ResourceLocation name) {
        return RenderTypeGroup.EMPTY;
    }
}
