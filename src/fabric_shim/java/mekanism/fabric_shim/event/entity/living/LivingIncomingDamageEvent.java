package mekanism.fabric_shim.event.entity.living;

import mekanism.fabric_shim.common.damagesource.DamageContainer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.ICancellableEvent;

/**
 * Stand-in for NeoForge's {@code LivingIncomingDamageEvent}. Carries the {@link DamageContainer} the
 * handler mutates. Compile-only; firing is Phase 3.
 */
public class LivingIncomingDamageEvent extends LivingEvent implements ICancellableEvent {

    private final DamageContainer container;

    public LivingIncomingDamageEvent(LivingEntity entity, DamageContainer container) {
        super(entity);
        this.container = container;
    }

    public DamageContainer getContainer() {
        return this.container;
    }

    public DamageSource getSource() {
        return this.container.getSource();
    }

    public float getAmount() {
        return this.container.getNewDamage();
    }

    public void setAmount(float newDamage) {
        this.container.setNewDamage(newDamage);
    }
}
