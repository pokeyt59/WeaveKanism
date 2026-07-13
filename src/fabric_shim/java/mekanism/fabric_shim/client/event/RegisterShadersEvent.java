package mekanism.fabric_shim.client.event;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import mekanism.fabric_shim.fml.event.IModBusEvent;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.neoforged.bus.api.Event;

/**
 * Stand-in for NeoForge's {@code RegisterShadersEvent}: collects the shaders Mekanism builds so the
 * client bootstrap can hand them to Fabric's CoreShaderRegistrationCallback (step 7 wiring — it closes
 * any instance it replaces on re-post). Carries the {@link ResourceProvider} the shader build needs.
 */
public class RegisterShadersEvent extends Event implements IModBusEvent {

    public record Registration(ShaderInstance instance, Consumer<ShaderInstance> onLoaded) {}

    private final ResourceProvider resourceProvider;
    private final List<Registration> registrations = new ArrayList<>();

    public RegisterShadersEvent(ResourceProvider resourceProvider) {
        this.resourceProvider = resourceProvider;
    }

    public ResourceProvider getResourceProvider() {
        return resourceProvider;
    }

    public void registerShader(ShaderInstance shaderInstance, Consumer<ShaderInstance> onLoaded) {
        registrations.add(new Registration(shaderInstance, onLoaded));
    }

    public List<Registration> getRegistrations() {
        return registrations;
    }
}
