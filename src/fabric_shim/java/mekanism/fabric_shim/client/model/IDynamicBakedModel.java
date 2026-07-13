package mekanism.fabric_shim.client.model;

import java.util.List;
import mekanism.fabric_shim.client.model.data.ModelData;
import mekanism.fabric_shim.inject.MekBakedModelExt;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Same surface as net.neoforged.neoforge.client.model.IDynamicBakedModel: a {@link BakedModel} whose
 * plain (deprecated) getQuads forwards to the ModelData-aware overload. Impls (EnergyCubeBakedModel)
 * supply {@code getQuads(state, side, rand, ModelData, RenderType)} and lean on the {@link MekBakedModelExt}
 * defaults for the rest. Fresh implementation.
 */
public interface IDynamicBakedModel extends BakedModel, MekBakedModelExt {

    @Override
    default List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand) {
        return getQuads(state, side, rand, ModelData.EMPTY, null);
    }
}
