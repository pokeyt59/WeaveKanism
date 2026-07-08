package mekanism.fabric_shim.client.render.armor;

import java.util.function.Predicate;
import mekanism.api.gear.ModuleData;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;

/**
 * Server-safe stand-in for {@code mekanism.client.render.armor.MekaSuitArmor}. Common module setup
 * registers which modules render on which armor slots; a no-op on the server. Phase 4 renders the
 * mekasuit.
 */
public final class MekaSuitArmor {

    private MekaSuitArmor() {
    }

    public static void registerModule(String name, Holder<ModuleData<?>> moduleData, EquipmentSlot slotType, Predicate<LivingEntity> isActive) {
    }
}
