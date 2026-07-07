package mekanism.fabric_shim.attachment;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.Tag;
import org.jetbrains.annotations.Nullable;

/**
 * Same surface as net.neoforged.neoforge.attachment.IAttachmentSerializer.
 */
public interface IAttachmentSerializer<S extends Tag, T> {

    T read(IAttachmentHolder holder, S tag, HolderLookup.Provider provider);

    @Nullable
    S write(T attachment, HolderLookup.Provider provider);
}
