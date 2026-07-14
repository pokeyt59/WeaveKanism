package mekanism.fabric_shim.client.model.pipeline;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.Map;
import mekanism.fabric_shim.client.model.IQuadTransformer;
import mekanism.fabric_shim.client.model.MekBakedQuadHooks;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;

/**
 * Same surface as net.neoforged.neoforge.client.model.pipeline.QuadBakingVertexConsumer: a
 * {@link VertexConsumer} that packs four vertices into a vanilla {@link BakedQuad}'s int[] (BLOCK format).
 * Fresh implementation — offsets/packing per {@link IQuadTransformer} / DefaultVertexFormat.BLOCK (color
 * ABGR, normal signed bytes). Vanilla BakedQuad carries no AO flag, so it is stashed in
 * {@link MekBakedQuadHooks}.
 */
public class QuadBakingVertexConsumer implements VertexConsumer {

    private static final Map<VertexFormatElement, Integer> ELEMENT_OFFSETS = new IdentityHashMap<>();

    static {
        for (VertexFormatElement element : DefaultVertexFormat.BLOCK.getElements()) {
            ELEMENT_OFFSETS.put(element, DefaultVertexFormat.BLOCK.getOffset(element) / 4);
        }
    }

    private final int[] quadData = new int[IQuadTransformer.STRIDE * 4];
    private int vertexIndex = 0;
    private boolean building = false;

    private int tintIndex = -1;
    private Direction direction = Direction.DOWN;
    private TextureAtlasSprite sprite;
    private boolean shade;
    private boolean hasAmbientOcclusion;

    @Override
    public VertexConsumer addVertex(float x, float y, float z) {
        if (building) {
            if (++vertexIndex > 4) {
                throw new IllegalStateException("Expected quad export after fourth vertex");
            }
        }
        building = true;
        int offset = vertexIndex * IQuadTransformer.STRIDE + IQuadTransformer.POSITION;
        quadData[offset] = Float.floatToRawIntBits(x);
        quadData[offset + 1] = Float.floatToRawIntBits(y);
        quadData[offset + 2] = Float.floatToRawIntBits(z);
        return this;
    }

    @Override
    public VertexConsumer setColor(int red, int green, int blue, int alpha) {
        int offset = vertexIndex * IQuadTransformer.STRIDE + IQuadTransformer.COLOR;
        quadData[offset] = ((alpha & 0xFF) << 24) | ((blue & 0xFF) << 16) | ((green & 0xFF) << 8) | (red & 0xFF);
        return this;
    }

    @Override
    public VertexConsumer setUv(float u, float v) {
        int offset = vertexIndex * IQuadTransformer.STRIDE + IQuadTransformer.UV0;
        quadData[offset] = Float.floatToRawIntBits(u);
        quadData[offset + 1] = Float.floatToRawIntBits(v);
        return this;
    }

    @Override
    public VertexConsumer setUv1(int u, int v) {
        if (IQuadTransformer.UV1 >= 0) {
            int offset = vertexIndex * IQuadTransformer.STRIDE + IQuadTransformer.UV1;
            quadData[offset] = (u & 0xFFFF) | ((v & 0xFFFF) << 16);
        }
        return this;
    }

    @Override
    public VertexConsumer setUv2(int u, int v) {
        int offset = vertexIndex * IQuadTransformer.STRIDE + IQuadTransformer.UV2;
        quadData[offset] = (u & 0xFFFF) | ((v & 0xFFFF) << 16);
        return this;
    }

    @Override
    public VertexConsumer setNormal(float x, float y, float z) {
        int offset = vertexIndex * IQuadTransformer.STRIDE + IQuadTransformer.NORMAL;
        quadData[offset] = ((int) (x * 127.0f) & 0xFF) | (((int) (y * 127.0f) & 0xFF) << 8) | (((int) (z * 127.0f) & 0xFF) << 16);
        return this;
    }

    @Override
    public VertexConsumer misc(VertexFormatElement element, int... rawData) {
        Integer baseOffset = ELEMENT_OFFSETS.get(element);
        if (baseOffset != null) {
            int offset = vertexIndex * IQuadTransformer.STRIDE + baseOffset;
            System.arraycopy(rawData, 0, quadData, offset, rawData.length);
        }
        return this;
    }

    public void setTintIndex(int tintIndex) {
        this.tintIndex = tintIndex;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public void setSprite(TextureAtlasSprite sprite) {
        this.sprite = sprite;
    }

    public void setShade(boolean shade) {
        this.shade = shade;
    }

    public void setHasAmbientOcclusion(boolean hasAmbientOcclusion) {
        this.hasAmbientOcclusion = hasAmbientOcclusion;
    }

    public BakedQuad bakeQuad() {
        if (!building || ++vertexIndex != 4) {
            throw new IllegalStateException("Not enough vertices available. Vertices in buffer: " + vertexIndex);
        }
        BakedQuad quad = new BakedQuad(quadData.clone(), tintIndex, direction, sprite, shade);
        MekBakedQuadHooks.setAmbientOcclusion(quad, hasAmbientOcclusion);
        vertexIndex = 0;
        building = false;
        Arrays.fill(quadData, 0);
        return quad;
    }
}
