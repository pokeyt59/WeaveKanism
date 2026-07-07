package mekanism.fabric_test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import io.netty.buffer.Unpooled;
import mekanism.fabric_shim.fluids.FluidStack;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.level.material.Fluids;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * GOLDEN WIRE-FORMAT TESTS. These lock the shim FluidStack's serialized forms to NeoForge's exact
 * formats so recipe JSON, saved NBT and network payloads stay compatible with upstream. If one of
 * these fails after a change, the change is wrong — do NOT update the expected values without
 * comparing against NeoForge's own FluidStack serialization.
 */
class FluidStackWireFormatTest {

    @BeforeAll
    static void bootstrap() {
        McBootstrap.ensure();
    }

    @Test
    @DisplayName("CODEC encodes to NeoForge's field layout: {id, amount}")
    void codecGoldenJson() {
        FluidStack stack = new FluidStack(Fluids.WATER, 1000);
        JsonElement encoded = FluidStack.CODEC.encodeStart(JsonOps.INSTANCE, stack).getOrThrow();
        JsonElement expected = JsonParser.parseString("{\"id\":\"minecraft:water\",\"amount\":1000}");
        assertEquals(expected, encoded);
    }

    @Test
    @DisplayName("OPTIONAL_CODEC encodes empty as {}")
    void optionalCodecEmpty() {
        JsonElement encoded = FluidStack.OPTIONAL_CODEC.encodeStart(JsonOps.INSTANCE, FluidStack.EMPTY).getOrThrow();
        JsonElement expected = JsonParser.parseString("{}");
        assertEquals(expected, encoded);
    }

    @Test
    @DisplayName("CODEC decodes NeoForge-format JSON")
    void codecDecodes() {
        FluidStack decoded = FluidStack.CODEC.parse(JsonOps.INSTANCE,
              JsonParser.parseString("{\"id\":\"minecraft:lava\",\"amount\":250}")).getOrThrow();
        assertTrue(decoded.is(Fluids.LAVA));
        assertEquals(250, decoded.getAmount());
    }

    @Test
    @DisplayName("STREAM_CODEC round-trips fluid, amount and components")
    void streamCodecRoundTrip() {
        RegistryAccess access = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
        RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(Unpooled.buffer(), access);
        FluidStack original = new FluidStack(Fluids.WATER, 12_345);
        FluidStack.STREAM_CODEC.encode(buf, original);
        FluidStack decoded = FluidStack.STREAM_CODEC.decode(buf);
        assertTrue(FluidStack.isSameFluidSameComponents(original, decoded));
        assertEquals(original.getAmount(), decoded.getAmount());
        assertEquals(0, buf.readableBytes(), "stream codec must consume exactly what it wrote");
    }
}
