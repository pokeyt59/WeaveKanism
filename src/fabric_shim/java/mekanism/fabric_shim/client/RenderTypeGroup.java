package mekanism.fabric_shim.client;

import net.minecraft.client.renderer.RenderType;
import org.jetbrains.annotations.Nullable;

/**
 * Same surface as net.neoforged.neoforge.client.RenderTypeGroup: the block/entity/entity-fabulous
 * RenderType triple a model geometry associates with a named render-type hint. On the port these come
 * from the geometry baking context (usually EMPTY — render-type hints aren't parsed); the FRAPI emit
 * shim reads block()/entity() when a group is non-empty (client-models.md §5/6). Fresh implementation.
 */
public class RenderTypeGroup {

    public static final RenderTypeGroup EMPTY = new RenderTypeGroup(null, null, null);

    @Nullable
    private final RenderType block;
    @Nullable
    private final RenderType entity;
    @Nullable
    private final RenderType entityFabulous;

    public RenderTypeGroup(@Nullable RenderType block, @Nullable RenderType entity) {
        this(block, entity, entity);
    }

    public RenderTypeGroup(@Nullable RenderType block, @Nullable RenderType entity, @Nullable RenderType entityFabulous) {
        this.block = block;
        this.entity = entity;
        this.entityFabulous = entityFabulous;
    }

    public boolean isEmpty() {
        return block == null;
    }

    @Nullable
    public RenderType block() {
        return block;
    }

    @Nullable
    public RenderType entity() {
        return entity;
    }

    @Nullable
    public RenderType entityFabulous() {
        return entityFabulous;
    }
}
