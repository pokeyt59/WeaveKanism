package mekanism.fabric_shim.fluids.crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.stream.Stream;
import mekanism.fabric_shim.fluids.FluidStack;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.component.DataComponentPredicate;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.material.Fluid;

/**
 * Matches fluids carrying given data components (stand-in for NeoForge's
 * DataComponentFluidIngredient; same surface, JSON: {@code {"type": "neoforge:components",
 * "fluids": ..., "components": ..., "strict": bool}}).
 */
public final class DataComponentFluidIngredient extends FluidIngredient {

    public static final MapCodec<DataComponentFluidIngredient> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
          RegistryCodecs.homogeneousList(Registries.FLUID).fieldOf("fluids").forGetter(DataComponentFluidIngredient::fluids),
          DataComponentPredicate.CODEC.fieldOf("components").forGetter(DataComponentFluidIngredient::components),
          Codec.BOOL.optionalFieldOf("strict", false).forGetter(DataComponentFluidIngredient::isStrict))
          .apply(instance, DataComponentFluidIngredient::new));

    //NeoForge FluidType.BUCKET_VOLUME; loader-neutral constant (mB per bucket)
    private static final int BUCKET_VOLUME = 1000;

    private final HolderSet<Fluid> fluids;
    private final DataComponentPredicate components;
    private final boolean strict;
    private final FluidStack[] stacks;

    public DataComponentFluidIngredient(HolderSet<Fluid> fluids, DataComponentPredicate components, boolean strict) {
        this.fluids = fluids;
        this.components = components;
        this.strict = strict;
        this.stacks = fluids.stream()
              .map(fluid -> {
                  FluidStack stack = new FluidStack(fluid, BUCKET_VOLUME);
                  stack.applyComponents(components.asPatch());
                  return stack;
              })
              .toArray(FluidStack[]::new);
    }

    public static FluidIngredient of(boolean strict, FluidStack stack) {
        return of(strict, DataComponentPredicate.allOf(stack.getComponents()), stack.getFluid());
    }

    public static <T> FluidIngredient of(boolean strict, DataComponentType<? super T> type, T value, Fluid... fluids) {
        return of(strict, DataComponentPredicate.builder().expect(type, value).build(), fluids);
    }

    public static FluidIngredient of(boolean strict, DataComponentPredicate predicate, Fluid... fluids) {
        return of(strict, predicate, HolderSet.direct(Stream.of(fluids).map(Fluid::builtInRegistryHolder).toList()));
    }

    @SafeVarargs
    public static FluidIngredient of(boolean strict, DataComponentPredicate predicate, Holder<Fluid>... fluids) {
        return of(strict, predicate, HolderSet.direct(fluids));
    }

    public static FluidIngredient of(boolean strict, DataComponentPredicate predicate, HolderSet<Fluid> fluids) {
        return new DataComponentFluidIngredient(fluids, predicate, strict);
    }

    public HolderSet<Fluid> fluids() {
        return fluids;
    }

    public DataComponentPredicate components() {
        return components;
    }

    public boolean isStrict() {
        return strict;
    }

    @Override
    public boolean test(FluidStack stack) {
        if (!fluids.contains(stack.getFluidHolder())) {
            return false;
        }
        if (strict) {
            return components.asPatch().equals(stack.getComponentsPatch());
        }
        return components.test(stack);
    }

    @Override
    protected Stream<FluidStack> generateStacks() {
        return Stream.of(stacks);
    }

    @Override
    public boolean isSimple() {
        return false;
    }

    @Override
    public FluidIngredientType<?> getType() {
        return TYPE;
    }

    static final FluidIngredientType<DataComponentFluidIngredient> TYPE = new FluidIngredientType<>(CODEC);

    @Override
    public int hashCode() {
        int result = fluids.hashCode();
        result = 31 * result + components.hashCode();
        return 31 * result + Boolean.hashCode(strict);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof DataComponentFluidIngredient other && strict == other.strict
               && fluids.equals(other.fluids) && components.equals(other.components);
    }
}
