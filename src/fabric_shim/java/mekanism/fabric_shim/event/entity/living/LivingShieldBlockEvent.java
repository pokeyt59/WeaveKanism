package mekanism.fabric_shim.event.entity.living;

import mekanism.fabric_shim.common.damagesource.DamageContainer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.ICancellableEvent;

/**
 * Same surface as net.neoforged.neoforge.event.entity.living.LivingShieldBlockEvent.
 */
public class LivingShieldBlockEvent extends LivingEvent implements ICancellableEvent {

    private final DamageContainer container;
    private final boolean originalBlocked;
    private float shieldDamage = -1;
    private float dmgBlocked;
    private boolean newBlocked;

    public LivingShieldBlockEvent(LivingEntity blocker, DamageContainer container, boolean originalBlockedState) {
        super(blocker);
        this.container = container;
        this.originalBlocked = originalBlockedState;
        this.newBlocked = originalBlockedState;
        this.dmgBlocked = container.getNewDamage();
    }

    public DamageContainer getDamageContainer() {
        return container;
    }

    public DamageSource getDamageSource() {
        return container.getSource();
    }

    public float getOriginalBlockedDamage() {
        return container.getNewDamage();
    }

    public float getBlockedDamage() {
        return Math.min(dmgBlocked, container.getNewDamage());
    }

    public float shieldDamage() {
        return shieldDamage >= 0 ? shieldDamage : getBlockedDamage();
    }

    public void setBlockedDamage(float blocked) {
        this.dmgBlocked = Math.clamp(blocked, 0, container.getNewDamage());
    }

    public void setShieldDamage(float damage) {
        this.shieldDamage = damage;
    }

    public boolean getOriginalBlock() {
        return originalBlocked;
    }

    public boolean getBlocked() {
        return newBlocked;
    }

    public void setBlocked(boolean isBlocked) {
        this.newBlocked = isBlocked;
    }
}
