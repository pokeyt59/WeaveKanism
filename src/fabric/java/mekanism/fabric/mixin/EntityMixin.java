package mekanism.fabric.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import mekanism.fabric_shim.common.NeoForge;
import mekanism.fabric_shim.event.entity.EntityInvulnerabilityCheckEvent;
import mekanism.fabric_shim.inject.MekAttachmentExt;
import mekanism.fabric_shim.inject.MekEntityExt;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Entity.class)
public abstract class EntityMixin implements MekEntityExt, MekAttachmentExt {

    /**
     * NeoForge's invulnerability-check hook: listeners see (and may override) the vanilla result —
     * Mekanism grants radiation immunity to tagged entity types through it.
     */
    @WrapMethod(method = "isInvulnerableTo")
    private boolean mekanism$fireInvulnerabilityCheck(DamageSource source, Operation<Boolean> original) {
        boolean vanilla = original.call(source);
        Entity self = (Entity) (Object) this;
        if (self.level().isClientSide) {
            return vanilla;
        }
        return NeoForge.EVENT_BUS.post(new EntityInvulnerabilityCheckEvent(self, source, vanilla)).isInvulnerable();
    }
}
