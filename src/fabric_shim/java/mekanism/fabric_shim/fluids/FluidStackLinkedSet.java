package mekanism.fabric_shim.fluids;

import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenCustomHashSet;
import java.util.Set;
import org.jetbrains.annotations.Nullable;

/**
 * Sets of fluid stacks hashed by fluid + components, ignoring amount (stand-in for NeoForge's
 * FluidStackLinkedSet; same surface).
 */
public final class FluidStackLinkedSet {

    private FluidStackLinkedSet() {
    }

    public static final Hash.Strategy<? super FluidStack> TYPE_AND_COMPONENTS = new Hash.Strategy<FluidStack>() {
        @Override
        public int hashCode(@Nullable FluidStack stack) {
            return FluidStack.hashFluidAndComponents(stack);
        }

        @Override
        public boolean equals(@Nullable FluidStack first, @Nullable FluidStack second) {
            return first == second
                   || first != null && second != null && first.isEmpty() == second.isEmpty() && FluidStack.isSameFluidSameComponents(first, second);
        }
    };

    public static Set<FluidStack> createTypeAndComponentsSet() {
        return new ObjectLinkedOpenCustomHashSet<>(TYPE_AND_COMPONENTS);
    }
}
