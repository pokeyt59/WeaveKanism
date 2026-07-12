package mekanism.fabric_shim.event.level;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkType;

/**
 * Stand-in for NeoForge's {@code ChunkDataEvent}. Compile-only; firing is Phase 3.
 */
public abstract class ChunkDataEvent extends ChunkEvent {

    private final CompoundTag data;

    public ChunkDataEvent(ChunkAccess chunk, CompoundTag data) {
        super(chunk);
        this.data = data;
    }

    public ChunkDataEvent(ChunkAccess chunk, LevelAccessor world, CompoundTag data) {
        super(chunk, world);
        this.data = data;
    }

    public CompoundTag getData() {
        return this.data;
    }

    public static class Load extends ChunkDataEvent {

        private final ChunkType type;

        public Load(ChunkAccess chunk, CompoundTag data, ChunkType type) {
            super(chunk, data);
            this.type = type;
        }

        //Port-only overload: NeoForge derives the level from its patched ChunkAccess#getLevel,
        //so the ChunkSerializer bridge mixin passes it explicitly instead.
        public Load(ChunkAccess chunk, LevelAccessor world, CompoundTag data, ChunkType type) {
            super(chunk, world, data);
            this.type = type;
        }

        public ChunkType getType() {
            return this.type;
        }
    }

    public static class Save extends ChunkDataEvent {

        public Save(ChunkAccess chunk, LevelAccessor world, CompoundTag data) {
            super(chunk, world, data);
        }
    }
}
