package mekanism.fabric_shim.fluids.crafting;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * Codec pair for a fluid ingredient implementation (stand-in for NeoForge's FluidIngredientType;
 * same surface). Types register through {@link FluidIngredient#registerType}.
 */
public record FluidIngredientType<T extends FluidIngredient>(MapCodec<T> codec, StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec) {

    public FluidIngredientType(MapCodec<T> mapCodec) {
        this(mapCodec, ByteBufCodecs.fromCodecWithRegistries(mapCodec.codec()));
    }
}
