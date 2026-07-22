package mekanism.client.render;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.io.IOException;
import java.util.function.Supplier;
import mekanism.common.Mekanism;
import net.minecraft.client.renderer.RenderStateShard.ShaderStateShard;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import mekanism.fabric_shim.distmarker.Dist;
import net.fabricmc.fabric.impl.client.rendering.FabricShaderProgram;
import net.neoforged.bus.api.SubscribeEvent;
import mekanism.fabric_shim.fml.common.EventBusSubscriber;
import mekanism.fabric_shim.client.event.RegisterShadersEvent;

@EventBusSubscriber(modid = Mekanism.MODID, value = Dist.CLIENT)
public class MekanismShaders {

    static final ShaderTracker MEKASUIT = new ShaderTracker();
    //Merge of position_color_tex and rendertype_lightning
    static final ShaderTracker SPS = new ShaderTracker();
    //Copy of position_color_tex with support for fog
    static final ShaderTracker FLAME = new ShaderTracker();

    @SubscribeEvent
    public static void shaderRegistry(RegisterShadersEvent event) throws IOException {
        registerShader(event, Mekanism.rl("rendertype_flame"), DefaultVertexFormat.POSITION_TEX_COLOR, FLAME);
        registerShader(event, Mekanism.rl("rendertype_mekasuit"), DefaultVertexFormat.NEW_ENTITY, MEKASUIT);
        registerShader(event, Mekanism.rl("rendertype_sps"), DefaultVertexFormat.POSITION_TEX_COLOR, SPS);
    }

    private static void registerShader(RegisterShadersEvent event, ResourceLocation shaderLocation, VertexFormat vertexFormat, ShaderTracker tracker) throws IOException {
        //fabric-port: NeoForge patches a ResourceLocation ctor onto ShaderInstance; vanilla's String
        // ctor forces the minecraft namespace. FabricShaderProgram is the subclass Fabric's own core
        // shader registration constructs — fabric-rendering-v1's ShaderProgramMixin only rewrites
        // namespaced ids for instances of it.
        event.registerShader(new FabricShaderProgram(event.getResourceProvider(), shaderLocation, vertexFormat), tracker::setInstance);
    }

    static class ShaderTracker implements Supplier<ShaderInstance> {

        private ShaderInstance instance;
        final ShaderStateShard shard = new ShaderStateShard(this);

        private ShaderTracker() {
        }

        private void setInstance(ShaderInstance instance) {
            this.instance = instance;
        }

        @Override
        public ShaderInstance get() {
            return instance;
        }
    }
}