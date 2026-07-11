package mekanism.common.network.to_client.configuration;

import java.util.function.Consumer;
import mekanism.common.Mekanism;
import mekanism.common.network.to_client.security.PacketBatchSecurityUpdate;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.configuration.ServerConfigurationPacketListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.network.ServerConfigurationPacketListenerImpl;
import mekanism.fabric_shim.network.configuration.ICustomConfigurationTask;
import org.jetbrains.annotations.NotNull;

public record SyncAllSecurityData(ServerConfigurationPacketListener listener) implements ICustomConfigurationTask {

    private static final ResourceLocation ID = Mekanism.rl("sync_security");
    //fabric-port: vanilla ConfigurationTask.Type takes a String (NeoForge's ResourceLocation overload just toStrings it)
    private static final Type TYPE = new Type(ID.toString());

    public void run(Consumer<CustomPacketPayload> sender) {
        sender.accept(new PacketBatchSecurityUpdate());
        //fabric-port: finishCurrentTask is on the impl class, not the vanilla interface (NeoForge patches it in)
        ((ServerConfigurationPacketListenerImpl) listener()).finishCurrentTask(type());
    }

    @NotNull
    public Type type() {
        return TYPE;
    }
}