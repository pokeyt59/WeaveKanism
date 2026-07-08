package mekanism.fabric_shim.network.codec;

import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

/**
 * Stream codec helpers (stand-in for NeoForge's NeoForgeStreamCodecs; only the surface Mekanism
 * uses is provided).
 */
public final class NeoForgeStreamCodecs {

    private NeoForgeStreamCodecs() {
    }

    /** Defers resolution of a stream codec until first use (for recursive/forward references). */
    public static <B, V> StreamCodec<B, V> lazy(Supplier<StreamCodec<B, V>> streamCodecSupplier) {
        return new StreamCodec<>() {
            private StreamCodec<B, V> delegate;

            private StreamCodec<B, V> delegate() {
                if (this.delegate == null) {
                    this.delegate = streamCodecSupplier.get();
                }
                return this.delegate;
            }

            @Override
            public V decode(B buffer) {
                return delegate().decode(buffer);
            }

            @Override
            public void encode(B buffer, V value) {
                delegate().encode(buffer, value);
            }
        };
    }

    public static <B extends FriendlyByteBuf, V extends Enum<V>> StreamCodec<B, V> enumCodec(Class<V> enumClass) {
        return new StreamCodec<>() {
            @Override
            public V decode(B buf) {
                return buf.readEnum(enumClass);
            }

            @Override
            public void encode(B buf, V value) {
                buf.writeEnum(value);
            }
        };
    }
}
