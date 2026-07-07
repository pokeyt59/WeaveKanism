package mekanism.fabric_shim.event.entity;

import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.HitResult;
import net.neoforged.bus.api.ICancellableEvent;

/**
 * Same surface as net.neoforged.neoforge.event.entity.ProjectileImpactEvent.
 */
public class ProjectileImpactEvent extends EntityEvent implements ICancellableEvent {

    private final HitResult ray;

    public ProjectileImpactEvent(Projectile projectile, HitResult ray) {
        super(projectile);
        this.ray = ray;
    }

    @Override
    public Projectile getEntity() {
        return (Projectile) super.getEntity();
    }

    public Projectile getProjectile() {
        return getEntity();
    }

    public HitResult getRayTraceResult() {
        return ray;
    }
}
