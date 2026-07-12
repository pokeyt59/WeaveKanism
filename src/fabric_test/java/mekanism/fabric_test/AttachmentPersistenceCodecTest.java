package mekanism.fabric_test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mojang.serialization.Codec;
import java.util.ArrayList;
import java.util.List;
import mekanism.fabric_shim.attachment.AttachmentHooks;
import mekanism.fabric_shim.attachment.AttachmentType;
import mekanism.fabric_shim.common.util.INBTSerializable;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Pins the persistence bridge from shim AttachmentTypes to Fabric attachment codecs: raw codecs
 * pass through untouched, and INBTSerializable serializers get the {@code {"v": tag}} envelope
 * whose absent-{@code v} form encodes NeoForge's "writer returned null, treat as default on load"
 * contract (MeltdownLevelData/RadiationLevelData return null when empty). This shapes world data.
 */
class AttachmentPersistenceCodecTest {

    @BeforeAll
    static void bootstrap() {
        McBootstrap.ensure();
    }

    /** Mirrors MeltdownLevelData's contract: serializes to a ListTag, null when empty. */
    private static final class TestData implements INBTSerializable<ListTag> {

        final List<Long> values = new ArrayList<>();

        @Nullable
        @Override
        public ListTag serializeNBT(HolderLookup.Provider provider) {
            if (values.isEmpty()) {
                return null;
            }
            ListTag list = new ListTag();
            values.forEach(value -> list.add(LongTag.valueOf(value)));
            return list;
        }

        @Override
        public void deserializeNBT(HolderLookup.Provider provider, ListTag tag) {
            for (Tag element : tag) {
                values.add(((LongTag) element).getAsLong());
            }
        }
    }

    @Test
    @DisplayName("raw-codec attachments hand their codec to Fabric unchanged")
    void rawCodecPassesThrough() {
        AttachmentType<Double> type = AttachmentType.builder(() -> 0.0D)
              .serialize(Codec.DOUBLE, value -> value > 0)
              .build();
        assertSame(Codec.DOUBLE, AttachmentHooks.persistenceCodec(type));
    }

    @Test
    @DisplayName("unserialized attachments get no persistence codec")
    void transientTypesHaveNoCodec() {
        assertNull(AttachmentHooks.persistenceCodec(AttachmentType.builder(() -> 1).build()));
    }

    @Test
    @DisplayName("serializable attachments round-trip through the envelope")
    void envelopeRoundTrip() {
        AttachmentType<TestData> type = AttachmentType.serializable(TestData::new).build();
        Codec<TestData> codec = AttachmentHooks.persistenceCodec(type);
        assertNotNull(codec);

        TestData data = new TestData();
        data.values.add(42L);
        data.values.add(-7L);

        Tag encoded = codec.encodeStart(NbtOps.INSTANCE, data).getOrThrow();
        assertTrue(encoded instanceof CompoundTag envelope && envelope.contains("v"));
        TestData decoded = codec.parse(NbtOps.INSTANCE, encoded).getOrThrow();
        assertEquals(List.of(42L, -7L), decoded.values);
    }

    @Test
    @DisplayName("a null write (empty data) encodes as an empty envelope and decodes to a fresh default")
    void nullWriteDecodesToDefault() {
        AttachmentType<TestData> type = AttachmentType.serializable(TestData::new).build();
        Codec<TestData> codec = AttachmentHooks.persistenceCodec(type);
        assertNotNull(codec);

        Tag encoded = codec.encodeStart(NbtOps.INSTANCE, new TestData()).getOrThrow();
        assertTrue(encoded instanceof CompoundTag envelope && !envelope.contains("v"));
        TestData decoded = codec.parse(NbtOps.INSTANCE, encoded).getOrThrow();
        assertTrue(decoded.values.isEmpty());
        assertFalse(decoded == null);
    }
}
