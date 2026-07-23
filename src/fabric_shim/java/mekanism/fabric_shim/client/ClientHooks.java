package mekanism.fabric_shim.client;

import com.mojang.blaze3d.vertex.PoseStack;
import mekanism.fabric_shim.client.event.RenderHighlightEvent;
import mekanism.fabric_shim.client.event.sound.PlaySoundEvent;
import mekanism.fabric_shim.common.NeoForge;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

/**
 * Stand-in for NeoForge's {@code ClientHooks} (only the slice Mekanism calls): each hook posts the
 * corresponding shim event on the game bus and reports the outcome, so Mekanism's own listeners
 * (sound muffling, custom block highlights) see the same pipeline they subscribe to. Fresh
 * implementation.
 */
public final class ClientHooks {

    private ClientHooks() {
    }

    /**
     * Fires {@link PlaySoundEvent} (SoundHandler re-runs it for muffled-volume checks); returns the
     * possibly-swapped sound, or null if a listener suppressed it.
     */
    @Nullable
    public static SoundInstance playSound(SoundEngine engine, SoundInstance sound) {
        PlaySoundEvent event = new PlaySoundEvent(engine, sound);
        NeoForge.EVENT_BUS.post(event);
        return event.getSound();
    }

    /**
     * Fires {@link RenderHighlightEvent.Block}; returns true if a listener canceled the highlight.
     * NeoForge dispatches on the hit-result type — Mekanism only calls the block form.
     */
    public static boolean onDrawHighlight(LevelRenderer levelRenderer, Camera camera, BlockHitResult target, DeltaTracker deltaTracker, PoseStack poseStack,
          MultiBufferSource bufferSource) {
        RenderHighlightEvent.Block event = new RenderHighlightEvent.Block(levelRenderer, camera, deltaTracker, poseStack, bufferSource, target);
        NeoForge.EVENT_BUS.post(event);
        return event.isCanceled();
    }
}
