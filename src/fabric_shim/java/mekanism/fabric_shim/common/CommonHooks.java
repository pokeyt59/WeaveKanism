package mekanism.fabric_shim.common;

import mekanism.fabric_shim.common.damagesource.DamageContainer;
import mekanism.fabric_shim.event.entity.living.LivingShieldBlockEvent;
import mekanism.fabric_shim.event.level.BlockEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Same surface (the slice Mekanism uses) as net.neoforged.neoforge.common.CommonHooks. Events are
 * posted on the shim game bus, so Mekanism's own listeners see them; VANILLA-side firing of these
 * events (so Mekanism sees other actors) is Phase 3 glue — see the hook-wiring checklist.
 */
public final class CommonHooks {

    private static final ThreadLocal<Player> CRAFTING_PLAYER = new ThreadLocal<>();

    private CommonHooks() {
    }

    public static void setCraftingPlayer(@Nullable Player player) {
        CRAFTING_PLAYER.set(player);
    }

    @Nullable
    public static Player getCraftingPlayer() {
        return CRAFTING_PLAYER.get();
    }

    public static BlockEvent.BreakEvent fireBlockBreak(Level level, GameType gameType, ServerPlayer player, BlockPos pos, BlockState state) {
        BlockEvent.BreakEvent event = new BlockEvent.BreakEvent(level, pos, state, player);
        NeoForge.EVENT_BUS.post(event);
        return event;
    }

    public static LivingShieldBlockEvent onDamageBlock(LivingEntity blocker, DamageContainer container, boolean originalBlocked) {
        LivingShieldBlockEvent event = new LivingShieldBlockEvent(blocker, container, originalBlocked);
        NeoForge.EVENT_BUS.post(event);
        return event;
    }

    /**
     * NeoForge dispenser shears behavior; not replicated on Fabric — returning false falls back to
     * vanilla dispenser handling (hook-wiring checklist).
     */
    public static boolean tryDispenseShearsHarvestBlock(BlockSource source, ItemStack stack, ServerLevel level, BlockPos pos) {
        return false;
    }
}
