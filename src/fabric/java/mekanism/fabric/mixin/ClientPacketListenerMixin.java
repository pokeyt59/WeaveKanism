package mekanism.fabric.mixin;

import mekanism.fabric_shim.client.event.ClientPlayerNetworkEvent;
import mekanism.fabric_shim.client.event.RecipesUpdatedEvent;
import mekanism.fabric_shim.common.NeoForge;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateRecipesPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Fires {@code RecipesUpdatedEvent} after the recipe sync lands (Mekanism clears its recipe-type
 * caches) and {@code ClientPlayerNetworkEvent.Clone} after a respawn swaps the local player
 * (Mekanism resets dimension-bound client state when the level changed). Both injections only
 * complete on the game thread — the netty pass exits early via vanilla's thread re-dispatch.
 */
@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerMixin {

    //The minecraft field lives in the parent (ClientCommonPacketListenerImpl) — out of @Shadow's
    // reach — and is just a cached reference to the singleton.
    @Unique
    private LocalPlayer mek$respawnOldPlayer;

    @Inject(method = "handleUpdateRecipes", at = @At("RETURN"))
    private void mek$recipesUpdated(ClientboundUpdateRecipesPacket packet, CallbackInfo ci) {
        NeoForge.EVENT_BUS.post(new RecipesUpdatedEvent());
    }

    @Inject(method = "handleRespawn", at = @At("HEAD"))
    private void mek$captureOldPlayer(ClientboundRespawnPacket packet, CallbackInfo ci) {
        this.mek$respawnOldPlayer = Minecraft.getInstance().player;
    }

    @Inject(method = "handleRespawn", at = @At("RETURN"))
    private void mek$postClone(ClientboundRespawnPacket packet, CallbackInfo ci) {
        LocalPlayer oldPlayer = this.mek$respawnOldPlayer;
        this.mek$respawnOldPlayer = null;
        LocalPlayer newPlayer = Minecraft.getInstance().player;
        if (oldPlayer != null && newPlayer != null) {
            NeoForge.EVENT_BUS.post(new ClientPlayerNetworkEvent.Clone(oldPlayer, newPlayer, ((ClientPacketListener) (Object) this).getConnection()));
        }
    }
}
