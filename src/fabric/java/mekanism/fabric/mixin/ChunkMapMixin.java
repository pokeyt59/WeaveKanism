package mekanism.fabric.mixin;

import mekanism.fabric_shim.common.NeoForge;
import mekanism.fabric_shim.event.level.ChunkTicketLevelUpdatedEvent;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.jetbrains.annotations.Nullable;

/**
 * Posts NeoForge's chunk-ticket-level event from vanilla's scheduling update — Mekanism's
 * transmitter network registry tracks chunk load/unload transitions through it.
 */
@Mixin(ChunkMap.class)
public abstract class ChunkMapMixin {

    @Shadow
    @Final
    ServerLevel level;

    @Inject(method = "updateChunkScheduling", at = @At("RETURN"))
    private void mekanism$fireTicketLevelUpdated(long chunkPos, int newLevel, @Nullable ChunkHolder holder, int oldLevel,
          CallbackInfoReturnable<ChunkHolder> cir) {
        NeoForge.EVENT_BUS.post(new ChunkTicketLevelUpdatedEvent(level, chunkPos, oldLevel, newLevel, cir.getReturnValue()));
    }
}
