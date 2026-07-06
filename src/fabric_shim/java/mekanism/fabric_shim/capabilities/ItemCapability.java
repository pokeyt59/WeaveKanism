package mekanism.fabric_shim.capabilities;

import java.util.HashMap;
import java.util.Map;
import net.fabricmc.fabric.api.lookup.v1.item.ItemApiLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * Item capability token backed by Fabric's {@link ItemApiLookup} (stand-in for NeoForge's
 * ItemCapability; same factory surface). NeoForge's {@code stack.getCapability} call sites map to
 * {@link #getCapability(ItemStack, Object)}.
 */
public final class ItemCapability<T, C> {

    private static final Map<ResourceLocation, ItemCapability<?, ?>> ALL = new HashMap<>();

    @SuppressWarnings("unchecked")
    public static synchronized <T, C> ItemCapability<T, C> create(ResourceLocation name, Class<T> typeClass, Class<C> contextClass) {
        ItemCapability<?, ?> existing = ALL.get(name);
        if (existing != null) {
            if (existing.typeClass != typeClass || existing.contextClass != contextClass) {
                throw new IllegalStateException("Item capability " + name + " already created with different type/context classes");
            }
            return (ItemCapability<T, C>) existing;
        }
        ItemCapability<T, C> capability = new ItemCapability<>(name, typeClass, contextClass);
        ALL.put(name, capability);
        return capability;
    }

    public static <T> ItemCapability<T, Void> createVoid(ResourceLocation name, Class<T> typeClass) {
        return create(name, typeClass, void.class);
    }

    private final ResourceLocation name;
    private final Class<T> typeClass;
    private final Class<C> contextClass;
    private final ItemApiLookup<T, C> lookup;

    private ItemCapability(ResourceLocation name, Class<T> typeClass, Class<C> contextClass) {
        this.name = name;
        this.typeClass = typeClass;
        this.contextClass = contextClass;
        this.lookup = ItemApiLookup.get(name, typeClass, contextClass);
    }

    public ResourceLocation name() {
        return name;
    }

    public Class<T> typeClass() {
        return typeClass;
    }

    public Class<C> contextClass() {
        return contextClass;
    }

    /**
     * The backing Fabric lookup, for provider registration (Phase 2 capability wiring).
     */
    public ItemApiLookup<T, C> lookup() {
        return lookup;
    }

    /**
     * Replacement for NeoForge's {@code stack.getCapability(cap, context)}.
     */
    @Nullable
    public T getCapability(ItemStack stack, C context) {
        return lookup.find(stack, context);
    }
}
