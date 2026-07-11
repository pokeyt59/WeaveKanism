package mekanism.fabric_shim.fluids.capability.wrappers;

import mekanism.fabric_shim.fluids.FluidStack;
import mekanism.fabric_shim.fluids.FluidType;
import mekanism.fabric_shim.fluids.capability.IFluidHandlerItem;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.NotNull;

/**
 * Fluid handler over a bucket item (stand-in for NeoForge's FluidBucketWrapper; same surface and
 * simulate contract): exactly one tank of {@link FluidType#BUCKET_VOLUME} that swaps the container
 * stack between the empty and filled bucket on execute.
 *
 * <p>Known deviation (see PORTING.md): no milk special-casing — milk has no registered fluid on
 * this port yet, so milk buckets read as empty.
 */
public class FluidBucketWrapper implements IFluidHandlerItem {

    @NotNull
    protected ItemStack container;

    public FluidBucketWrapper(@NotNull ItemStack container) {
        this.container = container;
    }

    @NotNull
    @Override
    public ItemStack getContainer() {
        return container;
    }

    public boolean canFillFluidType(FluidStack fluid) {
        return fluid.getFluid().getBucket() instanceof BucketItem;
    }

    @NotNull
    public FluidStack getFluid() {
        Item item = container.getItem();
        if (item instanceof BucketItem bucket && bucket.content != Fluids.EMPTY) {
            return new FluidStack(bucket.content, FluidType.BUCKET_VOLUME);
        }
        return FluidStack.EMPTY;
    }

    protected void setFluid(@NotNull FluidStack fluidStack) {
        if (fluidStack.isEmpty()) {
            container = new ItemStack(Items.BUCKET);
        } else {
            container = new ItemStack(fluidStack.getFluid().getBucket());
        }
    }

    @Override
    public int getTanks() {
        return 1;
    }

    @NotNull
    @Override
    public FluidStack getFluidInTank(int tank) {
        return getFluid();
    }

    @Override
    public int getTankCapacity(int tank) {
        return FluidType.BUCKET_VOLUME;
    }

    @Override
    public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
        return true;
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        if (container.getCount() != 1 || resource.getAmount() < FluidType.BUCKET_VOLUME || !getFluid().isEmpty() || !canFillFluidType(resource)) {
            return 0;
        }
        if (action.execute()) {
            setFluid(resource);
        }
        return FluidType.BUCKET_VOLUME;
    }

    @NotNull
    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) {
        if (container.getCount() != 1 || resource.getAmount() < FluidType.BUCKET_VOLUME) {
            return FluidStack.EMPTY;
        }
        FluidStack fluidStack = getFluid();
        if (!fluidStack.isEmpty() && FluidStack.isSameFluidSameComponents(fluidStack, resource)) {
            if (action.execute()) {
                setFluid(FluidStack.EMPTY);
            }
            return fluidStack;
        }
        return FluidStack.EMPTY;
    }

    @NotNull
    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
        if (container.getCount() != 1 || maxDrain < FluidType.BUCKET_VOLUME) {
            return FluidStack.EMPTY;
        }
        FluidStack fluidStack = getFluid();
        if (!fluidStack.isEmpty()) {
            if (action.execute()) {
                setFluid(FluidStack.EMPTY);
            }
            return fluidStack;
        }
        return FluidStack.EMPTY;
    }
}
