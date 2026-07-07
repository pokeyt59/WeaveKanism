package mekanism.fabric_shim.common.damagesource;

import java.util.EnumMap;
import java.util.Map;
import mekanism.fabric_shim.event.entity.living.LivingShieldBlockEvent;
import net.minecraft.world.damagesource.DamageSource;

/**
 * Same surface (the slice Mekanism uses) as net.neoforged.neoforge.common.damagesource.DamageContainer.
 */
public class DamageContainer {

    public enum Reduction {
        ARMOR,
        ENCHANTMENTS,
        MOB_EFFECTS,
        ABSORPTION
    }

    @FunctionalInterface
    public interface IReductionFunction {

        float modify(DamageContainer container, float reductionIn);
    }

    private final DamageSource source;
    private final float originalDamage;
    private final Map<Reduction, Float> reductions = new EnumMap<>(Reduction.class);
    private float newDamage;
    private float blockedDamage;
    private float shieldDamage;
    private int postAttackInvulnerabilityTicks = 20;

    public DamageContainer(DamageSource source, float originalDamage) {
        this.source = source;
        this.originalDamage = originalDamage;
        this.newDamage = originalDamage;
    }

    public float getOriginalDamage() {
        return originalDamage;
    }

    public DamageSource getSource() {
        return source;
    }

    public void setNewDamage(float damage) {
        this.newDamage = damage;
    }

    public float getNewDamage() {
        return newDamage;
    }

    public float getBlockedDamage() {
        return blockedDamage;
    }

    public float getShieldDamage() {
        return shieldDamage;
    }

    public void setPostAttackInvulnerabilityTicks(int ticks) {
        this.postAttackInvulnerabilityTicks = ticks;
    }

    public int getPostAttackInvulnerabilityTicks() {
        return postAttackInvulnerabilityTicks;
    }

    public float getReduction(Reduction type) {
        return reductions.getOrDefault(type, 0F);
    }

    public void setReduction(Reduction reduction, float amount) {
        reductions.put(reduction, amount);
    }

    public void setBlockedDamage(LivingShieldBlockEvent event) {
        if (event.getBlocked()) {
            this.blockedDamage = event.getBlockedDamage();
            this.shieldDamage = event.shieldDamage();
            this.newDamage -= this.blockedDamage;
        }
    }
}
