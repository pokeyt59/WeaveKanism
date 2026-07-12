package mekanism.client.render.armor;

import mekanism.fabric_shim.client.extensions.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;

public interface ISpecialGear extends IClientItemExtensions {

    @NotNull
    ICustomArmor gearModel();
}