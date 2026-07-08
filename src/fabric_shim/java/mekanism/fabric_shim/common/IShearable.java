package mekanism.fabric_shim.common;

import java.util.Collections;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/**
 * Stand-in for NeoForge's {@code IShearable}. Mekanism only uses it as an {@code instanceof} marker
 * (the shearing module). Default methods mirror NeoForge's surface; real shear behavior for Fabric
 * entities is Phase 3/4.
 */
public interface IShearable {

    default boolean isShearable(@Nullable Player player, ItemStack item, Level level, BlockPos pos) {
        return true;
    }

    default List<ItemStack> onSheared(@Nullable Player player, ItemStack item, Level level, BlockPos pos) {
        return Collections.emptyList();
    }

    default void spawnShearedDrop(Level level, BlockPos pos, ItemStack drop) {
    }
}
