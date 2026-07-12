package mekanism.fabric_shim.event.level;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.chunk.ChunkAccess;

/**
 * Stand-in for NeoForge's {@code ChunkEvent}, fired by the Phase 3 gameplay bridges.
 *
 * <p>NeoForge's single-arg constructors derive the level from a patched
 * {@code ChunkAccess#getLevel()} that vanilla lacks, so the port's bridges use the level-carrying
 * overloads (port-only additions; upstream code never constructs these).
 */
public abstract class ChunkEvent extends LevelEvent {

    private final ChunkAccess chunk;

    public ChunkEvent(ChunkAccess chunk) {
        this(chunk, null);
    }

    public ChunkEvent(ChunkAccess chunk, @org.jetbrains.annotations.Nullable LevelAccessor level) {
        super(level);
        this.chunk = chunk;
    }

    public ChunkAccess getChunk() {
        return this.chunk;
    }

    public static class Load extends ChunkEvent {

        private final boolean newChunk;

        public Load(ChunkAccess chunk, boolean newChunk) {
            this(chunk, null, newChunk);
        }

        public Load(ChunkAccess chunk, @org.jetbrains.annotations.Nullable LevelAccessor level, boolean newChunk) {
            super(chunk, level);
            this.newChunk = newChunk;
        }

        public boolean isNewChunk() {
            return this.newChunk;
        }
    }

    public static class Unload extends ChunkEvent {

        public Unload(ChunkAccess chunk) {
            super(chunk);
        }

        public Unload(ChunkAccess chunk, @org.jetbrains.annotations.Nullable LevelAccessor level) {
            super(chunk, level);
        }
    }
}
