package mekanism.fabric_shim.attachment;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;
import mekanism.fabric_shim.registries.NeoForgeRegistries;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentTarget;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Backing for the injected {@code getData/setData/...} attachment accessors (NeoForge's
 * {@code IAttachmentHolder} surface). Since Phase 3 the real storage is
 * {@code fabric-data-attachment-api-v1}: {@link #bridgeRegisteredTypes()} (bootstrap, after the
 * attachment-type RegisterEvent) creates a Fabric attachment type per registered shim type —
 * initializer from the default supplier, persistence from the captured codec (or an NBT envelope
 * around the INBTSerializable serializer: {@code {"v": tag}}, absent {@code v} = "writer skipped"
 * → fresh default), {@code copyOnDeath} forwarded — and accessors route through the holder's
 * {@link AttachmentTarget} (Entity, ServerLevel, BlockEntity, chunks). Fabric persists these with
 * the holder and handles player-respawn copying.
 *
 * <p>Deviations (PORTING.md): the shouldSerialize predicate is not applied (default-valued
 * attachments write a few extra bytes; loading is equivalent), and NeoForge's copyHandler
 * customization is unused — Fabric's plain copy has the same observable result for Mekanism's
 * types. The {@link WeakHashMap} fallback remains only for holders that are not Fabric attachment
 * targets (none in Mekanism's usage today).
 */
public final class AttachmentHooks {

    private static final Logger LOGGER = LoggerFactory.getLogger("MekanismShim");
    private static final Map<Object, Map<AttachmentType<?>, Object>> STORE = Collections.synchronizedMap(new WeakHashMap<>());

    private AttachmentHooks() {
    }

    /** Creates + binds the Fabric attachment type for every registered shim attachment type. */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void bridgeRegisteredTypes() {
        Registry<AttachmentType<?>> registry = (Registry<AttachmentType<?>>) BuiltInRegistries.REGISTRY
              .get(NeoForgeRegistries.Keys.ATTACHMENT_TYPES.location());
        if (registry == null) {
            LOGGER.warn("Attachment type registry missing; attachments stay on the transient store");
            return;
        }
        for (Map.Entry<ResourceKey<AttachmentType<?>>, AttachmentType<?>> entry : registry.entrySet()) {
            bridge(entry.getKey(), (AttachmentType) entry.getValue());
        }
    }

    private static <T> void bridge(ResourceKey<AttachmentType<?>> key, AttachmentType<T> type) {
        AttachmentRegistry.Builder<T> builder = AttachmentRegistry.<T>builder()
              //All of Mekanism's defaults ignore the holder argument (see MekanismAttachmentTypes)
              .initializer(() -> type.defaultValueSupplier().apply(null));
        Codec<T> codec = persistenceCodec(type);
        if (codec != null) {
            builder = builder.persistent(codec);
        }
        if (type.copyOnDeath()) {
            builder = builder.copyOnDeath();
        }
        type.bindFabricType(builder.buildAndRegister(key.location()));
    }

    /** Public for the guardrail tests — the envelope semantics protect world data. */
    @Nullable
    @SuppressWarnings("unchecked")
    public static <T> Codec<T> persistenceCodec(AttachmentType<T> type) {
        Codec<T> raw = type.persistenceCodec();
        if (raw != null) {
            return raw;
        }
        IAttachmentSerializer<Tag, T> serializer = (IAttachmentSerializer<Tag, T>) type.serializer();
        if (serializer == null) {
            return null;
        }
        //Envelope codec over the INBTSerializable serializer. The write side may return null
        // ("nothing worth saving", e.g. an empty meltdown list) — encoded as an envelope without
        // "v", decoded back to a fresh default. The serializer implementations behind this path
        // ignore the holder/provider arguments (verified for Mekanism's level-data attachments).
        return Codec.PASSTHROUGH.comapFlatMap(dynamic -> {
            try {
                Tag tag = (Tag) dynamic.convert(NbtOps.INSTANCE).getValue();
                if (tag instanceof CompoundTag compound && compound.contains("v")) {
                    return DataResult.success(serializer.read(null, compound.get("v"), RegistryAccess.EMPTY));
                }
                return DataResult.success(type.defaultValueSupplier().apply(null));
            } catch (Exception e) {
                return DataResult.error(() -> "Failed to read attachment: " + e.getMessage());
            }
        }, value -> {
            CompoundTag envelope = new CompoundTag();
            Tag written = serializer.write(value, RegistryAccess.EMPTY);
            if (written != null) {
                envelope.put("v", written);
            }
            return new Dynamic<>(NbtOps.INSTANCE, envelope);
        });
    }

    @SuppressWarnings("unchecked")
    public static <T> T getData(Object holder, AttachmentType<T> type) {
        if (holder instanceof AttachmentTarget target && type.fabricType() != null) {
            return target.getAttachedOrCreate(type.fabricType());
        }
        Map<AttachmentType<?>, Object> map = STORE.computeIfAbsent(holder, k -> new HashMap<>());
        if (map.containsKey(type)) {
            return (T) map.get(type);
        }
        //NeoForge stores and returns the default on first access so mutable attachments are stable
        T def = type.defaultValueSupplier().apply(null);
        map.put(type, def);
        return def;
    }

    public static <T> boolean hasData(Object holder, AttachmentType<T> type) {
        if (holder instanceof AttachmentTarget target && type.fabricType() != null) {
            return target.hasAttached(type.fabricType());
        }
        Map<AttachmentType<?>, Object> map = STORE.get(holder);
        return map != null && map.containsKey(type);
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public static <T> T setData(Object holder, AttachmentType<T> type, T data) {
        if (holder instanceof AttachmentTarget target && type.fabricType() != null) {
            return target.setAttached(type.fabricType(), data);
        }
        return (T) STORE.computeIfAbsent(holder, k -> new HashMap<>()).put(type, data);
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public static <T> T removeData(Object holder, AttachmentType<T> type) {
        if (holder instanceof AttachmentTarget target && type.fabricType() != null) {
            return target.removeAttached(type.fabricType());
        }
        Map<AttachmentType<?>, Object> map = STORE.get(holder);
        return map == null ? null : (T) map.remove(type);
    }

    @SuppressWarnings("unchecked")
    public static <T> Optional<T> getExistingData(Object holder, AttachmentType<T> type) {
        if (holder instanceof AttachmentTarget target && type.fabricType() != null) {
            return Optional.ofNullable(target.getAttached(type.fabricType()));
        }
        Map<AttachmentType<?>, Object> map = STORE.get(holder);
        return map != null && map.containsKey(type) ? Optional.of((T) map.get(type)) : Optional.empty();
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public static <T> T getExistingDataOrNull(Object holder, AttachmentType<T> type) {
        if (holder instanceof AttachmentTarget target && type.fabricType() != null) {
            return target.getAttached(type.fabricType());
        }
        Map<AttachmentType<?>, Object> map = STORE.get(holder);
        return map == null ? null : (T) map.get(type);
    }
}
