package mekanism.common.capabilities;

import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import mekanism.fabric_shim.capabilities.BlockCapability;
import mekanism.fabric_shim.capabilities.EntityCapability;
import mekanism.fabric_shim.capabilities.ItemCapability;
import org.jetbrains.annotations.Nullable;

public record MultiTypeCapability<HANDLER>(BlockCapability<HANDLER, @Nullable Direction> block,
                                           ItemCapability<HANDLER, Void> item,
                                           EntityCapability<HANDLER, ?> entity) implements IMultiTypeCapability<HANDLER, HANDLER> {

    public MultiTypeCapability(ResourceLocation name, Class<HANDLER> handlerClass) {
        this(
              BlockCapability.createSided(name, handlerClass),
              ItemCapability.createVoid(name, handlerClass),
              EntityCapability.createVoid(name, handlerClass)
        );
    }
}