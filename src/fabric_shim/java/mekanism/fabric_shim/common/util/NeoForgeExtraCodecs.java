package mekanism.fabric_shim.common.util;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.util.Optional;
import java.util.function.Function;

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
     * Like {@link Codec#optionalFieldOf(String, Object)}, except the default value is always written.
     */
    public static <T> MapCodec<T> optionalFieldAlwaysWrite(Codec<T> codec, String name, T defaultValue) {
        return codec.optionalFieldOf(name).xmap(o -> o.orElse(defaultValue), Optional::of);
    }
}
