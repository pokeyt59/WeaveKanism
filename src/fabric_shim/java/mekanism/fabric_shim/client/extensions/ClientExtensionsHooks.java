package mekanism.fabric_shim.client.extensions;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import mekanism.fabric_shim.client.IItemDecorator;
import mekanism.fabric_shim.fluids.FluidType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

/**
 * Registry behind the client-extensions shim surface: what NeoForge's ClientExtensionsManager
 * holds (item/block/fluid-type extension maps) plus the item-decorator table. Populated by the
 * {@code RegisterClientExtensionsEvent}/{@code RegisterItemDecorationsEvent} shims during client
 * init (single-threaded), read from render paths afterwards.
 *
 * <p>Runtime consumption status: {@code IClientItemExtensions#getCustomRenderer} feeds the BEWLR
 * bridge, fluid-type extensions feed the FluidRenderHandler bridge, block particle extensions and
 * item decorators need their ParticleEngine/GuiGraphics hooks — all wired in the client
 * bootstrap/mixin steps of the Phase 4 plan (client-compile-plan.md).
 */
public final class ClientExtensionsHooks {

    static final Map<Item, IClientItemExtensions> ITEM_EXTENSIONS = new IdentityHashMap<>();
    static final Map<Block, IClientBlockExtensions> BLOCK_EXTENSIONS = new IdentityHashMap<>();
    static final Map<FluidType, IClientFluidTypeExtensions> FLUID_TYPE_EXTENSIONS = new IdentityHashMap<>();
    private static final Map<Item, List<IItemDecorator>> ITEM_DECORATORS = new IdentityHashMap<>();

    private ClientExtensionsHooks() {
    }

    public static void registerItem(IClientItemExtensions extensions, Item item) {
        ITEM_EXTENSIONS.putIfAbsent(item, extensions);
    }

    public static void registerBlock(IClientBlockExtensions extensions, Block block) {
        BLOCK_EXTENSIONS.putIfAbsent(block, extensions);
    }

    public static void registerFluidType(IClientFluidTypeExtensions extensions, FluidType fluidType) {
        FLUID_TYPE_EXTENSIONS.putIfAbsent(fluidType, extensions);
    }

    public static void registerDecorator(Item item, IItemDecorator decorator) {
        ITEM_DECORATORS.computeIfAbsent(item, i -> new ArrayList<>()).add(decorator);
    }

    public static List<IItemDecorator> getDecorators(Item item) {
        return ITEM_DECORATORS.getOrDefault(item, List.of());
    }

    public static Map<Item, IClientItemExtensions> itemExtensions() {
        return ITEM_EXTENSIONS;
    }

    public static Map<FluidType, IClientFluidTypeExtensions> fluidTypeExtensions() {
        return FLUID_TYPE_EXTENSIONS;
    }
}
