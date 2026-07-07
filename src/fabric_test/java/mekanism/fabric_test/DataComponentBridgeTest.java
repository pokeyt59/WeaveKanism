package mekanism.fabric_test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.function.Supplier;
import net.minecraft.core.component.DataComponentHolder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Verifies the Step 3 data-component bridge END TO END at runtime: Loom interface injection makes
 * the Supplier overloads visible to javac, and the mekanism.mixins.json mixins make them real —
 * including the risky case, default-method dispatch through a receiver typed as the vanilla
 * DataComponentHolder INTERFACE (whose mixin adds a superinterface). If these tests fail after a
 * mixin/loader change, the ~370 upstream call sites compile but crash at runtime — fix the mixins,
 * do not touch the call sites.
 */
class DataComponentBridgeTest {

    private static final Supplier<DataComponentType<Integer>> DAMAGE = () -> DataComponents.DAMAGE;

    @BeforeAll
    static void bootstrap() {
        McBootstrap.ensure();
    }

    @Test
    @DisplayName("injected set/remove dispatch on ItemStack (class target)")
    void itemStackSetRemove() {
        ItemStack stack = new ItemStack(Items.IRON_PICKAXE);
        //a fresh tool already has DAMAGE=0 in its prototype, so set() reports 0 as the prior value
        assertEquals(0, stack.set(DAMAGE, 17));
        assertEquals(17, stack.getDamageValue());
        assertEquals(17, stack.remove(DAMAGE));
        assertEquals(0, stack.getDamageValue());
    }

    @Test
    @DisplayName("injected getOrDefault dispatches through the vanilla INTERFACE receiver")
    void interfaceReceiverDispatch() {
        ItemStack stack = new ItemStack(Items.IRON_PICKAXE);
        stack.set(DAMAGE, 5);
        //Receiver typed as the interface — resolves only if the mixin merged our superinterface
        DataComponentHolder holder = stack;
        assertEquals(5, holder.getOrDefault(DAMAGE, -1));
        assertEquals(5, holder.get(DAMAGE));
        assertTrue(holder.has(DAMAGE));
    }
}
