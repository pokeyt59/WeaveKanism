package mekanism.fabric_shim.fluids.crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Objects;
import java.util.stream.Stream;
import mekanism.fabric_shim.common.util.NeoForgeExtraCodecs;
import mekanism.fabric_shim.fluids.FluidStack;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.Nullable;

/**
 * A fluid ingredient plus a required amount in mB (stand-in for NeoForge's SizedFluidIngredient;
 * same surface and JSON formats — flat {@code {"fluid"/"tag"/"type": ..., "amount": n}} and nested
 * {@code {"ingredient": ..., "amount": n}}).
 */
public final class SizedFluidIngredient {

    //NeoForge FluidType.BUCKET_VOLUME; loader-neutral constant (mB per bucket)
    private static final int BUCKET_VOLUME = 1000;

    public static final Codec<SizedFluidIngredient> FLAT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
          FluidIngredient.MAP_CODEC_NONEMPTY.forGetter(SizedFluidIngredient::ingredient),
          NeoForgeExtraCodecs.optionalFieldAlwaysWrite(ExtraCodecs.POSITIVE_INT, "amount", BUCKET_VOLUME).forGetter(SizedFluidIngredient::amount))
          .apply(instance, SizedFluidIngredient::new));

    public static final Codec<SizedFluidIngredient> NESTED_CODEC = RecordCodecBuilder.create(instance -> instance.group(
          FluidIngredient.CODEC_NON_EMPTY.fieldOf("ingredient").forGetter(SizedFluidIngredient::ingredient),
          NeoForgeExtraCodecs.optionalFieldAlwaysWrite(ExtraCodecs.POSITIVE_INT, "amount", BUCKET_VOLUME).forGetter(SizedFluidIngredient::amount))
          .apply(instance, SizedFluidIngredient::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, SizedFluidIngredient> STREAM_CODEC = StreamCodec.composite(
          FluidIngredient.STREAM_CODEC,
          SizedFluidIngredient::ingredient,
          ByteBufCodecs.VAR_INT,
          SizedFluidIngredient::amount,
          SizedFluidIngredient::new);

    public static SizedFluidIngredient of(Fluid fluid, int amount) {
        return new SizedFluidIngredient(FluidIngredient.of(fluid), amount);
    }

    public static SizedFluidIngredient of(FluidStack stack) {
        return new SizedFluidIngredient(FluidIngredient.single(stack), stack.getAmount());
    }

    public static SizedFluidIngredient of(TagKey<Fluid> tag, int amount) {
        return new SizedFluidIngredient(FluidIngredient.tag(tag), amount);
    }

    private final FluidIngredient ingredient;
    private final int amount;
    @Nullable
    private FluidStack[] cachedStacks;

    public SizedFluidIngredient(FluidIngredient ingredient, int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Size must be positive");
        }
        this.ingredient = ingredient;
        this.amount = amount;
    }

    public FluidIngredient ingredient() {
        return ingredient;
    }

    public int amount() {
        return amount;
    }

    public boolean test(FluidStack stack) {
        return ingredient.test(stack) && stack.getAmount() >= amount;
    }

    public FluidStack[] getFluids() {
        if (cachedStacks == null) {
            cachedStacks = Stream.of(ingredient.getStacks())
                  .map(s -> s.copyWithAmount(amount))
                  .toArray(FluidStack[]::new);
        }
        return cachedStacks;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        return o instanceof SizedFluidIngredient other && amount == other.amount && ingredient.equals(other.ingredient);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ingredient, amount);
    }

    @Override
    public String toString() {
        return amount + "x " + ingredient;
    }
}
