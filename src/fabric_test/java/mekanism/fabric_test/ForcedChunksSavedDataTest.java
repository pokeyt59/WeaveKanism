package mekanism.fabric_test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;
import mekanism.fabric_shim.common.world.chunk.ForcedChunksSavedData;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.level.ChunkPos;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Pins the on-disk format of the port's forced-chunk owner persistence (this data lives in user
 * worlds — the shape must stay readable across port versions).
 */
class ForcedChunksSavedDataTest {

    @BeforeAll
    static void bootstrap() {
        McBootstrap.ensure();
    }

    private static CompoundTag ticketSet(long[] nonTicking, long[] ticking) {
        CompoundTag tag = new CompoundTag();
        tag.putLongArray("non_ticking", nonTicking);
        tag.putLongArray("ticking", ticking);
        return tag;
    }

    @Test
    @DisplayName("save(load(tag)) round-trips block and entity owners, both ticking classes")
    void nbtRoundTrip() {
        CompoundTag controller = new CompoundTag();
        controller.putString("id", "mekanism:chunk_loader");

        ListTag blocks = new ListTag();
        CompoundTag block = ticketSet(new long[]{ChunkPos.asLong(1, 2), ChunkPos.asLong(3, 4)}, new long[]{ChunkPos.asLong(5, 6)});
        block.putLong("pos", net.minecraft.core.BlockPos.asLong(16, 64, 32));
        blocks.add(block);
        controller.put("blocks", blocks);

        ListTag entities = new ListTag();
        CompoundTag entity = ticketSet(new long[0], new long[]{ChunkPos.asLong(-7, 9)});
        entity.putUUID("owner", UUID.fromString("7bd01f4c-1a2b-4cde-9f00-123456789abc"));
        entities.add(entity);
        controller.put("entities", entities);

        CompoundTag root = new CompoundTag();
        ListTag controllers = new ListTag();
        controllers.add(controller);
        root.put("controllers", controllers);

        ForcedChunksSavedData data = ForcedChunksSavedData.load(root, RegistryAccess.EMPTY);
        CompoundTag saved = data.save(new CompoundTag(), RegistryAccess.EMPTY);

        //Compare semantically: same controllers, owners, and chunk sets (long-array order within
        // a set is unspecified — hash sets — so normalize before comparing)
        assertEquals(1, saved.getList("controllers", CompoundTag.TAG_COMPOUND).size());
        CompoundTag savedController = saved.getList("controllers", CompoundTag.TAG_COMPOUND).getCompound(0);
        assertEquals("mekanism:chunk_loader", savedController.getString("id"));

        CompoundTag savedBlock = savedController.getList("blocks", CompoundTag.TAG_COMPOUND).getCompound(0);
        assertEquals(block.getLong("pos"), savedBlock.getLong("pos"));
        assertEquals(sorted(block.getLongArray("non_ticking")), sorted(savedBlock.getLongArray("non_ticking")));
        assertEquals(sorted(block.getLongArray("ticking")), sorted(savedBlock.getLongArray("ticking")));

        CompoundTag savedEntity = savedController.getList("entities", CompoundTag.TAG_COMPOUND).getCompound(0);
        assertEquals(entity.getUUID("owner"), savedEntity.getUUID("owner"));
        assertEquals(sorted(entity.getLongArray("ticking")), sorted(savedEntity.getLongArray("ticking")));
        assertTrue(savedEntity.getLongArray("non_ticking").length == 0);
    }

    @Test
    @DisplayName("empty owner entries are dropped on load instead of resurrecting")
    void emptyControllerDropped() {
        CompoundTag controller = new CompoundTag();
        controller.putString("id", "mekanism:chunk_loader");
        controller.put("blocks", new ListTag());
        controller.put("entities", new ListTag());
        CompoundTag root = new CompoundTag();
        ListTag controllers = new ListTag();
        controllers.add(controller);
        root.put("controllers", controllers);

        ForcedChunksSavedData data = ForcedChunksSavedData.load(root, RegistryAccess.EMPTY);
        assertEquals(0, data.save(new CompoundTag(), RegistryAccess.EMPTY).getList("controllers", CompoundTag.TAG_COMPOUND).size());
    }

    private static java.util.List<Long> sorted(long[] values) {
        return java.util.Arrays.stream(values).sorted().boxed().toList();
    }
}
