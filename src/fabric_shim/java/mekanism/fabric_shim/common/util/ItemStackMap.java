package mekanism.fabric_shim.common.util;

import it.unimi.dsi.fastutil.Hash.Strategy;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;
import java.util.Map;
import net.minecraft.world.item.ItemStack;

/**
 * Same surface (the slice Mekanism uses) as net.neoforged.neoforge.common.util.ItemStackMap:
 * hash maps keyed by item + components, ignoring count.
 */
public final class ItemStackMap {

    private static final Strategy<ItemStack> TYPE_AND_TAG = new Strategy<>() {
        @Override
        public int hashCode(ItemStack stack) {
            return stack == null || stack.isEmpty() ? 0 : ItemStack.hashItemAndComponents(stack);
        }

        @Override
        public boolean equals(ItemStack a, ItemStack b) {
            if (a == b) {
                return true;
            }
            if (a == null || b == null) {
                return false;
            }
            return a.isEmpty() == b.isEmpty() && ItemStack.isSameItemSameComponents(a, b);
        }
    };

    private ItemStackMap() {
    }

    public static <V> Map<ItemStack, V> createTypeAndTagMap() {
        return new Object2ObjectOpenCustomHashMap<>(TYPE_AND_TAG);
    }
}
