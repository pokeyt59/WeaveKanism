package mekanism.fabric_shim.common.crafting;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.stream.Stream;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

/**
 * Matches the base ingredient minus the subtracted ingredient (stand-in for NeoForge's
 * DifferenceIngredient; same surface and JSON format:
 * {@code {"type": "neoforge:difference", "base": ..., "subtracted": ...}}).
 */
public final class DifferenceIngredient implements ICustomIngredient {

    public static final MapCodec<DifferenceIngredient> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
          CustomIngredients.INGREDIENT_CODEC_NONEMPTY.fieldOf("base").forGetter(DifferenceIngredient::base),
          CustomIngredients.INGREDIENT_CODEC_NONEMPTY.fieldOf("subtracted").forGetter(DifferenceIngredient::subtracted))
          .apply(instance, DifferenceIngredient::new));

    public static final IngredientType<DifferenceIngredient> TYPE = new IngredientType<>(CODEC);

    private final Ingredient base;
    private final Ingredient subtracted;

    public DifferenceIngredient(Ingredient base, Ingredient subtracted) {
        this.base = base;
        this.subtracted = subtracted;
    }

    public static Ingredient of(Ingredient base, Ingredient subtracted) {
        return new DifferenceIngredient(base, subtracted).toVanilla();
    }

    public Ingredient base() {
        return base;
    }

    public Ingredient subtracted() {
        return subtracted;
    }

    @Override
    public boolean test(ItemStack stack) {
        return base.test(stack) && !subtracted.test(stack);
    }

    @Override
    public Stream<ItemStack> getItems() {
        return Stream.of(base.getItems()).filter(stack -> !subtracted.test(stack));
    }

    @Override
    public boolean isSimple() {
        return CustomIngredients.isSimple(base) && CustomIngredients.isSimple(subtracted);
    }

    @Override
    public IngredientType<?> getType() {
        return TYPE;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof DifferenceIngredient other && base.equals(other.base) && subtracted.equals(other.subtracted);
    }

    @Override
    public int hashCode() {
        return 31 * base.hashCode() + subtracted.hashCode();
    }
}
