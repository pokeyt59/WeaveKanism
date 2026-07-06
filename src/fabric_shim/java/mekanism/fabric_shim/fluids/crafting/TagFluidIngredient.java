package mekanism.fabric_shim.fluids.crafting;

import com.mojang.serialization.MapCodec;
import java.util.stream.Stream;
import mekanism.fabric_shim.fluids.FluidStack;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;

/**
 * Matches all fluids in a tag (stand-in for NeoForge's TagFluidIngredient; same surface, JSON:
 * {@code {"tag": id}}).
 */
public class TagFluidIngredient extends FluidIngredient {

    public static final MapCodec<TagFluidIngredient> CODEC = TagKey.codec(Registries.FLUID)
          .xmap(TagFluidIngredient::new, TagFluidIngredient::tag).fieldOf("tag");

    //NeoForge FluidType.BUCKET_VOLUME; loader-neutral constant (mB per bucket)
    private static final int BUCKET_VOLUME = 1000;

    private final TagKey<Fluid> tag;

    public TagFluidIngredient(TagKey<Fluid> tag) {
        this.tag = tag;
    }

    public TagKey<Fluid> tag() {
        return tag;
    }

    @Override
    public boolean test(FluidStack fluidStack) {
        return fluidStack.is(tag);
    }

    @Override
    protected Stream<FluidStack> generateStacks() {
        return BuiltInRegistries.FLUID.getTag(tag).stream()
              .flatMap(named -> named.stream())
              .map(holder -> new FluidStack(holder, BUCKET_VOLUME));
    }

    @Override
    public boolean isSimple() {
        return true;
    }

    @Override
    public FluidIngredientType<?> getType() {
        return TYPE;
    }

    static final FluidIngredientType<TagFluidIngredient> TYPE = new FluidIngredientType<>(CODEC);

    @Override
    public int hashCode() {
        return tag.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof TagFluidIngredient other && tag.equals(other.tag);
    }
}
