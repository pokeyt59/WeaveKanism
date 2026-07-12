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
 * Stand-in for NeoForge's {@code RegisterSpawnPlacementsEvent} (mod bus), fired once from the
 * Fabric bootstrap after registration. Registrations apply straight to vanilla
 * {@link SpawnPlacements} (access-widened): NeoForge's operation-merging with pre-existing data
 * only matters when modifying OTHER entities' placements, which Mekanism does not do — a duplicate
 * registration is logged and skipped rather than merged.
 */
public class RegisterSpawnPlacementsEvent extends Event implements IModBusEvent {

    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger("Mekanism");

    /** How a mod's spawn predicate combines with the entity's existing one. */
    public enum Operation {
        AND,
        OR,
        REPLACE
    }

    public <T extends Entity> void register(EntityType<T> entityType, SpawnPlacements.SpawnPredicate<T> predicate) {
        register(entityType, null, null, predicate, Operation.AND);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public <T extends Entity> void register(EntityType<T> entityType, @Nullable SpawnPlacementType placementType,
          @Nullable Heightmap.Types heightmap, SpawnPlacements.SpawnPredicate<T> predicate, Operation operation) {
        SpawnPlacementType type = placementType == null ? net.minecraft.world.entity.SpawnPlacementTypes.NO_RESTRICTIONS : placementType;
        Heightmap.Types heightmapType = heightmap == null ? Heightmap.Types.MOTION_BLOCKING_NO_LEAVES : heightmap;
        try {
            //Vanilla register is Mob-bounded; the event surface is Entity-bounded like NeoForge's
            SpawnPlacements.register((EntityType) entityType, type, heightmapType, (SpawnPlacements.SpawnPredicate) predicate);
        } catch (IllegalStateException e) {
            LOGGER.warn("Spawn placement for {} was already registered; operation-merging ({}) is not supported on the Fabric port", entityType, operation);
        }
    }
}
