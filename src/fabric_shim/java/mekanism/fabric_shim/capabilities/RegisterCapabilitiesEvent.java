package mekanism.fabric_shim.capabilities;

import java.util.HashSet;
import java.util.Set;
import mekanism.fabric_shim.fml.event.IModBusEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.Event;

/**
 * Same surface as net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent, registering
 * providers directly into the shim tokens' backing Fabric API lookups — so on Fabric this event
 * IS the live capability registration (there is no deferred apply step).
 */
public class RegisterCapabilitiesEvent extends Event implements IModBusEvent {

    private record Registration(Object capability, Object target) {
    }

    private final Set<Registration> registrations = new HashSet<>();
    private final Set<Object> proxyable = new HashSet<>();

    public <T, C> void registerBlock(BlockCapability<T, C> capability, IBlockCapabilityProvider<T, C> provider, Block... blocks) {
        capability.lookup().registerForBlocks(provider::getCapability, blocks);
        for (Block block : blocks) {
            registrations.add(new Registration(capability, block));
        }
    }

    public <T, C, BE extends BlockEntity> void registerBlockEntity(BlockCapability<T, C> capability, BlockEntityType<BE> blockEntityType,
          ICapabilityProvider<? super BE, C, T> provider) {
        capability.lookup().registerForBlockEntity(provider::getCapability, blockEntityType);
        registrations.add(new Registration(capability, blockEntityType));
    }

    public boolean isBlockRegistered(BlockCapability<?, ?> capability, Block block) {
        return registrations.contains(new Registration(capability, block));
    }

    public <T, C, E extends Entity> void registerEntity(EntityCapability<T, C> capability, EntityType<E> entityType, ICapabilityProvider<? super E, C, T> provider) {
        capability.lookup().registerForType((entity, context) -> {
            @SuppressWarnings("unchecked")
            E typed = (E) entity;
            return provider.getCapability(typed, context);
        }, entityType);
        registrations.add(new Registration(capability, entityType));
    }

    public boolean isEntityRegistered(EntityCapability<?, ?> capability, EntityType<?> entityType) {
        return registrations.contains(new Registration(capability, entityType));
    }

    public <T, C> void registerItem(ItemCapability<T, C> capability, ICapabilityProvider<net.minecraft.world.item.ItemStack, C, T> provider, ItemLike... items) {
        Item[] rawItems = new Item[items.length];
        for (int i = 0; i < items.length; i++) {
            rawItems[i] = items[i].asItem();
        }
        capability.lookup().registerForItems(provider::getCapability, rawItems);
        for (Item item : rawItems) {
            registrations.add(new Registration(capability, item));
        }
    }

    public boolean isItemRegistered(ItemCapability<?, ?> capability, Item item) {
        return registrations.contains(new Registration(capability, item));
    }

    public void setProxyable(BlockCapability<?, ?> capability) {
        proxyable.add(capability);
    }

    public void setNonProxyable(BlockCapability<?, ?> capability) {
        proxyable.remove(capability);
    }
}
