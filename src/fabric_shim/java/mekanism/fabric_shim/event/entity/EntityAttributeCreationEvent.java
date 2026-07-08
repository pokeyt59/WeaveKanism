package mekanism.fabric_shim.event.entity;

import java.util.HashMap;
import java.util.Map;
import mekanism.fabric_shim.fml.event.IModBusEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.neoforged.bus.api.Event;

/**
 * Stand-in for NeoForge's {@code EntityAttributeCreationEvent} (mod bus). Collects per-entity
 * attribute suppliers; firing + applying them to Fabric's {@code FabricDefaultAttributeRegistry} is
 * Phase 3. Compile-only for now.
 */
public class EntityAttributeCreationEvent extends Event implements IModBusEvent {

    private final Map<EntityType<? extends LivingEntity>, AttributeSupplier> map;

    public EntityAttributeCreationEvent(Map<EntityType<? extends LivingEntity>, AttributeSupplier> map) {
        this.map = map;
    }

    public EntityAttributeCreationEvent() {
        this(new HashMap<>());
    }

    public void put(EntityType<? extends LivingEntity> entity, AttributeSupplier map) {
        this.map.put(entity, map);
    }

    public Map<EntityType<? extends LivingEntity>, AttributeSupplier> getAttributes() {
        return this.map;
    }
}
