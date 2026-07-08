package mekanism.fabric_shim.inject;

import java.util.stream.Stream;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;

/**
 * NeoForge patches {@code EntityType} with {@code getTags()} (the tags the entity type is in).
 * Injected + EntityTypeMixin; routes through the entity-type registry holder.
 */
public interface MekEntityTypeExt {

    @SuppressWarnings("unchecked")
    default Stream<TagKey<EntityType<?>>> getTags() {
        return BuiltInRegistries.ENTITY_TYPE.wrapAsHolder((EntityType<?>) this).tags()
              .map(tag -> (TagKey<EntityType<?>>) (Object) tag);
    }
}
