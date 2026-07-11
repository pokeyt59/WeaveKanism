package mekanism.fabric_shim.common.util;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Codec helpers (stand-in for NeoForge's NeoForgeExtraCodecs; only the surface Mekanism uses is
 * provided). Behavior matches NeoForge so serialized formats stay compatible.
 */
public final class NeoForgeExtraCodecs {

    private NeoForgeExtraCodecs() {
    }

    public static <T> MapCodec<T> aliasedFieldOf(final Codec<T> codec, final String... names) {
        if (names.length == 0) {
            throw new IllegalArgumentException("Must have at least one name!");
        }
        MapCodec<T> mapCodec = codec.fieldOf(names[0]);
        for (int i = 1; i < names.length; i++) {
            mapCodec = mapWithAlternative(mapCodec, codec.fieldOf(names[i]));
        }
        return mapCodec;
    }

    public static <T> MapCodec<T> mapWithAlternative(final MapCodec<T> mapCodec, final MapCodec<? extends T> alternative) {
        return Codec.mapEither(mapCodec, alternative)
              .xmap(either -> either.map(Function.identity(), Function.identity()), Either::left);
    }

    /**
     * Decodes with the primary codec, falling back to the alternative on failure; unlike
     * {@link #mapWithAlternative}, encoding also falls back to the alternative if the primary
     * cannot encode the value (matches NeoForge's withAlternative contract).
     */
    public static <T> MapCodec<T> withAlternative(final MapCodec<T> codec, final MapCodec<T> alternative) {
        return new AlternativeMapCodec<>(codec, alternative);
    }

    /**
     * Like {@link Codec#optionalFieldOf(String, Object)}, except the default value is always written.
     */
    public static <T> MapCodec<T> optionalFieldAlwaysWrite(Codec<T> codec, String name, T defaultValue) {
        return codec.optionalFieldOf(name).xmap(o -> o.orElse(defaultValue), Optional::of);
    }

    /**
     * Map codec that requires exactly one of the two alternatives to be present (matches NeoForge's
     * xor semantics: ambiguous input where both parse is an error).
     */
    public static <F, S> MapCodec<Either<F, S>> xor(MapCodec<F> first, MapCodec<S> second) {
        return new XorMapCodec<>(first, second);
    }

    /**
     * Dispatches on a {@code "type"} key when present, otherwise falls back to the given codec —
     * NeoForge's mechanism for "custom ingredient object or plain single/tag object" formats.
     */
    public static <A, E, B> MapCodec<Either<E, B>> dispatchMapOrElse(Codec<A> typeCodec, Function<? super E, ? extends A> type,
          Function<? super A, ? extends MapCodec<? extends E>> codec, MapCodec<B> fallbackCodec) {
        MapCodec<E> dispatchCodec = typeCodec.dispatchMap(type, codec);
        return new MapCodec<>() {
            @Override
            public <T> Stream<T> keys(DynamicOps<T> ops) {
                return Stream.concat(dispatchCodec.keys(ops), fallbackCodec.keys(ops)).distinct();
            }

            @Override
            public <T> DataResult<Either<E, B>> decode(DynamicOps<T> ops, MapLike<T> input) {
                if (input.get("type") != null) {
                    return dispatchCodec.decode(ops, input).map(Either::left);
                }
                return fallbackCodec.decode(ops, input).map(Either::right);
            }

            @Override
            public <T> RecordBuilder<T> encode(Either<E, B> input, DynamicOps<T> ops, RecordBuilder<T> prefix) {
                return input.map(
                      dispatched -> dispatchCodec.encode(dispatched, ops, prefix),
                      fallback -> fallbackCodec.encode(fallback, ops, prefix));
            }

            @Override
            public String toString() {
                return "DispatchOrElse[" + dispatchCodec + ", " + fallbackCodec + "]";
            }
        };
    }

    private static final class AlternativeMapCodec<T> extends MapCodec<T> {

        private final MapCodec<T> codec;
        private final MapCodec<T> alternative;

        private AlternativeMapCodec(MapCodec<T> codec, MapCodec<T> alternative) {
            this.codec = codec;
            this.alternative = alternative;
        }

        @Override
        public <O> Stream<O> keys(DynamicOps<O> ops) {
            return Stream.concat(codec.keys(ops), alternative.keys(ops)).distinct();
        }

        @Override
        public <O> DataResult<T> decode(DynamicOps<O> ops, MapLike<O> input) {
            DataResult<T> primary = codec.decode(ops, input);
            if (primary.result().isPresent()) {
                return primary;
            }
            DataResult<T> fallback = alternative.decode(ops, input);
            return fallback.result().isPresent() ? fallback : primary;
        }

        @Override
        public <O> RecordBuilder<O> encode(T input, DynamicOps<O> ops, RecordBuilder<O> prefix) {
            //Probe the primary codec against an empty builder; only fall back if it cannot encode this value
            DataResult<O> probe = codec.encode(input, ops, ops.mapBuilder()).build(ops.emptyMap());
            if (probe.result().isPresent()) {
                return codec.encode(input, ops, prefix);
            }
            return alternative.encode(input, ops, prefix);
        }

        @Override
        public String toString() {
            return "AlternativeMapCodec[" + codec + ", " + alternative + "]";
        }
    }

    private static final class XorMapCodec<F, S> extends MapCodec<Either<F, S>> {

        private final MapCodec<F> first;
        private final MapCodec<S> second;

        private XorMapCodec(MapCodec<F> first, MapCodec<S> second) {
            this.first = first;
            this.second = second;
        }

        @Override
        public <T> Stream<T> keys(DynamicOps<T> ops) {
            return Stream.concat(first.keys(ops), second.keys(ops)).distinct();
        }

        @Override
        public <T> DataResult<Either<F, S>> decode(DynamicOps<T> ops, MapLike<T> input) {
            DataResult<Either<F, S>> firstResult = first.decode(ops, input).map(Either::left);
            DataResult<Either<F, S>> secondResult = second.decode(ops, input).map(Either::right);
            if (firstResult.result().isPresent()) {
                if (secondResult.result().isPresent()) {
                    return DataResult.error(() -> "Both alternatives parsed: " + firstResult.result().get() + " and " + secondResult.result().get(),
                          firstResult.result().get());
                }
                return firstResult;
            }
            return secondResult.result().isPresent() ? secondResult : firstResult;
        }

        @Override
        public <T> RecordBuilder<T> encode(Either<F, S> input, DynamicOps<T> ops, RecordBuilder<T> prefix) {
            return input.map(f -> first.encode(f, ops, prefix), s -> second.encode(s, ops, prefix));
        }

        @Override
        public String toString() {
            return "XorMapCodec[" + first + ", " + second + "]";
        }
    }
}
