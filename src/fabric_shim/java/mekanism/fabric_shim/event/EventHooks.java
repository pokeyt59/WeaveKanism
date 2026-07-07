package mekanism.fabric_shim.event;

import mekanism.fabric_shim.common.NeoForge;
import mekanism.fabric_shim.common.util.BlockSnapshot;
import mekanism.fabric_shim.event.entity.EntityStruckByLightningEvent;
import mekanism.fabric_shim.event.entity.ProjectileImpactEvent;
import mekanism.fabric_shim.event.entity.player.PlayerEvent;
import mekanism.fabric_shim.event.level.BlockEvent;
import mekanism.fabric_shim.event.level.ExplosionEvent;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Nullable;

/**
 * Same surface (the slice Mekanism uses) as net.neoforged.neoforge.event.EventHooks. Return-value
 * conventions match NeoForge exactly (see each method). Vanilla-side firing is Phase 3 glue.
 */
public final class EventHooks {

    private EventHooks() {
    }

    /**
     * @return true if the placement was CANCELED (caller restores the snapshot).
     */
    public static boolean onBlockPlace(@Nullable Entity entity, BlockSnapshot blockSnapshot, Direction direction) {
        BlockEvent.EntityPlaceEvent event = new BlockEvent.EntityPlaceEvent(blockSnapshot,
              blockSnapshot.getLevel().getBlockState(blockSnapshot.getPos().relative(direction.getOpposite())), entity);
        NeoForge.EVENT_BUS.post(event);
        return event.isCanceled();
    }

    public static void firePlayerCraftingEvent(Player player, ItemStack crafted, Container craftMatrix) {
        NeoForge.EVENT_BUS.post(new PlayerEvent.ItemCraftedEvent(player, crafted, craftMatrix));
    }

    /**
     * @return true if the impact was CANCELED.
     */
    public static boolean onProjectileImpact(Projectile projectile, HitResult ray) {
        ProjectileImpactEvent event = new ProjectileImpactEvent(projectile, ray);
        NeoForge.EVENT_BUS.post(event);
        return event.isCanceled();
    }

    /**
     * @return true if the explosion should PROCEED (not canceled) — matches NeoForge.
     */
    public static boolean onExplosionStart(Level level, Explosion explosion) {
        ExplosionEvent.Start event = new ExplosionEvent.Start(level, explosion);
        NeoForge.EVENT_BUS.post(event);
        return !event.isCanceled();
    }

    /**
     * @return true if the strike should PROCEED (not canceled) — matches NeoForge.
     */
    public static boolean onEntityStruckByLightning(Entity entity, LightningBolt bolt) {
        EntityStruckByLightningEvent event = new EntityStruckByLightningEvent(entity, bolt);
        NeoForge.EVENT_BUS.post(event);
        return !event.isCanceled();
    }
}
