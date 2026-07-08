package mekanism.fabric_shim.integration.computer;

import java.util.function.BooleanSupplier;
import mekanism.common.integration.computer.IComputerTile;
import mekanism.common.registration.impl.TileEntityTypeDeferredRegister.BlockEntityTypeBuilder;
import mekanism.common.tile.base.CapabilityTileEntity;
import mekanism.fabric_shim.capabilities.RegisterCapabilitiesEvent;

/**
 * Server-safe stand-in for {@code mekanism.common.integration.computer.ComputerCapabilityHelper}. The
 * real one wires ComputerCraft / OpenComputers2 capabilities (Phase 5, and those bindings are
 * excluded); here it is a no-op so tile registration compiles.
 */
public final class ComputerCapabilityHelper {

    private ComputerCapabilityHelper() {
    }

    public static <TILE extends CapabilityTileEntity & IComputerTile> void addComputerCapabilities(BlockEntityTypeBuilder<TILE> builder,
          BooleanSupplier supportsComputer) {
    }

    public static void addBoundingComputerCapabilities(RegisterCapabilitiesEvent event) {
    }
}
