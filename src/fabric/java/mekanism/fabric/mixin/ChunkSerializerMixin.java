package mekanism.fabric.mixin;

import mekanism.fabric_shim.common.NeoForge;
import mekanism.fabric_shim.event.level.ChunkDataEvent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.chunk.storage.ChunkSerializer;
import net.minecraft.world.level.chunk.storage.RegionStorageInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Posts NeoForge's chunk NBT events from vanilla chunk serialization: Mekanism stamps/reads its
 * worldgen version into every chunk's save data for retrogen tracking.
 */
@Mixin(ChunkSerializer.class)
public abstract class ChunkSerializerMixin {

    @Inject(method = "write", at = @At("RETURN"))
    private static void mekanism$fireChunkSave(ServerLevel level, ChunkAccess chunk, CallbackInfoReturnable<CompoundTag> cir) {
        NeoForge.EVENT_BUS.post(new ChunkDataEvent.Save(chunk, level, cir.getReturnValue()));
    }

    @Inject(method = "read", at = @At("RETURN"))
    private static void mekanism$fireChunkLoad(ServerLevel level, PoiManager poiManager, RegionStorageInfo storageInfo, ChunkPos pos, CompoundTag tag,
          CallbackInfoReturnable<ProtoChunk> cir) {
        ProtoChunk chunk = cir.getReturnValue();
        NeoForge.EVENT_BUS.post(new ChunkDataEvent.Load(chunk, level, tag, chunk.getPersistedStatus().getChunkType()));
    }
}
