package mekanism.fabric_shim.attachment;

import java.util.Optional;

/**
 * Same surface as net.neoforged.neoforge.attachment.IAttachmentHolder. On NeoForge this is
 * implemented by patched vanilla classes (Entity, Level, ChunkAccess, ItemStack); the Fabric port
 * routes attachment access through the Fabric data attachment API instead — implementations and
 * the data-access wiring land with the Phase 3 usage sites (RadiationManager and friends).
 */
public interface IAttachmentHolder {

    boolean hasAttachments();

    boolean hasData(AttachmentType<?> type);

    <T> T getData(AttachmentType<T> type);

    <T> Optional<T> getExistingData(AttachmentType<T> type);

    <T> T setData(AttachmentType<T> type, T data);

    <T> T removeData(AttachmentType<T> type);
}
