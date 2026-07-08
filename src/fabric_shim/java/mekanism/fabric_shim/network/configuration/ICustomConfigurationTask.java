package mekanism.fabric_shim.network.configuration;

import java.util.function.Consumer;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.network.ConfigurationTask;

/**
 * Stand-in for NeoForge's {@code ICustomConfigurationTask}: a vanilla {@link ConfigurationTask} that
 * emits {@link CustomPacketPayload}s instead of raw packets. Same surface as upstream.
 */
public interface ICustomConfigurationTask extends ConfigurationTask {

    void run(Consumer<CustomPacketPayload> sender);

    @Override
    default void start(Consumer<Packet<?>> sender) {
        run(payload -> sender.accept(new ClientboundCustomPayloadPacket(payload)));
    }
}
