package mekanism.fabric_shim.common.crafting;

import com.mojang.serialization.MapCodec;
import java.util.List;
import java.util.stream.Stream;
import mekanism.fabric_shim.common.util.NeoForgeExtraCodecs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

/**
 * Union of several ingredients (stand-in for NeoForge's CompoundIngredient; same surface and JSON
 * format: {@code {"type": "neoforge:compound", "children": [...]}}). A record, like upstream, so
 * callers can use record deconstruction patterns on it.
 */
public record CompoundIngredient(List<Ingredient> children) implements ICustomIngredient {

    public static final MapCodec<CompoundIngredient> CODEC =
          NeoForgeExtraCodecs.aliasedFieldOf(CustomIngredients.LIST_CODEC_NONEMPTY, "children", "ingredients")
                .xmap(CompoundIngredient::new, CompoundIngredient::children);

    public static final IngredientType<CompoundIngredient> TYPE = new IngredientType<>(CODEC);

    public CompoundIngredient {
        if (children.isEmpty()) {
            throw new IllegalArgumentException("Compound ingredient must have at least one child");
        }
        children = List.copyOf(children);
    }

    public static Ingredient of(Ingredient... children) {
        return of(List.of(children));
    }

    public static Ingredient of(List<Ingredient> children) {
        if (children.isEmpty()) {
            throw new IllegalArgumentException("Cannot create a compound ingredient with no children");
        }
        if (children.size() == 1) {
            return children.getFirst();
        }
        return new CompoundIngredient(children).toVanilla();
    }

    @Override
    public boolean test(ItemStack stack) {
        for (Ingredient child : children) {
            if (child.test(stack)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Stream<ItemStack> getItems() {
        return children.stream().flatMap(child -> Stream.of(child.getItems()));
    }

    @Override
    public boolean isSimple() {
        for (Ingredient child : children) {
            if (!CustomIngredients.isSimple(child)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public IngredientType<?> getType() {
        return TYPE;
    }
}
