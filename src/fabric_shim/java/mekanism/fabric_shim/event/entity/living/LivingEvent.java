package mekanism.fabric_shim.event.entity.living;

import mekanism.fabric_shim.event.entity.EntityEvent;
import net.minecraft.world.entity.LivingEntity;

/**
 * Same surface as net.neoforged.neoforge.event.entity.living.LivingEvent.
 */
public abstract class LivingEvent extends EntityEvent {

    public LivingEvent(LivingEntity entity) {
        super(entity);
    }

    @Override
    public LivingEntity getEntity() {
        return (LivingEntity) super.getEntity();
    }

    public static class LivingJumpEvent extends LivingEvent {

        public LivingJumpEvent(LivingEntity entity) {
            super(entity);
        }
    }
}
