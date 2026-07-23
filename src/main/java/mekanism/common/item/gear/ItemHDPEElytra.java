package mekanism.common.item.gear;

import mekanism.common.registries.MekanismItems;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ElytraItem;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ItemHDPEElytra extends ElytraItem {

    public ItemHDPEElytra(Properties properties) {
        super(properties);
    }

    @Nullable
    public EquipmentSlot getEquipmentSlot(ItemStack stack) {
        return EquipmentSlot.CHEST;
    }

    //fabric-port: NeoForge patches ElytraItem to answer its canElytraFly item extension via
    // isFlyEnabled; vanilla has neither, so the rule is mirrored here for the broken-texture
    // property override (flight itself is vanilla ElytraItem behavior).
    public boolean canElytraFly(ItemStack stack, LivingEntity entity) {
        return ElytraItem.isFlyEnabled(stack);
    }

    @Override
    public boolean isValidRepairItem(@NotNull ItemStack toRepair, ItemStack repair) {
        return repair.is(MekanismItems.HDPE_SHEET);
    }
}