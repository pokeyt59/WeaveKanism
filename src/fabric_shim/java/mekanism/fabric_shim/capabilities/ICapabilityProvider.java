package mekanism.fabric_shim.capabilities;

import org.jetbrains.annotations.Nullable;

/**
 * Same surface as net.neoforged.neoforge.capabilities.ICapabilityProvider.
 */
@FunctionalInterface
public interface ICapabilityProvider<O, C, T> {

    @Nullable
    T getCapability(O object, C context);
}
