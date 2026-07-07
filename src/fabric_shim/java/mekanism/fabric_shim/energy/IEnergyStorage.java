package mekanism.fabric_shim.energy;

/**
 * Same surface as net.neoforged.neoforge.energy.IEnergyStorage (Forge Energy). On Fabric no mod
 * exposes this interface at runtime — it exists so Mekanism's ForgeEnergyCompat plumbing compiles;
 * the live external-energy interop is the team-reborn adapter (Phase 2, see
 * fabric-port/design/transfer-bridge.md).
 */
public interface IEnergyStorage {

    int receiveEnergy(int toReceive, boolean simulate);

    int extractEnergy(int toExtract, boolean simulate);

    int getEnergyStored();

    int getMaxEnergyStored();

    boolean canExtract();

    boolean canReceive();
}
