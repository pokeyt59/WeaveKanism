package mekanism.fabric_shim.inject;

import java.util.Optional;
import java.util.function.Supplier;
import mekanism.fabric_shim.attachment.AttachmentHooks;
import mekanism.fabric_shim.attachment.AttachmentType;
import org.jetbrains.annotations.Nullable;

/**
 * NeoForge's {@code IAttachmentHolder} data accessors, injected onto the vanilla holders Mekanism
 * attaches data to (Entity, Level; BlockEntity/ItemStack use their own component paths). Backed by
 * the transient {@link AttachmentHooks} store for 1f (persistence is Phase 3).
 */
public interface MekAttachmentExt {

    default <T> T getData(AttachmentType<T> type) {
        return AttachmentHooks.getData(this, type);
    }

    default <T> T getData(Supplier<AttachmentType<T>> type) {
        return AttachmentHooks.getData(this, type.get());
    }

    default <T> boolean hasData(AttachmentType<T> type) {
        return AttachmentHooks.hasData(this, type);
    }

    default <T> boolean hasData(Supplier<AttachmentType<T>> type) {
        return AttachmentHooks.hasData(this, type.get());
    }

    default <T> Optional<T> getExistingData(AttachmentType<T> type) {
        return AttachmentHooks.getExistingData(this, type);
    }

    default <T> Optional<T> getExistingData(Supplier<AttachmentType<T>> type) {
        return AttachmentHooks.getExistingData(this, type.get());
    }

    @Nullable
    default <T> T getExistingDataOrNull(AttachmentType<T> type) {
        return AttachmentHooks.getExistingDataOrNull(this, type);
    }

    @Nullable
    default <T> T getExistingDataOrNull(Supplier<AttachmentType<T>> type) {
        return AttachmentHooks.getExistingDataOrNull(this, type.get());
    }

    @Nullable
    default <T> T setData(AttachmentType<T> type, T data) {
        return AttachmentHooks.setData(this, type, data);
    }

    @Nullable
    default <T> T setData(Supplier<AttachmentType<T>> type, T data) {
        return AttachmentHooks.setData(this, type.get(), data);
    }

    @Nullable
    default <T> T removeData(Supplier<AttachmentType<T>> type) {
        return AttachmentHooks.removeData(this, type.get());
    }
}
