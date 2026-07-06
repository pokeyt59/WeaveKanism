package mekanism.fabric_shim.common.crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredient;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredientSerializer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import mekanism.fabric_shim.common.util.NeoForgeExtraCodecs;
import org.jetbrains.annotations.Nullable;

/**
 * Internal to the Fabric port: registry of shim {@link IngredientType}s, the NeoForge-format
 * ingredient codecs built on top of it, and the bridge into Fabric API's custom-ingredient system.
 *
 * <p>NeoForge patches vanilla Ingredient to carry custom logic and adds map/list codecs to it; on
 * this port those live here instead. JSON formats match NeoForge ({@code {"item"/"tag": ...}}
 * inline, or {@code {"type": "<id>", ...}} for custom ingredients) for everything flowing through
 * Mekanism's own recipe codecs. Vanilla recipe slots use Fabric's format for custom ingredients —
 * upstream datagen JSON that puts NeoForge custom ingredients in vanilla recipes is audited in
 * Phase 6 (see PORTING.md).
 */
public final class CustomIngredients {

    private CustomIngredients() {
    }

    private static final Map<ResourceLocation, IngredientType<?>> TYPES = new HashMap<>();
    private static final Map<IngredientType<?>, ResourceLocation> IDS = new HashMap<>();
    private static final Map<IngredientType<?>, CustomIngredientSerializer<FabricAdapter>> SERIALIZERS = new HashMap<>();

    /**
     * Codec for ingredient type ids, used for {@code "type"}-dispatch in the map codec.
     */
    public static final Codec<IngredientType<?>> TYPE_CODEC = ResourceLocation.CODEC.comapFlatMap(
          id -> {
              IngredientType<?> type = TYPES.get(id);
              return type != null ? DataResult.success(type) : DataResult.<IngredientType<?>>error(() -> "Unknown ingredient type: " + id);
          },
          type -> {
              ResourceLocation id = IDS.get(type);
              if (id == null) {
                  throw new IllegalStateException("Unregistered ingredient type: " + type);
              }
              return id;
          });

    /**
     * Single {@code {"item": id}} or {@code {"tag": id}} object — the inline format vanilla-style
     * ingredients take inside NeoForge's flat codecs. Encoding requires a single-value ingredient;
     * multi-value vanilla ingredients cannot be flattened (matches NeoForge).
     */
    private static final MapCodec<Ingredient> VANILLA_SINGLE_OR_TAG = NeoForgeExtraCodecs.xor(
          BuiltInRegistries.ITEM.holderByNameCodec().fieldOf("item"),
          TagKey.codec(Registries.ITEM).fieldOf("tag")
    ).flatXmap(
          either -> DataResult.success(either.map(holder -> Ingredient.of(holder.value()), Ingredient::of)),
          CustomIngredients::flattenVanilla);

    /**
     * NeoForge's Ingredient.MAP_CODEC_NONEMPTY equivalent: custom ingredients dispatch on
     * {@code "type"}, otherwise inline single item/tag.
     */
    public static final MapCodec<Ingredient> MAP_CODEC_NONEMPTY = NeoForgeExtraCodecs.<IngredientType<?>, Ingredient, Ingredient>dispatchMapOrElse(
          TYPE_CODEC,
          ingredient -> {
              ICustomIngredient custom = getCustom(ingredient);
              if (custom == null) {
                  throw new IllegalStateException("Cannot type-dispatch a vanilla ingredient");
              }
              return custom.getType();
          },
          type -> customMapCodec(type),
          VANILLA_SINGLE_OR_TAG
    ).xmap(either -> either.map(i -> i, i -> i), ingredient -> getCustom(ingredient) != null
                                                             ? com.mojang.datafixers.util.Either.left(ingredient)
                                                             : com.mojang.datafixers.util.Either.right(ingredient));

    /**
     * NeoForge's patched Ingredient.CODEC_NONEMPTY equivalent: an array of ingredient objects or a
     * single ingredient object.
     */
    public static final Codec<Ingredient> INGREDIENT_CODEC_NONEMPTY = Codec.lazyInitialized(() -> {
        Codec<Ingredient> mapCodec = MAP_CODEC_NONEMPTY.codec();
        return Codec.either(mapCodec.listOf(), mapCodec).xmap(
              either -> either.map(CompoundIngredient::of, i -> i),
              ingredient -> getCustom(ingredient) instanceof CompoundIngredient compound
                            ? com.mojang.datafixers.util.Either.left(compound.children())
                            : com.mojang.datafixers.util.Either.right(ingredient));
    });

    /**
     * NeoForge's patched Ingredient.LIST_CODEC_NONEMPTY equivalent.
     */
    public static final Codec<List<Ingredient>> LIST_CODEC_NONEMPTY = MAP_CODEC_NONEMPTY.codec().listOf()
          .validate(list -> list.isEmpty() ? DataResult.error(() -> "Ingredient list cannot be empty") : DataResult.success(list));

    @SuppressWarnings("unchecked")
    private static MapCodec<? extends Ingredient> customMapCodec(IngredientType<?> type) {
        return ((IngredientType<ICustomIngredient>) type).codec().xmap(ICustomIngredient::toVanilla, ingredient -> {
            ICustomIngredient custom = getCustom(ingredient);
            if (custom == null) {
                throw new IllegalStateException("Expected a custom ingredient");
            }
            return custom;
        });
    }

    private static DataResult<com.mojang.datafixers.util.Either<net.minecraft.core.Holder<net.minecraft.world.item.Item>, TagKey<net.minecraft.world.item.Item>>> flattenVanilla(Ingredient ingredient) {
        //Encode support is limited to single-item vanilla ingredients: vanilla 1.21.1 keeps its
        // values array private on Fabric, so tags/multi-value ingredients cannot be introspected.
        // Decode (the runtime-critical path) is unaffected. Datagen runs on the NeoForge branch.
        ItemStack[] items = ingredient.getItems();
        if (items.length == 1) {
            return DataResult.success(com.mojang.datafixers.util.Either.left(items[0].getItemHolder()));
        }
        return DataResult.error(() -> "Cannot flatten a multi-value or tag vanilla ingredient for encoding on the Fabric port");
    }

    /**
     * Registers a shim ingredient type under the given id and bridges it into Fabric's
     * custom-ingredient system so these ingredients also work in vanilla recipe slots and sync.
     */
    public static synchronized <T extends ICustomIngredient> void register(ResourceLocation id, IngredientType<T> type) {
        if (TYPES.putIfAbsent(id, type) != null) {
            throw new IllegalArgumentException("Duplicate ingredient type: " + id);
        }
        IDS.put(type, id);
        CustomIngredientSerializer<FabricAdapter> serializer = new FabricSerializer<>(id, type);
        SERIALIZERS.put(type, serializer);
        CustomIngredientSerializer.register(serializer);
    }

    /**
     * Registers the NeoForge built-in ingredient types Mekanism uses, under NeoForge's ids so
     * Mekanism-pipeline recipe JSON matches upstream. Called once from the Fabric bootstrap.
     */
    public static void registerBuiltins() {
        register(ResourceLocation.fromNamespaceAndPath("neoforge", "compound"), CompoundIngredient.TYPE);
        register(ResourceLocation.fromNamespaceAndPath("neoforge", "difference"), DifferenceIngredient.TYPE);
        register(ResourceLocation.fromNamespaceAndPath("neoforge", "components"), DataComponentIngredient.TYPE);
    }

    /**
     * Extracts the shim custom ingredient from a vanilla Ingredient, or null if it is plain vanilla
     * (replacement for NeoForge's patched {@code Ingredient#getCustomIngredient()}).
     */
    @Nullable
    public static ICustomIngredient getCustom(Ingredient ingredient) {
        return ingredient.getCustomIngredient() instanceof FabricAdapter adapter ? adapter.wrapped : null;
    }

    /**
     * Replacement for NeoForge's patched {@code Ingredient#isSimple()}.
     */
    public static boolean isSimple(Ingredient ingredient) {
        ICustomIngredient custom = getCustom(ingredient);
        return custom == null ? ingredient.getCustomIngredient() == null : custom.isSimple();
    }

    /**
     * Replacement for NeoForge's patched {@code Ingredient#hasNoItems()}.
     */
    public static boolean hasNoItems(Ingredient ingredient) {
        return ingredient.getItems().length == 0;
    }

    static Ingredient toVanilla(ICustomIngredient custom) {
        return new FabricAdapter(custom).toVanilla();
    }

    /**
     * Bridges a shim custom ingredient into Fabric's custom-ingredient system.
     */
    static final class FabricAdapter implements CustomIngredient {

        final ICustomIngredient wrapped;

        FabricAdapter(ICustomIngredient wrapped) {
            this.wrapped = wrapped;
        }

        @Override
        public boolean test(ItemStack stack) {
            return wrapped.test(stack);
        }

        @Override
        public List<ItemStack> getMatchingStacks() {
            return wrapped.getItems().toList();
        }

        @Override
        public boolean requiresTesting() {
            return !wrapped.isSimple();
        }

        @Override
        public CustomIngredientSerializer<?> getSerializer() {
            CustomIngredientSerializer<FabricAdapter> serializer = SERIALIZERS.get(wrapped.getType());
            if (serializer == null) {
                throw new IllegalStateException("Ingredient type not registered with CustomIngredients: " + wrapped.getType());
            }
            return serializer;
        }
    }

    private record FabricSerializer<T extends ICustomIngredient>(ResourceLocation id, IngredientType<T> type)
          implements CustomIngredientSerializer<FabricAdapter> {

        @Override
        public ResourceLocation getIdentifier() {
            return id;
        }

        @Override
        @SuppressWarnings("unchecked")
        public MapCodec<FabricAdapter> getCodec(boolean allowEmpty) {
            return type.codec().xmap(FabricAdapter::new, adapter -> (T) adapter.wrapped);
        }

        @Override
        @SuppressWarnings("unchecked")
        public StreamCodec<RegistryFriendlyByteBuf, FabricAdapter> getPacketCodec() {
            return StreamCodec.of(
                  (buf, adapter) -> ((StreamCodec<RegistryFriendlyByteBuf, T>) type.streamCodec()).encode(buf, (T) adapter.wrapped),
                  buf -> new FabricAdapter(((StreamCodec<RegistryFriendlyByteBuf, T>) type.streamCodec()).decode(buf)));
        }
    }
}
