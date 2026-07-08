package mekanism.fabric_shim.inject;

/**
 * NeoForge patches vanilla {@code Ingredient} with {@code isSimple()} (whether it can be matched by a
 * plain item-set cache). Injected onto Ingredient + IngredientMixin; returns {@code true} — Mekanism's
 * recipe input caches use it only to pick a caching strategy, and vanilla/item ingredients are simple.
 */
public interface MekIngredientExt {

    default boolean isSimple() {
        return true;
    }
}
