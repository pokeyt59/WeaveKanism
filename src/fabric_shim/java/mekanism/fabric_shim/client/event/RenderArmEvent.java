package mekanism.fabric_shim.client.event;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.HumanoidArm;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

/**
 * Stand-in for NeoForge's {@code RenderArmEvent} (game bus): fired when a player's first-person arm is
 * rendered, so Mekanism can draw the MekaSuit sleeve and cancel the vanilla arm. Posted from an
 * ItemInHandRenderer#renderPlayerArm mixin (cancellable) at step 3b. Fresh implementation.
 */
public class RenderArmEvent extends Event implements ICancellableEvent {

    private final AbstractClientPlayer player;
    private final HumanoidArm arm;
    private final PoseStack poseStack;
    private final MultiBufferSource multiBufferSource;
    private final int packedLight;

    public RenderArmEvent(AbstractClientPlayer player, HumanoidArm arm, PoseStack poseStack, MultiBufferSource multiBufferSource, int packedLight) {
        this.player = player;
        this.arm = arm;
        this.poseStack = poseStack;
        this.multiBufferSource = multiBufferSource;
        this.packedLight = packedLight;
    }

    public AbstractClientPlayer getPlayer() {
        return player;
    }

    public HumanoidArm getArm() {
        return arm;
    }

    public PoseStack getPoseStack() {
        return poseStack;
    }

    public MultiBufferSource getMultiBufferSource() {
        return multiBufferSource;
    }

    public int getPackedLight() {
        return packedLight;
    }
}
