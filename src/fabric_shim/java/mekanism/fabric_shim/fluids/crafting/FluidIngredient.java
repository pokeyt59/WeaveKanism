package mekanism.fabric_shim.fluids.crafting;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import mekanism.fabric_shim.common.util.NeoForgeExtraCodecs;
import mekanism.fabric_shim.fluids.FluidStack;
import mekanism.fabric_shim.fluids.FluidStackLinkedSet;
import net.minecraft.core.Holder;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.Nullable;

/**
 * Fluid analogue of Ingredient (stand-in for NeoForge's FluidIngredient; same surface and JSON
 * formats: inline {@code {"fluid"/"tag": id}}, {@code {"type": id, ...}} for custom types, or an
 * array for unions).
 *
 * <p>Type dispatch runs over a shim-internal id map (NeoForge uses a registry); network encoding
 * writes the type id as a string rather than a registry index — wire format only needs to agree
 * with this same mod on Fabric.
 */
public abstract class FluidIngredient implements Predicate<FluidStack> {

    private static final Map<ResourceLocation, FluidIngredientType<?>> TYPES = new HashMap<>();
    private static final Map<FluidIngredientType<?>, ResourceLocation> TYPE_IDS = new HashMap<>();

    /**
     * Registers a fluid ingredient type. The NeoForge built-ins register from the class initializer
     * under NeoForge's ids so JSON matches upstream.
     */
    public static synchronized void registerType(ResourceLocation id, FluidIngredientType<?> type) {
        if (TYPES.putIfAbsent(id, type) != null) {
            throw new IllegalArgumentException("Duplicate fluid ingredient type: " + id);
        }
        TYPE_IDS.put(type, id);
    }

    static {
        registerType(ResourceLocation.fromNamespaceAndPath("neoforge", "single"), SingleFluidIngredient.TYPE);
        registerType(ResourceLocation.fromNamespaceAndPath("neoforge", "tag"), TagFluidIngredient.TYPE);
        registerType(ResourceLocation.fromNamespaceAndPath("neoforge", "empty"), EmptyFluidIngredient.TYPE);
        registerType(ResourceLocation.fromNamespaceAndPath("neoforge", "compound"), CompoundFluidIngredient.TYPE);
        registerType(ResourceLocation.fromNamespaceAndPath("neoforge", "components"), DataComponentFluidIngredient.TYPE);
    }

    public static final Codec<FluidIngredientType<?>> TYPE_CODEC = ResourceLocation.CODEC.comapFlatMap(
          id -> {
              FluidIngredientType<?> type = TYPES.get(id);
              return type != null ? DataResult.success(type) : DataResult.<FluidIngredientType<?>>error(() -> "Unknown fluid ingredient type: " + id);
          },
          type -> {
              ResourceLocation id = TYPE_IDS.get(type);
              if (id == null) {
                  throw new IllegalStateException("Unregistered fluid ingredient type: " + type);
              }
              return id;
          });

    private static final MapCodec<FluidIngredient> SINGLE_OR_TAG_CODEC = MapCodec.recursive(
          "FluidIngredient.SINGLE_OR_TAG_CODEC", self -> singleOrTagCodec());

    public static final MapCodec<FluidIngredient> MAP_CODEC_NONEMPTY = makeMapCodec();
    private static final Codec<FluidIngredient> MAP_CODEC_CODEC = MAP_CODEC_NONEMPTY.codec();

    public static final Codec<List<FluidIngredient>> LIST_CODEC = MAP_CODEC_CODEC.listOf();
    public static final Codec<List<FluidIngredient>> LIST_CODEC_NON_EMPTY = LIST_CODEC.validate(list -> {
        if (list.isEmpty()) {
            return DataResult.error(() -> "Fluid ingredient cannot be empty, at least one item must be defined");
        }
        return DataResult.success(list);
    });

    public static final Codec<FluidIngredient> CODEC = codec(true);
    public static final Codec<FluidIngredient> CODEC_NON_EMPTY = codec(false);

    public static final StreamCodec<RegistryFriendlyByteBuf, FluidIngredient> STREAM_CODEC = new StreamCodec<>() {
        private static final StreamCodec<RegistryFriendlyByteBuf, FluidIngredient> DISPATCH_CODEC =
              ByteBufCodecs.fromCodec(TYPE_CODEC).<RegistryFriendlyByteBuf>cast().dispatch(FluidIngredient::getType, FluidIngredientType::streamCodec);
        private static final StreamCodec<RegistryFriendlyByteBuf, List<FluidStack>> FLUID_LIST_CODEC = FluidStack.STREAM_CODEC.apply(
              ByteBufCodecs.collection(NonNullList::createWithCapacity));

        @Override
        public void encode(RegistryFriendlyByteBuf buf, FluidIngredient ingredient) {
            if (ingredient.isSimple()) {
                FLUID_LIST_CODEC.encode(buf, Arrays.asList(ingredient.getStacks()));
            } else {
                buf.writeVarInt(-1);
                DISPATCH_CODEC.encode(buf, ingredient);
            }
        }

        @Override
        public FluidIngredient decode(RegistryFriendlyByteBuf buf) {
            int size = buf.readVarInt();
            if (size == -1) {
                return DISPATCH_CODEC.decode(buf);
            }
            return CompoundFluidIngredient.of(
                  Stream.generate(() -> FluidStack.STREAM_CODEC.decode(buf))
                        .limit(size)
                        .map(FluidIngredient::single));
        }
    };

    @Nullable
    private FluidStack[] stacks;

    public final FluidStack[] getStacks() {
        if (stacks == null) {
            stacks = generateStacks()
                  .collect(Collectors.toCollection(FluidStackLinkedSet::createTypeAndComponentsSet))
                  .toArray(FluidStack[]::new);
        }
        return stacks;
    }

    @Override
    public abstract boolean test(FluidStack fluidStack);

    protected abstract Stream<FluidStack> generateStacks();

    public abstract boolean isSimple();

    public abstract FluidIngredientType<?> getType();

    public final boolean isEmpty() {
        return this == empty();
    }

    public final boolean hasNoFluids() {
        return getStacks().length == 0;
    }

    @Override
    public abstract int hashCode();

    @Override
    public abstract boolean equals(Object obj);

    public static FluidIngredient empty() {
        return EmptyFluidIngredient.INSTANCE;
    }

    public static FluidIngredient of() {
        return empty();
    }

    public static FluidIngredient of(FluidStack... fluids) {
        return of(Arrays.stream(fluids).map(FluidStack::getFluid));
    }

    public static FluidIngredient of(Fluid... fluids) {
        return of(Arrays.stream(fluids));
    }

    private static FluidIngredient of(Stream<Fluid> fluids) {
        return CompoundFluidIngredient.of(fluids.map(FluidIngredient::single));
    }

    public static FluidIngredient single(FluidStack stack) {
        return single(stack.getFluid());
    }

    public static FluidIngredient single(Fluid fluid) {
        return single(fluid.builtInRegistryHolder());
    }

    public static FluidIngredient single(Holder<Fluid> holder) {
        return new SingleFluidIngredient(holder);
    }

    public static FluidIngredient tag(TagKey<Fluid> tag) {
        return new TagFluidIngredient(tag);
    }

    private static MapCodec<FluidIngredient> singleOrTagCodec() {
        return NeoForgeExtraCodecs.xor(SingleFluidIngredient.CODEC, TagFluidIngredient.CODEC)
              .xmap(either -> either.map(i -> i, i -> i), ingredient -> {
                  if (ingredient instanceof SingleFluidIngredient fluid) {
                      return Either.left(fluid);
                  } else if (ingredient instanceof TagFluidIngredient tag) {
                      return Either.right(tag);
                  }
                  throw new IllegalStateException("Basic fluid ingredient should be either a fluid or a tag!");
              });
    }

    private static MapCodec<FluidIngredient> makeMapCodec() {
        return NeoForgeExtraCodecs.<FluidIngredientType<?>, FluidIngredient, FluidIngredient>dispatchMapOrElse(
              TYPE_CODEC,
              FluidIngredient::getType,
              FluidIngredientType::codec,
              SINGLE_OR_TAG_CODEC
        ).xmap(either -> either.map(i -> i, i -> i), ingredient -> {
            if (ingredient instanceof SingleFluidIngredient || ingredient instanceof TagFluidIngredient) {
                return Either.right(ingredient);
            }
            return Either.left(ingredient);
        }).validate(ingredient -> {
            if (ingredient.isEmpty()) {
                return DataResult.error(() -> "Cannot serialize empty fluid ingredient using the map codec");
            }
            return DataResult.success(ingredient);
        });
    }

    private static Codec<FluidIngredient> codec(boolean allowEmpty) {
        Codec<List<FluidIngredient>> listCodec = Codec.lazyInitialized(() -> allowEmpty ? LIST_CODEC : LIST_CODEC_NON_EMPTY);
        return Codec.either(listCodec, MAP_CODEC_CODEC)
              .xmap(either -> either.map(CompoundFluidIngredient::of, i -> i),
                    ingredient -> {
                        if (ingredient instanceof CompoundFluidIngredient compound) {
                            return Either.left(compound.children());
                        } else if (ingredient.isEmpty()) {
                            return Either.left(List.of());
                        }
                        return Either.right(ingredient);
                    });
    }
}
