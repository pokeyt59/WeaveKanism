package mekanism.fabric_shim.client.recipe_viewer;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import mekanism.api.recipes.basic.BasicItemStackToFluidOptionalItemRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.fabric_shim.client.gui.element.bar.IBarInfoHandler;
import mekanism.fabric_shim.client.gui.element.progress.IProgressInfoHandler;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Server-safe stand-in for {@code mekanism.client.recipe_viewer.RecipeViewerUtils}. The recipe-id
 * helpers ({@link #synthetic}) and index/current rotation are real (common code generates synthetic
 * recipe ids through here); the display helpers (bars/progress/stacks) return neutral/empty values
 * since recipe-viewer rendering is Phase 4/5.
 */
public final class RecipeViewerUtils {

    public static final IProgressInfoHandler CONSTANT_PROGRESS = () -> 1;
    public static final IBarInfoHandler FULL_BAR = () -> 1;

    private RecipeViewerUtils() {
    }

    public static IProgressInfoHandler progressHandler(int processTime) {
        return () -> 1;
    }

    public static IBarInfoHandler barProgressHandler(int processTime) {
        return () -> 1;
    }

    public static ResourceLocation synthetic(ResourceLocation id, String prefix, String namespace) {
        return synthetic(ResourceLocation.fromNamespaceAndPath(namespace, id.toString().replace(':', '_')), prefix);
    }

    public static ResourceLocation synthetic(String id, String prefix, String namespace) {
        if (id.equals("[unregistered]")) {
            return synthetic(ResourceLocation.fromNamespaceAndPath(namespace, "_unregistered_sad_face_"), prefix);
        }
        return synthetic(ResourceLocation.fromNamespaceAndPath(namespace, id.replace(':', '_')), prefix);
    }

    public static ResourceLocation synthetic(ResourceLocation id, String prefix) {
        return id.withPrefix("/" + prefix + "/");
    }

    public static <T> T getCurrent(List<T> elements) {
        return elements.get(getIndex(elements));
    }

    public static int getIndex(List<?> elements) {
        return (int) (System.currentTimeMillis() / 1000L % elements.size());
    }

    public static long getCurrent(long[] elements) {
        return elements[getIndex(elements)];
    }

    public static int getIndex(long[] elements) {
        return (int) (System.currentTimeMillis() / 1000L % elements.length);
    }

    public static List<ItemStack> getStacksFor(ChemicalStackIngredient ingredient, boolean displayConversions) {
        return Collections.emptyList();
    }

    public static Map<ResourceLocation, BasicItemStackToFluidOptionalItemRecipe> getLiquificationRecipes() {
        return Collections.emptyMap();
    }

    public static List<ItemStack> getDisplayItems(ChemicalStackIngredient ingredient) {
        return Collections.emptyList();
    }

    public static Item.TooltipContext getRVTooltipContext() {
        return Item.TooltipContext.EMPTY;
    }
}
