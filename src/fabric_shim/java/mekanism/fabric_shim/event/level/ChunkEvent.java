package mekanism.fabric_shim.event.level;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.chunk.ChunkAccess;

/**
 * Stand-in for NeoForge's {@code ChunkEvent}. Compile-only; firing is Phase 3.
 *
 * <p>NeoForge's single-arg constructor derives the level from a patched {@code ChunkAccess#getLevel()}
 * that vanilla lacks, so here it passes {@code null} — harmless while these events do not fire.
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
            super(chunk);
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
    }
}
