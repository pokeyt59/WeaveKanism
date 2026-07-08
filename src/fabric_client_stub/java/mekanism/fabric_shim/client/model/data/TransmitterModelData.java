package mekanism.fabric_shim.client.model.data;

import mekanism.common.lib.transmitter.ConnectionType;
import net.minecraft.core.Direction;

/**
 * Server-safe stand-in for {@code mekanism.client.model.data.TransmitterModelData}. Common transmitter
 * tiles build one and stash connection/colour flags for the Phase 4 model renderer to read; the
 * setters just record nothing here. Lives in the portMain source set (references src/main
 * {@code ConnectionType}).
 */
public class TransmitterModelData {

    public void setConnectionData(Direction direction, ConnectionType connectionType) {
    }

    public void setHasColor(boolean hasColor) {
    }

    public static final class Diversion extends TransmitterModelData {

        @Override
        public void setHasColor(boolean hasColor) {
        }
    }
}
