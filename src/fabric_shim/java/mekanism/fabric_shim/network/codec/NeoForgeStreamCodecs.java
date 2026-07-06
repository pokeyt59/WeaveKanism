package mekanism.fabric_shim.network.codec;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

/**
 * Stream codec helpers (stand-in for NeoForge's NeoForgeStreamCodecs; only the surface Mekanism
 * uses is provided).
 */
public final class NeoForgeStreamCodecs {

    private NeoForgeStreamCodecs() {
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
