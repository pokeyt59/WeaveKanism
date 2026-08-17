package mekanism.fabric_shim.internal;

import mekanism.fabric_shim.client.event.ClientPlayerNetworkEvent;
import mekanism.fabric_shim.client.event.ClientTickEvent;
import mekanism.fabric_shim.client.event.RenderHighlightEvent;
import mekanism.fabric_shim.client.event.RenderLevelStageEvent;
import mekanism.fabric_shim.client.event.ScreenEvent;
import mekanism.fabric_shim.common.NeoForge;
import mekanism.fabric_shim.event.entity.EntityJoinLevelEvent;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

/**
 * Client counterpart of {@link ShimGameplayEvents}: bridges Fabric's client callbacks into the
 * NeoForge-shaped game-bus events Mekanism's client tick/render handlers subscribe to. Called once
 * from the Fabric client bootstrap. Events with no Fabric callback (mouse scroll, fog, recipes
 * tail, living pre/post, arm, HUD-layer pre, sound play/load, atlas stitched) come from client
 * mixins instead (step 7b).
 *
 * <p>Deviations: both {@code RenderLevelStageEvent} stages Mekanism listens to fire at Fabric's
 * single after-translucent point (bolts render before actual particles instead of after —
 * additive glow, order-insensitive). {@code ScreenEvent.Opening} is not bridged: its only
 * listener is recipe-viewer gated (Phase 5).
 */
public final class ShimClientGameplayEvents {

    private ShimClientGameplayEvents() {
    }

    public static void init() {
        ClientTickEvents.START_CLIENT_TICK.register(client -> NeoForge.EVENT_BUS.post(new ClientTickEvent.Pre()));
        ClientTickEvents.END_CLIENT_TICK.register(client -> NeoForge.EVENT_BUS.post(new ClientTickEvent.Post()));

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) ->
              NeoForge.EVENT_BUS.post(new ClientPlayerNetworkEvent.LoggingIn(handler.getConnection())));
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) ->
              NeoForge.EVENT_BUS.post(new ClientPlayerNetworkEvent.LoggingOut(handler.getConnection())));

        //Server-side joins bridge in ShimGameplayEvents; the client half matters for player-sound
        // tracking (flamethrower) which checks isClientSide
        ClientEntityEvents.ENTITY_LOAD.register((entity, level) ->
              NeoForge.EVENT_BUS.post(new EntityJoinLevelEvent(entity, level)));

        WorldRenderEvents.AFTER_TRANSLUCENT.register(context -> {
            //NeoForge's renderTick is LevelRenderer's tick counter (MekLevelRendererExt inject)
            int renderTick = context.worldRenderer().getTicks();
            NeoForge.EVENT_BUS.post(new RenderLevelStageEvent(RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS,
                  context.matrixStack(), renderTick, context.tickCounter(), context.camera()));
            NeoForge.EVENT_BUS.post(new RenderLevelStageEvent(RenderLevelStageEvent.Stage.AFTER_PARTICLES,
                  context.matrixStack(), renderTick, context.tickCounter(), context.camera()));
        });

        WorldRenderEvents.BLOCK_OUTLINE.register((context, blockOutlineContext) -> {
            if (Minecraft.getInstance().hitResult instanceof BlockHitResult target && target.getType() == HitResult.Type.BLOCK) {
                RenderHighlightEvent.Block event = new RenderHighlightEvent.Block(context.worldRenderer(), context.camera(),
                      context.tickCounter(), context.matrixStack(), context.consumers(), target);
                NeoForge.EVENT_BUS.post(event);
                //Canceled means a listener drew its own highlight: skip the vanilla outline
                return !event.isCanceled();
            }
            return true;
        });

        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) ->
              ScreenEvents.afterRender(screen).register((screen1, guiGraphics, mouseX, mouseY, tickDelta) ->
                    NeoForge.EVENT_BUS.post(new ScreenEvent.Render.Post(screen1, guiGraphics))));
    }
}
