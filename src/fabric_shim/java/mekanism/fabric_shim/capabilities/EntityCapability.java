package mekanism.fabric_shim.capabilities;

import java.util.HashMap;
import java.util.Map;
import net.fabricmc.fabric.api.lookup.v1.entity.EntityApiLookup;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

/**
 * Entity capability token backed by Fabric's {@link EntityApiLookup} (stand-in for NeoForge's
 * EntityCapability; same factory surface). NeoForge's {@code entity.getCapability} call sites map
 * to {@link #getCapability(Entity, Object)}.
 */
public final class EntityCapability<T, C> {

    private static final Map<ResourceLocation, EntityCapability<?, ?>> ALL = new HashMap<>();

    @SuppressWarnings("unchecked")
    public static synchronized <T, C> EntityCapability<T, C> create(ResourceLocation name, Class<T> typeClass, Class<C> contextClass) {
        EntityCapability<?, ?> existing = ALL.get(name);
        if (existing != null) {
            if (existing.typeClass != typeClass || existing.contextClass != contextClass) {
                throw new IllegalStateException("Entity capability " + name + " already created with different type/context classes");
            }
            return (EntityCapability<T, C>) existing;
        }
        EntityCapability<T, C> capability = new EntityCapability<>(name, typeClass, contextClass);
        ALL.put(name, capability);
        return capability;
    }

    public static <T> EntityCapability<T, Void> createVoid(ResourceLocation name, Class<T> typeClass) {
        return create(name, typeClass, void.class);
    }

    public static <T> EntityCapability<T, @Nullable Direction> createSided(ResourceLocation name, Class<T> typeClass) {
        return create(name, typeClass, Direction.class);
    }

    private final ResourceLocation name;
    private final Class<T> typeClass;
    private final Class<C> contextClass;
    private final EntityApiLookup<T, C> lookup;

    private EntityCapability(ResourceLocation name, Class<T> typeClass, Class<C> contextClass) {
        this.name = name;
        this.typeClass = typeClass;
        this.contextClass = contextClass;
        this.lookup = EntityApiLookup.get(name, typeClass, contextClass);
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
    public EntityApiLookup<T, C> lookup() {
        return lookup;
    }

    /**
     * Replacement for NeoForge's {@code entity.getCapability(cap, context)}.
     */
    @Nullable
    public T getCapability(Entity entity, C context) {
        return lookup.find(entity, context);
    }
}
