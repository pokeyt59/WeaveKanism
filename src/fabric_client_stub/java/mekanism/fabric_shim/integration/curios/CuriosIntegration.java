package mekanism.fabric_shim.integration.curios;

import java.util.function.Predicate;
import mekanism.fabric_shim.items.IItemHandler;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import org.jetbrains.annotations.Nullable;

/**
 * Server-safe stand-in for {@code mekanism.common.integration.curios.CuriosIntegration} (Curios is a
 * NeoForge mod; the Fabric equivalent is Phase 5). Reports "no curios": empty stacks / null inventory,
 * so common charge/mode code compiles and simply finds nothing worn. The result-returning variant is
 * only used by the excluded client key handler, so it is omitted (it would need the Curios API type).
 */
public final class CuriosIntegration {

    private CuriosIntegration() {
    }

    public static void addListeners(IEventBus bus) {
    }

    @Nullable
    public static IItemHandler getCuriosInventory(LivingEntity entity) {
        return null;
    }

    public static ItemStack findFirstCurio(LivingEntity entity, Predicate<ItemStack> filter) {
        return ItemStack.EMPTY;
    }

    public static ItemStack getCurioStack(LivingEntity entity, String slotType, int slot) {
        return ItemStack.EMPTY;
    }
}
