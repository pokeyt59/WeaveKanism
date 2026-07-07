package mekanism.fabric_shim.attachment;

import net.minecraft.core.HolderLookup;
import org.jetbrains.annotations.Nullable;

/**
 * Same surface as net.neoforged.neoforge.attachment.IAttachmentCopyHandler.
 */
public interface IAttachmentCopyHandler<T> {

    @Nullable
    T copy(T attachment, IAttachmentHolder holder, HolderLookup.Provider provider);
}
