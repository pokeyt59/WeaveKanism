package mekanism.common.capabilities;

import mekanism.fabric_shim.capabilities.RegisterCapabilitiesEvent;

@FunctionalInterface
public interface ICapabilityAware {

    void attachCapabilities(RegisterCapabilitiesEvent event);
}