package mekanism.common.attachments.containers.creator;

import mekanism.common.attachments.containers.ContainerType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import mekanism.fabric_shim.common.util.INBTSerializable;

@FunctionalInterface
public interface IBasicContainerCreator<CONTAINER extends INBTSerializable<CompoundTag>> {

    CONTAINER create(ContainerType<? super CONTAINER, ?, ?> containerType, ItemStack attachedTo, int containerIndex);
}