package mekanism.fabric_test;

import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.fabric_shim.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

/**
 * Minimal in-memory IExtendedFluidTank for bridge tests — the abstract subset only; insert/extract
 * come from the interface defaults, i.e. the exact logic Mekanism tanks run in production.
 */
public class TestFluidTank implements IExtendedFluidTank {

    private final int capacity;
    private FluidStack stack = FluidStack.EMPTY;
    public int contentsChangedCount;

    public TestFluidTank(int capacity) {
        this.capacity = capacity;
    }

    @NotNull
    @Override
    public FluidStack getFluid() {
        return stack;
    }

    @Override
    public void setStack(FluidStack stack) {
        this.stack = stack;
        onContentsChanged();
    }

    @Override
    public void setStackUnchecked(FluidStack stack) {
        this.stack = stack;
        onContentsChanged();
    }

    @Override
    public int getCapacity() {
        return capacity;
    }

    @Override
    public boolean isFluidValid(FluidStack stack) {
        return true;
    }

    @Override
    public void onContentsChanged() {
        contentsChangedCount++;
    }
}
