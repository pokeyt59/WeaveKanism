package mekanism.fabric_shim.inject;

import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import mekanism.fabric_shim.common.ItemAbility;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
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
 * NeoForge's ItemStack data-component patch surface (Supplier overloads), attached to
 * {@link ItemStack} via Loom interface injection + ItemStackMixin.
 */
public interface MekItemStackExt {

    private ItemStack self() {
        return (ItemStack) this;
    }

    @Nullable
    default <T> T set(Supplier<? extends DataComponentType<T>> type, @Nullable T value) {
        return self().set(type.get(), value);
    }

    @Nullable
    default <T> T remove(Supplier<? extends DataComponentType<? extends T>> type) {
        return self().remove(type.get());
    }

    @Nullable
    default <T> T update(Supplier<? extends DataComponentType<T>> type, T defaultValue, UnaryOperator<T> updater) {
        return self().update(type.get(), defaultValue, updater);
    }

    //NeoForge IItemStackExtension slice (1-arg convenience forms). Conservative compile-only defaults,
    //matching MekItemExt; tool-ability / enchantment behavior is Phase 4/5.
    default boolean canPerformAction(ItemAbility itemAbility) {
        return false;
    }

    default boolean isPrimaryItemFor(Holder<Enchantment> enchantment) {
        return false;
    }

    default boolean isBookEnchantable(ItemStack book) {
        return true;
    }

    default boolean supportsEnchantment(Holder<Enchantment> enchantment) {
        return true;
    }

    default int getEnchantmentLevel(Holder<Enchantment> enchantment) {
        return self().getEnchantments().getLevel(enchantment);
    }

    default ItemEnchantments getAllEnchantments(HolderLookup.RegistryLookup<Enchantment> lookup) {
        return self().getEnchantments();
    }

    default int getBurnTime(@Nullable RecipeType<?> recipeType) {
        return 0;
    }

    default boolean isNotReplaceableByPickAction(Player player, int inventorySlot) {
        return false;
    }

    @Nullable
    default FoodProperties getFoodProperties(@Nullable LivingEntity entity) {
        return self().get(DataComponents.FOOD);
    }

    default boolean canEquip(EquipmentSlot armorType, LivingEntity entity) {
        return entity.getEquipmentSlotForItem(self()) == armorType;
    }

    default boolean hasCraftingRemainingItem() {
        return !getCraftingRemainingItem().isEmpty();
    }

    default ItemStack getCraftingRemainingItem() {
        return ItemStack.EMPTY;
    }

    default boolean isComponentsPatchEmpty() {
        return self().getComponentsPatch().isEmpty();
    }
}
