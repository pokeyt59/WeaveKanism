package mekanism.fabric_shim.common.crafting;

import java.util.stream.Stream;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.ApiStatus;

/**
 * Custom (non-vanilla) ingredient logic (stand-in for NeoForge's ICustomIngredient; same surface).
 * On NeoForge, vanilla Ingredient is patched to carry these; on this port {@link #toVanilla()}
 * bridges through Fabric API's custom-ingredient system, whose custom ingredients are themselves
 * vanilla Ingredient instances.
 */
public interface ICustomIngredient {

    boolean test(ItemStack stack);

    Stream<ItemStack> getItems();

    /**
     * @return true if matching depends only on the item (not count/components), allowing display/network shortcuts
     */
    boolean isSimple();

    IngredientType<?> getType();

    @ApiStatus.NonExtendable
    default Ingredient toVanilla() {
        return CustomIngredients.toVanilla(this);
    }
}
