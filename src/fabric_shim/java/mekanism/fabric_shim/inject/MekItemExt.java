package mekanism.fabric_shim.inject;

import mekanism.fabric_shim.common.ItemAbility;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.jetbrains.annotations.Nullable;

/**
 * NeoForge's {@code IItemExtension} surface (the slice Mekanism's gear items call via {@code super}),
 * injected onto vanilla {@code Item} + ItemMixin. Compile-only defaults: tool abilities / enchantment
 * gating / burn time are conservative (behavior arrives in Phase 4/5). See the hook-wiring checklist.
 */
public interface MekItemExt {

    default boolean canPerformAction(ItemStack stack, ItemAbility itemAbility) {
        return false;
    }

    default boolean isPrimaryItemFor(ItemStack stack, Holder<Enchantment> enchantment) {
        return false;
    }

    default boolean isBookEnchantable(ItemStack stack, ItemStack book) {
        return true;
    }

    default boolean supportsEnchantment(ItemStack stack, Holder<Enchantment> enchantment) {
        return true;
    }

    default int getEnchantmentLevel(ItemStack stack, Holder<Enchantment> enchantment) {
        return stack.getEnchantments().getLevel(enchantment);
    }

    default ItemEnchantments getAllEnchantments(ItemStack stack, HolderLookup.RegistryLookup<Enchantment> lookup) {
        return stack.getEnchantments();
    }

    default int getBurnTime(ItemStack stack, @Nullable RecipeType<?> recipeType) {
        return 0;
    }

    default boolean isNotReplaceableByPickAction(ItemStack stack, Player player, int inventorySlot) {
        return false;
    }

    @Nullable
    default FoodProperties getFoodProperties(ItemStack stack, @Nullable LivingEntity entity) {
        return stack.get(DataComponents.FOOD);
    }

    default String getCreatorModId(ItemStack itemStack) {
        return BuiltInRegistries.ITEM.getKey(itemStack.getItem()).getNamespace();
    }

    default boolean canEquip(ItemStack stack, EquipmentSlot armorType, LivingEntity entity) {
        return entity.getEquipmentSlotForItem(stack) == armorType;
    }

    default boolean hasCraftingRemainingItem(ItemStack stack) {
        return !getCraftingRemainingItem(stack).isEmpty();
    }

    default ItemStack getCraftingRemainingItem(ItemStack itemStack) {
        return ItemStack.EMPTY;
    }
}
