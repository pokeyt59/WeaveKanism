package mekanism.fabric_shim.client.model.geometry;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

/**
 * Same surface as net.neoforged.neoforge.client.model.geometry.IGeometryLoader. Loaders register
 * through {@code ModelEvent.RegisterGeometryLoaders} into {@code ClientModelHooks}; how the bake
 * pipeline invokes them on Fabric is the step-4 model design (client-models.md) — this interface
 * exists first so registration code compiles.
 */
@FunctionalInterface
public interface IGeometryLoader<T> {

    T read(JsonObject jsonObject, JsonDeserializationContext deserializationContext) throws JsonParseException;
}
