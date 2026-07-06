package mekanism.fabric_shim.fluids.crafting;

import com.mojang.serialization.MapCodec;
import java.util.stream.Stream;
import mekanism.fabric_shim.fluids.FluidStack;
import net.minecraft.core.Holder;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

/**
 * Matches a single fluid (stand-in for NeoForge's SingleFluidIngredient; same surface, JSON:
 * {@code {"fluid": id}}).
 */
public class SingleFluidIngredient extends FluidIngredient {

    public static final MapCodec<SingleFluidIngredient> CODEC = FluidStack.FLUID_NON_EMPTY_CODEC
          .xmap(SingleFluidIngredient::new, SingleFluidIngredient::fluid).fieldOf("fluid");

    //NeoForge FluidType.BUCKET_VOLUME; loader-neutral constant (mB per bucket)
    private static final int BUCKET_VOLUME = 1000;

    private final Holder<Fluid> fluid;

    public SingleFluidIngredient(Holder<Fluid> fluid) {
        if (fluid.is(Fluids.EMPTY.builtInRegistryHolder())) {
            throw new IllegalStateException("SingleFluidIngredient must not be constructed with minecraft:empty, use FluidIngredient.empty() instead!");
        }
        this.fluid = fluid;
    }

    public Holder<Fluid> fluid() {
        return fluid;
    }

    @Override
    public boolean test(FluidStack fluidStack) {
        return fluidStack.is(fluid);
    }

    @Override
    protected Stream<FluidStack> generateStacks() {
        return Stream.of(new FluidStack(fluid, BUCKET_VOLUME));
    }

    @Override
    public boolean isSimple() {
        return true;
    }

    @Override
    public FluidIngredientType<?> getType() {
        return TYPE;
    }

    static final FluidIngredientType<SingleFluidIngredient> TYPE = new FluidIngredientType<>(CODEC);

    @Override
    public int hashCode() {
        return fluid.value().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof SingleFluidIngredient other && other.fluid.value() == fluid.value();
    }
}
