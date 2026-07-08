package mekanism.fabric_shim.attachment;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;
import org.jetbrains.annotations.Nullable;

/**
 * Backing store for the injected {@code getData/setData/...} attachment accessors (NeoForge's
 * {@code IAttachmentHolder} surface), keyed by holder identity.
 *
 * <p><b>Phase 1f is transient only</b>: attachments live in an in-memory {@link WeakHashMap} and do
 * not persist across save/load or sync to clients. Phase 3 replaces this with
 * {@code fabric-data-attachment-api} wiring (see PORTING.md). No gameplay runs during the 1f boot
 * check, so the transient store is sufficient to compile and boot.
 */
public final class AttachmentHooks {

    private static final Map<Object, Map<AttachmentType<?>, Object>> STORE = Collections.synchronizedMap(new WeakHashMap<>());

    private AttachmentHooks() {
    }

    @SuppressWarnings("unchecked")
    public static <T> T getData(Object holder, AttachmentType<T> type) {
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
        Map<AttachmentType<?>, Object> map = STORE.get(holder);
        return map != null && map.containsKey(type);
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public static <T> T setData(Object holder, AttachmentType<T> type, T data) {
        return (T) STORE.computeIfAbsent(holder, k -> new HashMap<>()).put(type, data);
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public static <T> T removeData(Object holder, AttachmentType<T> type) {
        Map<AttachmentType<?>, Object> map = STORE.get(holder);
        return map == null ? null : (T) map.remove(type);
    }

    @SuppressWarnings("unchecked")
    public static <T> Optional<T> getExistingData(Object holder, AttachmentType<T> type) {
        Map<AttachmentType<?>, Object> map = STORE.get(holder);
        return map != null && map.containsKey(type) ? Optional.of((T) map.get(type)) : Optional.empty();
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public static <T> T getExistingDataOrNull(Object holder, AttachmentType<T> type) {
        Map<AttachmentType<?>, Object> map = STORE.get(holder);
        return map == null ? null : (T) map.get(type);
    }
}
