package mekanism.fabric.mixin;

import mekanism.fabric_shim.inject.MekModelManagerExt;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.util.profiling.ProfilerFiller;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Implements {@link MekModelManagerExt}: NeoForge retains the ModelBakery on ModelManager after
 * each reload; vanilla only threads it through the reload state, so capture it there
 * (ReloadState is AW'd accessible).
 */
@Mixin(ModelManager.class)
public abstract class ModelManagerMixin implements MekModelManagerExt {

    @Unique
    private ModelBakery mek$modelBakery;

    @Inject(method = "apply", at = @At("HEAD"))
    private void mek$captureBakery(ModelManager.ReloadState reloadState, ProfilerFiller profiler, CallbackInfo ci) {
        this.mek$modelBakery = reloadState.modelBakery();
    }

    @Override
    public ModelBakery getModelBakery() {
        return this.mek$modelBakery;
    }
}
