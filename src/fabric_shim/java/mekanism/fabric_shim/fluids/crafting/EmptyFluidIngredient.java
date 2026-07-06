package mekanism.fabric_shim.fluids.crafting;

import com.mojang.serialization.MapCodec;
import java.util.stream.Stream;
import mekanism.fabric_shim.fluids.FluidStack;

/**
 * The empty fluid ingredient (stand-in for NeoForge's EmptyFluidIngredient; same surface).
 */
public final class EmptyFluidIngredient extends FluidIngredient {

    public static final EmptyFluidIngredient INSTANCE = new EmptyFluidIngredient();

    public static final MapCodec<EmptyFluidIngredient> CODEC = MapCodec.unit(INSTANCE);

    private EmptyFluidIngredient() {
    }

    @Override
    public boolean test(FluidStack fluidStack) {
        return fluidStack.isEmpty();
    }

    @Override
    protected Stream<FluidStack> generateStacks() {
        return Stream.empty();
    }

    @Override
    public boolean isSimple() {
        return true;
    }

    @Override
    public FluidIngredientType<?> getType() {
        return TYPE;
    }

    static final FluidIngredientType<EmptyFluidIngredient> TYPE = new FluidIngredientType<>(CODEC);

    @Override
    public int hashCode() {
        return System.identityHashCode(this);
    }

    @Override
    public boolean equals(Object obj) {
        return obj == this;
    }
}
