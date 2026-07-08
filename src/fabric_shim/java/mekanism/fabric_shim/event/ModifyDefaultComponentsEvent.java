package mekanism.fabric_shim.event;

import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;
import mekanism.fabric_shim.fml.event.IModBusEvent;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.neoforged.bus.api.Event;

/**
 * Stand-in for NeoForge's {@code ModifyDefaultComponentsEvent} (mod bus). Compile-only: applying the
 * patches to items' default component maps needs a mixin/registry hook (Phase 3), so {@link #modify}
 * currently records nothing. Tracked in the hook-wiring checklist.
 */
public class ModifyDefaultComponentsEvent extends Event implements IModBusEvent {

    public void modify(ItemLike item, Consumer<DataComponentPatch.Builder> patch) {
        //TODO(fabric-port, Phase 3): build the patch and apply to the item's default components
    }

    public void modifyMatching(Predicate<? super Item> predicate, Consumer<DataComponentPatch.Builder> patch) {
        //TODO(fabric-port, Phase 3): as modify(), for every matching item
    }

    public Stream<Item> getAllItems() {
        return BuiltInRegistries.ITEM.stream();
    }
}
