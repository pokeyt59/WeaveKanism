package mekanism.fabric_shim.client;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.client.renderer.RenderType;

/**
 * Same surface as net.neoforged.neoforge.client.ChunkRenderTypeSet: the set of chunk RenderTypes a model
 * renders its block quads in. Mekanism builds these from a model's render-type group; the FRAPI emit shim
 * iterates the set to pick per-quad materials (client-models.md §5/6, decision C). Fresh implementation.
 */
public final class ChunkRenderTypeSet implements Iterable<RenderType> {

    private static final ChunkRenderTypeSet NONE = new ChunkRenderTypeSet(List.of());

    private final List<RenderType> renderTypes;

    private ChunkRenderTypeSet(List<RenderType> renderTypes) {
        this.renderTypes = renderTypes;
    }

    public static ChunkRenderTypeSet none() {
        return NONE;
    }

    public static ChunkRenderTypeSet of(RenderType... renderTypes) {
        List<RenderType> list = new ArrayList<>(renderTypes.length);
        for (RenderType type : renderTypes) {
            if (type != null) {
                list.add(type);
            }
        }
        return list.isEmpty() ? NONE : new ChunkRenderTypeSet(List.copyOf(list));
    }

    public boolean isEmpty() {
        return renderTypes.isEmpty();
    }

    public boolean contains(RenderType renderType) {
        return renderTypes.contains(renderType);
    }

    public List<RenderType> asList() {
        return renderTypes;
    }

    @Override
    public Iterator<RenderType> iterator() {
        return renderTypes.iterator();
    }
}
