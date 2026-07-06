package mekanism.fabric_shim.common.crafting;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Arrays;
import java.util.function.Supplier;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPredicate;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

/**
 * Matches items carrying given data components (stand-in for NeoForge's DataComponentIngredient;
 * same surface and JSON format: {@code {"type": "neoforge:components", "items": ..., "components":
 * ..., "strict": bool}}).
 */
public final class DataComponentIngredient implements ICustomIngredient {

    public static final MapCodec<DataComponentIngredient> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
          RegistryCodecs.homogeneousList(Registries.ITEM).fieldOf("items").forGetter(DataComponentIngredient::items),
          DataComponentPredicate.CODEC.fieldOf("components").forGetter(DataComponentIngredient::components),
          com.mojang.serialization.Codec.BOOL.optionalFieldOf("strict", false).forGetter(DataComponentIngredient::isStrict))
          .apply(instance, DataComponentIngredient::new));

    public static final IngredientType<DataComponentIngredient> TYPE = new IngredientType<>(CODEC);

    private final HolderSet<net.minecraft.world.item.Item> items;
    private final DataComponentPredicate components;
    private final boolean strict;
    private final ItemStack[] stacks;

    public DataComponentIngredient(HolderSet<net.minecraft.world.item.Item> items, DataComponentPredicate components, boolean strict) {
        this.items = items;
        this.components = components;
        this.strict = strict;
        this.stacks = items.stream()
              .map(item -> {
                  ItemStack stack = new ItemStack(item);
                  stack.applyComponents(components.asPatch());
                  return stack;
              })
              .toArray(ItemStack[]::new);
    }

    public static Ingredient of(boolean strict, ItemStack stack) {
        return of(strict, DataComponentPredicate.allOf(stack.getComponents()), stack.getItemHolder());
    }

    public static <T> Ingredient of(boolean strict, DataComponentType<? super T> type, T value, ItemLike... items) {
        return of(strict, DataComponentPredicate.builder().expect(type, value).build(), items);
    }

    public static <T> Ingredient of(boolean strict, Supplier<? extends DataComponentType<? super T>> type, T value, ItemLike... items) {
        return of(strict, type.get(), value, items);
    }

    public static Ingredient of(boolean strict, DataComponentMap map, ItemLike... items) {
        return of(strict, DataComponentPredicate.allOf(map), items);
    }

    @SafeVarargs
    public static Ingredient of(boolean strict, DataComponentMap map, Holder<net.minecraft.world.item.Item>... items) {
        return of(strict, DataComponentPredicate.allOf(map), items);
    }

    public static Ingredient of(boolean strict, DataComponentMap map, HolderSet<net.minecraft.world.item.Item> items) {
        return of(strict, DataComponentPredicate.allOf(map), items);
    }

    @SafeVarargs
    public static Ingredient of(boolean strict, DataComponentPredicate predicate, Holder<net.minecraft.world.item.Item>... items) {
        return of(strict, predicate, HolderSet.direct(items));
    }

    public static Ingredient of(boolean strict, DataComponentPredicate predicate, ItemLike... items) {
        return of(strict, predicate, HolderSet.direct(Arrays.stream(items).map(item -> item.asItem().builtInRegistryHolder()).toList()));
    }

    public static Ingredient of(boolean strict, DataComponentPredicate predicate, HolderSet<net.minecraft.world.item.Item> items) {
        return new DataComponentIngredient(items, predicate, strict).toVanilla();
    }

    public HolderSet<net.minecraft.world.item.Item> items() {
        return items;
    }

    public DataComponentPredicate components() {
        return components;
    }

    public boolean isStrict() {
        return strict;
    }

    @Override
    public boolean test(ItemStack stack) {
        if (!items.contains(stack.getItemHolder())) {
            return false;
        }
        if (strict) {
            //Strict matching: the stack's component patch must be exactly the expected components
            return components.asPatch().equals(stack.getComponentsPatch());
        }
        return components.test(stack);
    }

    @Override
    public Stream<ItemStack> getItems() {
        return Stream.of(stacks).map(ItemStack::copy);
    }

    @Override
    public boolean isSimple() {
        return false;
    }

    @Override
    public IngredientType<?> getType() {
        return TYPE;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof DataComponentIngredient other && strict == other.strict
               && items.equals(other.items) && components.equals(other.components);
    }

    @Override
    public int hashCode() {
        int result = items.hashCode();
        result = 31 * result + components.hashCode();
        return 31 * result + Boolean.hashCode(strict);
    }
}
