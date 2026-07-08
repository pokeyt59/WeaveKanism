package mekanism.fabric.mixin;

import mekanism.fabric_shim.inject.MekIngredientExt;
import net.minecraft.world.item.crafting.Ingredient;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Ingredient.class)
public abstract class IngredientMixin implements MekIngredientExt {
}
