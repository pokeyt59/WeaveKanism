package mekanism.fabric_shim.inject;

/**
 * NeoForge patches vanilla {@code Ingredient} with {@code isSimple()} (whether it can be matched by a
 * plain item-set cache). Injected onto Ingredient + IngredientMixin; delegates to the shim
 * custom-ingredient bridge so compound/data-component ingredients report their real simplicity.
 */
public interface MekIngredientExt {

    default boolean isSimple() {
        return mekanism.fabric_shim.common.crafting.CustomIngredients.isSimple((net.minecraft.world.item.crafting.Ingredient) this);
    }
}
