package mekanism.fabric_shim.common;

import java.util.function.Supplier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import org.jetbrains.annotations.Nullable;

/**
 * Stand-in for NeoForge's {@code DeferredSpawnEggItem}: a {@link SpawnEggItem} whose {@link EntityType}
 * is supplied lazily (so the egg can be constructed before the entity type is registered). The vanilla
 * super is given a {@code null} type and {@link #getType(ItemStack)} is overridden to resolve the
 * supplier.
 */
public class DeferredSpawnEggItem extends SpawnEggItem {

    private final Supplier<? extends EntityType<? extends Mob>> typeSupplier;

    public DeferredSpawnEggItem(Supplier<? extends EntityType<? extends Mob>> type, int backgroundColor, int highlightColor, Properties props) {
        super(null, backgroundColor, highlightColor, props);
        this.typeSupplier = type;
    }

    @Override
    public EntityType<?> getType(@Nullable ItemStack stack) {
        return this.typeSupplier.get();
    }
}
