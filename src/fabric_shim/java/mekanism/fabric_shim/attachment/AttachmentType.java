package mekanism.fabric_shim.attachment;

import com.mojang.serialization.Codec;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import mekanism.fabric_shim.common.util.INBTSerializable;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import org.jetbrains.annotations.Nullable;

/**
 * Same surface as net.neoforged.neoforge.attachment.AttachmentType (builder, codec/INBTSerializable
 * serialization, copy handlers). This shim holds the configuration; the actual data storage is
 * wired to the Fabric data attachment API together with the Phase 3 usage sites (getData/setData
 * call sites in RadiationManager and friends), once the attachment's registry name is known from
 * its DeferredRegister registration.
 */
public final class AttachmentType<T> {

    private final Function<IAttachmentHolder, T> defaultValueSupplier;
    @Nullable
    private final IAttachmentSerializer<?, T> serializer;
    @Nullable
    private final Codec<T> persistenceCodec;
    private final boolean copyOnDeath;
    @Nullable
    private final IAttachmentCopyHandler<T> copyHandler;
    //Bound after registration by AttachmentHooks.bridgeRegisteredTypes — the Fabric attachment
    // type that actually stores/persists the data on Entity/ServerLevel holders
    @Nullable
    private net.fabricmc.fabric.api.attachment.v1.AttachmentType<T> fabricType;

    private AttachmentType(Builder<T> builder) {
        this.defaultValueSupplier = builder.defaultValueSupplier;
        this.serializer = builder.serializer;
        this.persistenceCodec = builder.persistenceCodec;
        this.copyOnDeath = builder.copyOnDeath;
        this.copyHandler = builder.copyHandler;
    }

    public Function<IAttachmentHolder, T> defaultValueSupplier() {
        return defaultValueSupplier;
    }

    @Nullable
    public IAttachmentSerializer<?, T> serializer() {
        return serializer;
    }

    /** The raw codec captured from {@code serialize(Codec, ...)}, if that path built this type. */
    @Nullable
    public Codec<T> persistenceCodec() {
        return persistenceCodec;
    }

    @Nullable
    public net.fabricmc.fabric.api.attachment.v1.AttachmentType<T> fabricType() {
        return fabricType;
    }

    public void bindFabricType(net.fabricmc.fabric.api.attachment.v1.AttachmentType<T> fabricType) {
        if (this.fabricType != null) {
            throw new IllegalStateException("Fabric attachment type already bound");
        }
        this.fabricType = fabricType;
    }

    public boolean copyOnDeath() {
        return copyOnDeath;
    }

    @Nullable
    public IAttachmentCopyHandler<T> copyHandler() {
        return copyHandler;
    }

    public static <T> Builder<T> builder(Supplier<T> defaultValueSupplier) {
        Objects.requireNonNull(defaultValueSupplier);
        return builder(holder -> defaultValueSupplier.get());
    }

    public static <T> Builder<T> builder(Function<IAttachmentHolder, T> defaultValueConstructor) {
        return new Builder<>(Objects.requireNonNull(defaultValueConstructor));
    }

    public static <S extends Tag, T extends INBTSerializable<S>> Builder<T> serializable(Supplier<T> defaultValueSupplier) {
        Objects.requireNonNull(defaultValueSupplier);
        return serializable(holder -> defaultValueSupplier.get());
    }

    public static <S extends Tag, T extends INBTSerializable<S>> Builder<T> serializable(Function<IAttachmentHolder, T> defaultValueConstructor) {
        return builder(defaultValueConstructor).serialize(new IAttachmentSerializer<S, T>() {
            @Override
            public T read(IAttachmentHolder holder, S tag, HolderLookup.Provider provider) {
                T value = defaultValueConstructor.apply(holder);
                value.deserializeNBT(provider, tag);
                return value;
            }

            @Override
            public S write(T attachment, HolderLookup.Provider provider) {
                return attachment.serializeNBT(provider);
            }
        });
    }

    public static class Builder<T> {

        private final Function<IAttachmentHolder, T> defaultValueSupplier;
        @Nullable
        private IAttachmentSerializer<?, T> serializer;
        @Nullable
        private Codec<T> persistenceCodec;
        private boolean copyOnDeath;
        @Nullable
        private IAttachmentCopyHandler<T> copyHandler;

        private Builder(Function<IAttachmentHolder, T> defaultValueSupplier) {
            this.defaultValueSupplier = defaultValueSupplier;
        }

        public Builder<T> serialize(IAttachmentSerializer<?, T> serializer) {
            Objects.requireNonNull(serializer);
            if (this.serializer != null) {
                throw new IllegalStateException("Serializer already set");
            }
            this.serializer = serializer;
            return this;
        }

        public Builder<T> serialize(Codec<T> codec) {
            return serialize(codec, value -> true);
        }

        public Builder<T> serialize(Codec<T> codec, Predicate<? super T> shouldSerialize) {
            Objects.requireNonNull(codec);
            this.persistenceCodec = codec;
            return serialize(new IAttachmentSerializer<Tag, T>() {
                @Override
                public T read(IAttachmentHolder holder, Tag tag, HolderLookup.Provider provider) {
                    return codec.parse(provider.createSerializationContext(NbtOps.INSTANCE), tag).getOrThrow();
                }

                @Nullable
                @Override
                public Tag write(T attachment, HolderLookup.Provider provider) {
                    if (!shouldSerialize.test(attachment)) {
                        return null;
                    }
                    return codec.encodeStart(provider.createSerializationContext(NbtOps.INSTANCE), attachment).getOrThrow();
                }
            });
        }

        public Builder<T> copyOnDeath() {
            if (this.serializer == null) {
                throw new IllegalStateException("copyOnDeath requires a serializer");
            }
            this.copyOnDeath = true;
            return this;
        }

        public Builder<T> copyHandler(IAttachmentCopyHandler<T> cloner) {
            Objects.requireNonNull(cloner);
            if (this.serializer == null) {
                throw new IllegalStateException("copyHandler requires a serializer");
            }
            this.copyHandler = cloner;
            return this;
        }

        public AttachmentType<T> build() {
            return new AttachmentType<>(this);
        }
    }
}
