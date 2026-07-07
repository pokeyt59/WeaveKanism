package mekanism.fabric_shim.common;

import com.mojang.datafixers.util.Either;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;
import mekanism.fabric_shim.fluids.FluidType;
import mekanism.fabric_shim.fluids.FluidTypes;
import mekanism.fabric_shim.registries.DeferredRegister;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderOwner;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.bus.api.IEventBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Stand-in for the slice of net.neoforged.neoforge.common.NeoForgeMod that Mekanism touches.
 *
 * <p>SWIM_SPEED / CREATIVE_FLIGHT are registered as real attributes under NeoForge's ids
 * ({@code neoforge:swim_speed}, {@code neoforge:creative_flight}) so module attribute-modifier
 * NBT stays upstream-compatible. NOTE (Phase 3): registering the attribute does not give it
 * behavior — vanilla entities don't have it in their attribute maps and nothing consumes it;
 * applying it to players needs default-attribute injection + movement/ability hooks (see
 * PORTING.md residuals).
 *
 * <p>MILK: Fabric has no milk fluid; the holder is present for source compatibility, answers
 * {@code false}/empty to all matching queries, and throws if dereferenced. Usage sites
 * (fluid tank bucket interaction) get audited in Phase 2.
 */
public final class NeoForgeMod {

    private static final Logger LOGGER = LoggerFactory.getLogger("MekanismShim");
    private static final DeferredRegister<Attribute> ATTRIBUTES = DeferredRegister.create(Registries.ATTRIBUTE, "neoforge");

    public static final Holder<Attribute> SWIM_SPEED = ATTRIBUTES.register("swim_speed",
          () -> new RangedAttribute("neoforge.swim_speed", 1.0D, 0.0D, 1024.0D).setSyncable(true));
    public static final Holder<Attribute> CREATIVE_FLIGHT = ATTRIBUTES.register("creative_flight",
          () -> new RangedAttribute("neoforge.creative_flight", 0.0D, 0.0D, 1.0D).setSyncable(true));

    public static final AbsentHolder<Fluid> MILK = new AbsentHolder<>(ResourceKey.create(Registries.FLUID, ResourceLocation.fromNamespaceAndPath("neoforge", "milk")));

    //Built-in fluid types re-exported from the fluids shim so upstream identity checks against these
    //(e.g. the electrolytic breathing unit comparing WATER_TYPE.value()) keep resolving to the same
    //singletons the injected Fluid#getFluidType() hands out. See FluidTypes.
    public static final Holder<FluidType> WATER_TYPE = FluidTypes.WATER;
    public static final Holder<FluidType> LAVA_TYPE = FluidTypes.LAVA;
    public static final Holder<FluidType> EMPTY_TYPE = FluidTypes.EMPTY;

    private NeoForgeMod() {
    }

    public static void init(IEventBus modBus) {
        ATTRIBUTES.register(modBus);
    }

    public static void enableMilkFluid() {
        LOGGER.debug("enableMilkFluid() requested; the Fabric port has no milk fluid (no-op)");
    }

    public record AbsentHolder<T>(ResourceKey<T> key) implements Holder<T>, Supplier<T> {

        @Override
        public T value() {
            throw new IllegalStateException(key + " is not available on the Fabric port");
        }

        @Override
        public T get() {
            return value();
        }

        @Override
        public boolean isBound() {
            return false;
        }

        @Override
        public boolean is(ResourceLocation location) {
            return key.location().equals(location);
        }

        @Override
        public boolean is(ResourceKey<T> resourceKey) {
            return key == resourceKey;
        }

        @Override
        public boolean is(Predicate<ResourceKey<T>> predicate) {
            return predicate.test(key);
        }

        @Override
        public boolean is(TagKey<T> tagKey) {
            return false;
        }

        @Override
        public boolean is(Holder<T> holder) {
            return holder == this;
        }

        @Override
        public Stream<TagKey<T>> tags() {
            return Stream.empty();
        }

        @Override
        public Either<ResourceKey<T>, T> unwrap() {
            return Either.left(key);
        }

        @Override
        public Optional<ResourceKey<T>> unwrapKey() {
            return Optional.of(key);
        }

        @Override
        public Kind kind() {
            return Kind.REFERENCE;
        }

        @Override
        public boolean canSerializeIn(HolderOwner<T> owner) {
            return false;
        }
    }
}
