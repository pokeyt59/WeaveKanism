package mekanism.fabric_shim.common.crafting;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * Codec pair for a custom ingredient implementation (stand-in for NeoForge's IngredientType; same
 * surface). Types are registered through {@link CustomIngredients}, which also bridges them into
 * Fabric's custom-ingredient system.
 */
public record IngredientType<T extends ICustomIngredient>(MapCodec<T> codec, StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec) {

    public IngredientType(MapCodec<T> codec) {
        this(codec, ByteBufCodecs.fromCodecWithRegistries(codec.codec()));
    }
}
