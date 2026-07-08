package mekanism.fabric_shim.entity;

import net.minecraft.world.entity.Entity;

/**
 * Stand-in for NeoForge's {@code PartEntity} (multipart entity sub-part). Mekanism only uses it as an
 * {@code instanceof} marker (teleporter eligibility). Abstract and never instantiated by the port, so
 * the vanilla {@link Entity} abstract members stay unimplemented here.
 */
public abstract class PartEntity<T extends Entity> extends Entity {

    private final T parent;

    public PartEntity(T parent) {
        super(parent.getType(), parent.level());
        this.parent = parent;
    }

    public T getParent() {
        return this.parent;
    }
}
