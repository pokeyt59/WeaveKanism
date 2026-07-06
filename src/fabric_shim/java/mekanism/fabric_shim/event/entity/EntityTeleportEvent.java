package mekanism.fabric_shim.event.entity;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.ICancellableEvent;

/**
 * Fired before an entity teleports; cancellable, target mutable (stand-in for NeoForge's
 * EntityTeleportEvent; same surface). Posted on the game bus by the shim event glue (Phase 3);
 * Mekanism's own MekanismTeleportEvent extends this.
 */
public class EntityTeleportEvent extends EntityEvent implements ICancellableEvent {

    protected double targetX;
    protected double targetY;
    protected double targetZ;

    public EntityTeleportEvent(Entity entity, double targetX, double targetY, double targetZ) {
        super(entity);
        this.targetX = targetX;
        this.targetY = targetY;
        this.targetZ = targetZ;
    }

    public double getTargetX() {
        return targetX;
    }

    public void setTargetX(double targetX) {
        this.targetX = targetX;
    }

    public double getTargetY() {
        return targetY;
    }

    public void setTargetY(double targetY) {
        this.targetY = targetY;
    }

    public double getTargetZ() {
        return targetZ;
    }

    public void setTargetZ(double targetZ) {
        this.targetZ = targetZ;
    }

    public Vec3 getTarget() {
        return new Vec3(this.targetX, this.targetY, this.targetZ);
    }

    public double getPrevX() {
        return getEntity().getX();
    }

    public double getPrevY() {
        return getEntity().getY();
    }

    public double getPrevZ() {
        return getEntity().getZ();
    }

    public Vec3 getPrev() {
        return getEntity().position();
    }
}
