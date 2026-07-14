package mekanism.fabric_shim.client.model.obj;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import mekanism.fabric_shim.client.model.geometry.IGeometryLoader;

/**
 * Stub for net.neoforged.neoforge.client.model.obj.ObjLoader — hands back an empty {@link ObjModel} from
 * both the settings path (BaseModelCache) and the JSON path (an IGeometryLoader, so a
 * {@code "loader": "…obj"} model still resolves). OBJ parsing is deferred; see {@link ObjModel}.
 */
public class ObjLoader implements IGeometryLoader<ObjModel> {

    public static final ObjLoader INSTANCE = new ObjLoader();

    private ObjLoader() {
    }

    public ObjModel loadModel(ObjModel.ModelSettings settings) {
        return new ObjModel();
    }

    @Override
    public ObjModel read(JsonObject jsonObject, JsonDeserializationContext deserializationContext) {
        return new ObjModel();
    }
}
