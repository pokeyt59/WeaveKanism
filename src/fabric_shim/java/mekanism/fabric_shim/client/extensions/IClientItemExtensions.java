package mekanism.fabric_shim.client.extensions;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Same surface as net.neoforged.neoforge.client.extensions.common.IClientItemExtensions (the slice
 * Mekanism uses: the custom-BEWLR hook and per-item resolution). Registered through
 * {@code RegisterClientExtensionsEvent}; the client bootstrap bridges
 * {@link #getCustomRenderer()} into Fabric's BuiltinItemRendererRegistry.
 */
public interface IClientItemExtensions {

    IClientItemExtensions DEFAULT = new IClientItemExtensions() {
    };

    static IClientItemExtensions of(ItemStack stack) {
        return of(stack.getItem());
    }

    static IClientItemExtensions of(Item item) {
        return ClientExtensionsHooks.ITEM_EXTENSIONS.getOrDefault(item, DEFAULT);
    }

    default BlockEntityWithoutLevelRenderer getCustomRenderer() {
        //Vanilla's shared BEWLR; the field is private with no accessor (AW'd)
        return Minecraft.getInstance().getItemRenderer().blockEntityRenderer;
    }
}
