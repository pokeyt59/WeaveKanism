package mekanism.fabric_shim.event.entity.living;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.ICancellableEvent;

/**
 * Stand-in for NeoForge's {@code LivingDeathEvent}. Compile-only; firing (off Fabric's
 * {@code ServerLivingEntityEvents.ALLOW_DEATH}) is Phase 3.
 */
public class LivingDeathEvent extends LivingEvent implements ICancellableEvent {

    private final DamageSource source;

    public LivingDeathEvent(LivingEntity entity, DamageSource source) {
        super(entity);
        this.source = source;
    }

    public DamageSource getSource() {
        return this.source;
    }
}
