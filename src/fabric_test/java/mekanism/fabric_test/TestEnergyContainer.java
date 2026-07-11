package mekanism.fabric_test;

import mekanism.api.energy.IEnergyContainer;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;

/**
 * Minimal in-memory IEnergyContainer for bridge tests — the abstract subset only; insert/extract
 * come from the interface defaults, i.e. the exact logic Mekanism containers run in production.
 */
public class TestEnergyContainer implements IEnergyContainer {

    private final long maxEnergy;
    private long energy;
    public int contentsChangedCount;

    public TestEnergyContainer(long maxEnergy) {
        this.maxEnergy = maxEnergy;
    }

    @Override
    public long getEnergy() {
        return energy;
    }

    @Override
    public void setEnergy(long energy) {
        this.energy = energy;
        onContentsChanged();
    }

    @Override
    public long getMaxEnergy() {
        return maxEnergy;
    }

    @Override
    public void onContentsChanged() {
        contentsChangedCount++;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
        //Not exercised by bridge tests
    }
}
