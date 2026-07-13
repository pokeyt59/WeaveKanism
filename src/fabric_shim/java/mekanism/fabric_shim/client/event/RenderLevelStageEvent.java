package mekanism.fabric_shim.client.event;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.neoforged.bus.api.Event;

/**
 * Stand-in for NeoForge's {@code RenderLevelStageEvent} (game bus): fired once per world-render stage
 * so Mekanism can draw its delayed/translucent renders and lightning. Only the two stages Mekanism
 * gates on exist as identity constants; posted from WorldRenderEvents (AFTER_TRANSLUCENT ≈
 * AFTER_TRANSLUCENT_BLOCKS, a particles stage ≈ AFTER_PARTICLES) at step 3b. Fresh implementation.
 */
public class RenderLevelStageEvent extends Event {

    /**
     * NeoForge's Stage is a registry of named pipeline points; Mekanism only identity-compares against
     * these two, so the shim carries just them.
     */
    public static final class Stage {

        public static final Stage AFTER_TRANSLUCENT_BLOCKS = new Stage("after_translucent_blocks");
        public static final Stage AFTER_PARTICLES = new Stage("after_particles");

        private final String name;

        private Stage(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    private final Stage stage;
    private final PoseStack poseStack;
    private final int renderTick;
    private final DeltaTracker partialTick;
    private final Camera camera;

    public RenderLevelStageEvent(Stage stage, PoseStack poseStack, int renderTick, DeltaTracker partialTick, Camera camera) {
        this.stage = stage;
        this.poseStack = poseStack;
        this.renderTick = renderTick;
        this.partialTick = partialTick;
        this.camera = camera;
    }

    public Stage getStage() {
        return stage;
    }

    public PoseStack getPoseStack() {
        return poseStack;
    }

    public int getRenderTick() {
        return renderTick;
    }

    public DeltaTracker getPartialTick() {
        return partialTick;
    }

    public Camera getCamera() {
        return camera;
    }
}
