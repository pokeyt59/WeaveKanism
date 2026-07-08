package mekanism.fabric_shim.event.entity;

import mekanism.fabric_shim.fml.event.IModBusEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnPlacementType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.Event;
import org.jetbrains.annotations.Nullable;

/**
 * Stand-in for NeoForge's {@code RegisterSpawnPlacementsEvent} (mod bus). Compile-only: applying the
 * merged predicates to Fabric's {@code SpawnPlacements} is Phase 3, so {@link #register} currently
 * records nothing. See the hook-wiring checklist.
 */
public class RegisterSpawnPlacementsEvent extends Event implements IModBusEvent {

    /** How a mod's spawn predicate combines with the entity's existing one. */
    public enum Operation {
        AND,
        OR,
        REPLACE
    }

    public <T extends Entity> void register(EntityType<T> entityType, SpawnPlacements.SpawnPredicate<T> predicate) {
        register(entityType, null, null, predicate, Operation.AND);
    }

    public <T extends Entity> void register(EntityType<T> entityType, @Nullable SpawnPlacementType placementType,
          @Nullable Heightmap.Types heightmap, SpawnPlacements.SpawnPredicate<T> predicate, Operation operation) {
        //TODO(fabric-port, Phase 3): apply to Fabric SpawnPlacements once the event is fired
    }
}
