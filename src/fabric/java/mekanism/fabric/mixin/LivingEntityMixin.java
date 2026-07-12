package mekanism.fabric.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import mekanism.fabric_shim.common.NeoForge;
import mekanism.fabric_shim.common.damagesource.DamageContainer;
import mekanism.fabric_shim.event.entity.living.LivingEvent;
import mekanism.fabric_shim.event.entity.living.LivingFallEvent;
import mekanism.fabric_shim.event.entity.living.LivingIncomingDamageEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Posts the living events NeoForge patches into vanilla and Fabric has no callbacks for:
 * incoming damage (cancellable + amount-modifiable — scuba mask / MekaSuit absorption), fall
 * damage (cancellable + distance-modifiable — free runners / hydraulic units), and jump
 * (MekaSuit hydraulic boost).
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    @WrapMethod(method = "hurt")
    private boolean mekanism$fireIncomingDamage(DamageSource source, float amount, Operation<Boolean> original) {
        LivingEntity self = (LivingEntity) (Object) this;
        //Match NeoForge's firing conditions: server side, actual damage, on a vulnerable, living target
        if (!self.level().isClientSide && amount > 0 && self.isAlive() && !self.isInvulnerableTo(source)) {
            LivingIncomingDamageEvent event = NeoForge.EVENT_BUS.post(new LivingIncomingDamageEvent(self, new DamageContainer(source, amount)));
            if (event.isCanceled()) {
                return false;
            }
            amount = event.getContainer().getNewDamage();
        }
        return original.call(source, amount);
    }

    @WrapMethod(method = "causeFallDamage")
    private boolean mekanism$fireLivingFall(float fallDistance, float damageMultiplier, DamageSource source, Operation<Boolean> original) {
        LivingEntity self = (LivingEntity) (Object) this;
        LivingFallEvent event = NeoForge.EVENT_BUS.post(new LivingFallEvent(self, fallDistance, damageMultiplier));
        if (event.isCanceled()) {
            return false;
        }
        return original.call(event.getDistance(), event.getDamageMultiplier(), source);
    }

    @Inject(method = "jumpFromGround", at = @At("TAIL"))
    private void mekanism$fireLivingJump(CallbackInfo ci) {
        NeoForge.EVENT_BUS.post(new LivingEvent.LivingJumpEvent((LivingEntity) (Object) this));
    }
}
