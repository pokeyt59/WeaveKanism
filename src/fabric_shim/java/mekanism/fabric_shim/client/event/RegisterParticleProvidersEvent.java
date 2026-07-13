package mekanism.fabric_shim.client.event;

import mekanism.fabric_shim.fml.event.IModBusEvent;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.neoforged.bus.api.Event;

/**
 * Stand-in for NeoForge's {@code RegisterParticleProvidersEvent} (the sprite-set slice Mekanism uses):
 * registrations apply into Fabric's {@link ParticleFactoryRegistry}. Vanilla's SpriteParticleRegistration
 * takes a SpriteSet, which Fabric's FabricSpriteProvider extends, so the {@code create} ref adapts
 * directly. Posted from the client bootstrap in NeoForge's client-init order.
 */
public class RegisterParticleProvidersEvent extends Event implements IModBusEvent {

    public <T extends ParticleOptions> void registerSpriteSet(ParticleType<T> type, ParticleEngine.SpriteParticleRegistration<T> registration) {
        ParticleFactoryRegistry.getInstance().register(type, registration::create);
    }
}
