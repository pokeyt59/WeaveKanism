package mekanism.fabric_shim.fluids.crafting;

import com.mojang.serialization.MapCodec;
import java.util.List;
import java.util.stream.Stream;
import mekanism.fabric_shim.common.util.NeoForgeExtraCodecs;
import mekanism.fabric_shim.fluids.FluidStack;

/**
 * Union of several fluid ingredients (stand-in for NeoForge's CompoundFluidIngredient; same
 * surface, JSON: {@code {"type": "neoforge:compound", "children": [...]}}).
 */
public final class CompoundFluidIngredient extends FluidIngredient {

    public static final MapCodec<CompoundFluidIngredient> CODEC =
          NeoForgeExtraCodecs.aliasedFieldOf(FluidIngredient.LIST_CODEC_NON_EMPTY, "children", "ingredients")
                .xmap(CompoundFluidIngredient::new, CompoundFluidIngredient::children);

    private final List<FluidIngredient> children;

    public CompoundFluidIngredient(List<? extends FluidIngredient> children) {
        if (children.isEmpty()) {
            throw new IllegalArgumentException("Compound fluid ingredient must have at least one child");
        }
        this.children = List.copyOf(children);
    }

    public static FluidIngredient of(FluidIngredient... children) {
        if (children.length == 0) {
            return FluidIngredient.empty();
        }
        if (children.length == 1) {
            return children[0];
        }
        return new CompoundFluidIngredient(List.of(children));
    }

    public static FluidIngredient of(List<FluidIngredient> children) {
        if (children.isEmpty()) {
            return FluidIngredient.empty();
        }
        if (children.size() == 1) {
            return children.getFirst();
        }
        return new CompoundFluidIngredient(children);
    }

    public static FluidIngredient of(Stream<FluidIngredient> stream) {
        return of(stream.toList());
    }

    public List<FluidIngredient> children() {
        return children;
    }

    @Override
    public boolean test(FluidStack stack) {
        for (FluidIngredient child : children) {
            if (child.test(stack)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected Stream<FluidStack> generateStacks() {
        return children.stream().flatMap(FluidIngredient::generateStacks);
    }

    @Override
    public boolean isSimple() {
        for (FluidIngredient child : children) {
            if (!child.isSimple()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public FluidIngredientType<?> getType() {
        return TYPE;
    }

    static final FluidIngredientType<CompoundFluidIngredient> TYPE = new FluidIngredientType<>(CODEC);

    @Override
    public int hashCode() {
        return children.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof CompoundFluidIngredient other && children.equals(other.children);
    }
}
