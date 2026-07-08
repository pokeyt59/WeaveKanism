package mekanism.fabric_shim.network.codec;

import com.mojang.datafixers.util.Function7;
import com.mojang.datafixers.util.Function8;
import com.mojang.datafixers.util.Function9;
import java.util.function.Function;
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

    //Higher-arity StreamCodec.composite variants NeoForge adds (vanilla stops at 6). Only the
    //arities Mekanism uses are provided.
    public static <B, C, T1, T2, T3, T4, T5, T6, T7> StreamCodec<B, C> composite(
          StreamCodec<? super B, T1> c1, Function<C, T1> g1,
          StreamCodec<? super B, T2> c2, Function<C, T2> g2,
          StreamCodec<? super B, T3> c3, Function<C, T3> g3,
          StreamCodec<? super B, T4> c4, Function<C, T4> g4,
          StreamCodec<? super B, T5> c5, Function<C, T5> g5,
          StreamCodec<? super B, T6> c6, Function<C, T6> g6,
          StreamCodec<? super B, T7> c7, Function<C, T7> g7,
          Function7<T1, T2, T3, T4, T5, T6, T7, C> ctor) {
        return new StreamCodec<>() {
            @Override
            public C decode(B buf) {
                return ctor.apply(c1.decode(buf), c2.decode(buf), c3.decode(buf), c4.decode(buf), c5.decode(buf), c6.decode(buf), c7.decode(buf));
            }

            @Override
            public void encode(B buf, C v) {
                c1.encode(buf, g1.apply(v)); c2.encode(buf, g2.apply(v)); c3.encode(buf, g3.apply(v)); c4.encode(buf, g4.apply(v));
                c5.encode(buf, g5.apply(v)); c6.encode(buf, g6.apply(v)); c7.encode(buf, g7.apply(v));
            }
        };
    }

    public static <B, C, T1, T2, T3, T4, T5, T6, T7, T8> StreamCodec<B, C> composite(
          StreamCodec<? super B, T1> c1, Function<C, T1> g1,
          StreamCodec<? super B, T2> c2, Function<C, T2> g2,
          StreamCodec<? super B, T3> c3, Function<C, T3> g3,
          StreamCodec<? super B, T4> c4, Function<C, T4> g4,
          StreamCodec<? super B, T5> c5, Function<C, T5> g5,
          StreamCodec<? super B, T6> c6, Function<C, T6> g6,
          StreamCodec<? super B, T7> c7, Function<C, T7> g7,
          StreamCodec<? super B, T8> c8, Function<C, T8> g8,
          Function8<T1, T2, T3, T4, T5, T6, T7, T8, C> ctor) {
        return new StreamCodec<>() {
            @Override
            public C decode(B buf) {
                return ctor.apply(c1.decode(buf), c2.decode(buf), c3.decode(buf), c4.decode(buf), c5.decode(buf), c6.decode(buf), c7.decode(buf), c8.decode(buf));
            }

            @Override
            public void encode(B buf, C v) {
                c1.encode(buf, g1.apply(v)); c2.encode(buf, g2.apply(v)); c3.encode(buf, g3.apply(v)); c4.encode(buf, g4.apply(v));
                c5.encode(buf, g5.apply(v)); c6.encode(buf, g6.apply(v)); c7.encode(buf, g7.apply(v)); c8.encode(buf, g8.apply(v));
            }
        };
    }

    public static <B, C, T1, T2, T3, T4, T5, T6, T7, T8, T9> StreamCodec<B, C> composite(
          StreamCodec<? super B, T1> c1, Function<C, T1> g1,
          StreamCodec<? super B, T2> c2, Function<C, T2> g2,
          StreamCodec<? super B, T3> c3, Function<C, T3> g3,
          StreamCodec<? super B, T4> c4, Function<C, T4> g4,
          StreamCodec<? super B, T5> c5, Function<C, T5> g5,
          StreamCodec<? super B, T6> c6, Function<C, T6> g6,
          StreamCodec<? super B, T7> c7, Function<C, T7> g7,
          StreamCodec<? super B, T8> c8, Function<C, T8> g8,
          StreamCodec<? super B, T9> c9, Function<C, T9> g9,
          Function9<T1, T2, T3, T4, T5, T6, T7, T8, T9, C> ctor) {
        return new StreamCodec<>() {
            @Override
            public C decode(B buf) {
                return ctor.apply(c1.decode(buf), c2.decode(buf), c3.decode(buf), c4.decode(buf), c5.decode(buf), c6.decode(buf), c7.decode(buf), c8.decode(buf), c9.decode(buf));
            }

            @Override
            public void encode(B buf, C v) {
                c1.encode(buf, g1.apply(v)); c2.encode(buf, g2.apply(v)); c3.encode(buf, g3.apply(v)); c4.encode(buf, g4.apply(v));
                c5.encode(buf, g5.apply(v)); c6.encode(buf, g6.apply(v)); c7.encode(buf, g7.apply(v)); c8.encode(buf, g8.apply(v)); c9.encode(buf, g9.apply(v));
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
