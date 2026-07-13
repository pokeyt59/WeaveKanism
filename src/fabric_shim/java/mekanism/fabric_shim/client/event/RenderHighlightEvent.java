package mekanism.fabric_shim.client.event;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

/**
 * Stand-in for NeoForge's {@code RenderHighlightEvent} (game bus): Block fires while the targeted
 * block's selection outline is drawn, so Mekanism can draw multiblock/miner wireframes. Bridged off
 * Fabric's WorldRenderEvents.BLOCK_OUTLINE (cancel → skip vanilla outline) at step 3b. Only the Block
 * slice Mekanism uses. Fresh implementation.
 */
public abstract class RenderHighlightEvent extends Event {

    private final LevelRenderer levelRenderer;
    private final Camera camera;
    private final DeltaTracker deltaTracker;
    private final PoseStack poseStack;
    private final MultiBufferSource multiBufferSource;

    protected RenderHighlightEvent(LevelRenderer levelRenderer, Camera camera, DeltaTracker deltaTracker, PoseStack poseStack, MultiBufferSource multiBufferSource) {
        this.levelRenderer = levelRenderer;
        this.camera = camera;
        this.deltaTracker = deltaTracker;
        this.poseStack = poseStack;
        this.multiBufferSource = multiBufferSource;
    }

    public LevelRenderer getLevelRenderer() {
        return levelRenderer;
    }

    public Camera getCamera() {
        return camera;
    }

    public DeltaTracker getDeltaTracker() {
        return deltaTracker;
    }

    public PoseStack getPoseStack() {
        return poseStack;
    }

    public MultiBufferSource getMultiBufferSource() {
        return multiBufferSource;
    }

    public static class Block extends RenderHighlightEvent implements ICancellableEvent {

        private final BlockHitResult target;

        public Block(LevelRenderer levelRenderer, Camera camera, DeltaTracker deltaTracker, PoseStack poseStack, MultiBufferSource multiBufferSource, BlockHitResult target) {
            super(levelRenderer, camera, deltaTracker, poseStack, multiBufferSource);
            this.target = target;
        }

        public BlockHitResult getTarget() {
            return target;
        }
    }
}
