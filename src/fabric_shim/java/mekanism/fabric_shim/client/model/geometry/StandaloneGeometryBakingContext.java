package mekanism.fabric_shim.client.model.geometry;

import com.mojang.math.Transformation;
import mekanism.fabric_shim.client.RenderTypeGroup;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

/**
 * Same surface as net.neoforged.neoforge.client.model.geometry.StandaloneGeometryBakingContext — a
 * self-contained {@link IGeometryBakingContext} built via {@link #builder()}. Mekanism bakes models
 * outside the JSON pipeline with it (RenderTransmitterBase). Materials/render-types resolve to neutral
 * defaults (the port doesn't parse render-type hints on a standalone context). Fresh implementation.
 */
public class StandaloneGeometryBakingContext implements IGeometryBakingContext {

    private final ResourceLocation modelName;
    private final boolean gui3d;
    private final boolean useBlockLight;
    private final boolean useAmbientOcclusion;

    private StandaloneGeometryBakingContext(ResourceLocation modelName, boolean gui3d, boolean useBlockLight, boolean useAmbientOcclusion) {
        this.modelName = modelName;
        this.gui3d = gui3d;
        this.useBlockLight = useBlockLight;
        this.useAmbientOcclusion = useAmbientOcclusion;
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public String getModelName() {
        return modelName.toString();
    }

    @Override
    public boolean hasMaterial(String name) {
        return false;
    }

    @Override
    public Material getMaterial(String name) {
        throw new UnsupportedOperationException("StandaloneGeometryBakingContext has no materials: " + name);
    }

    @Override
    public boolean isGui3d() {
        return gui3d;
    }

    @Override
    public boolean useBlockLight() {
        return useBlockLight;
    }

    @Override
    public boolean useAmbientOcclusion() {
        return useAmbientOcclusion;
    }

    @Override
    public ItemTransforms getTransforms() {
        return ItemTransforms.NO_TRANSFORMS;
    }

    @Override
    public Transformation getRootTransform() {
        return Transformation.identity();
    }

    @Override
    public boolean isComponentVisible(String component, boolean fallback) {
        return fallback;
    }

    @Nullable
    @Override
    public ResourceLocation getRenderTypeHint() {
        return null;
    }

    @Override
    public RenderTypeGroup getRenderType(ResourceLocation name) {
        return RenderTypeGroup.EMPTY;
    }

    public static class Builder {

        private boolean gui3d = true;
        private boolean useBlockLight = true;
        private boolean useAmbientOcclusion = true;

        public Builder withGui3d(boolean gui3d) {
            this.gui3d = gui3d;
            return this;
        }

        public Builder withUseBlockLight(boolean useBlockLight) {
            this.useBlockLight = useBlockLight;
            return this;
        }

        public Builder withUseAmbientOcclusion(boolean useAmbientOcclusion) {
            this.useAmbientOcclusion = useAmbientOcclusion;
            return this;
        }

        public StandaloneGeometryBakingContext build(ResourceLocation modelName) {
            return new StandaloneGeometryBakingContext(modelName, gui3d, useBlockLight, useAmbientOcclusion);
        }
    }
}
