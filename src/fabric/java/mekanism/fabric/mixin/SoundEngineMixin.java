package mekanism.fabric.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import mekanism.fabric_shim.client.ClientHooks;
import mekanism.fabric_shim.client.event.sound.SoundEngineLoadEvent;
import mekanism.fabric_shim.internal.ShimBuses;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * NeoForge's two SoundEngine hooks: every play() runs through the PlaySoundEvent pipeline
 * (via {@link ClientHooks#playSound} — null result suppresses, a swapped instance plays instead;
 * Mekanism's muffling listener wraps machine sounds here), and {@code SoundEngineLoadEvent} posts
 * on the mod bus after the library (re)loads.
 */
@Mixin(SoundEngine.class)
public abstract class SoundEngineMixin {

    @WrapMethod(method = "play")
    private void mek$wrapPlay(SoundInstance sound, Operation<Void> original) {
        SoundInstance result = ClientHooks.playSound((SoundEngine) (Object) this, sound);
        if (result != null) {
            original.call(result);
        }
    }

    @Inject(method = "loadLibrary", at = @At("RETURN"))
    private void mek$postEngineLoad(CallbackInfo ci) {
        ShimBuses.MOD_BUS.post(new SoundEngineLoadEvent((SoundEngine) (Object) this));
    }
}
