package mekanism.fabric.mixin;

import mekanism.fabric_shim.inject.MekItemPropertiesExt;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Item.Properties.class)
public abstract class ItemPropertiesMixin implements MekItemPropertiesExt {

    @Unique
    private boolean mek$noRepair;

    @Override
    public Item.Properties setNoRepair() {
        //Flag stored; the repair-blocking behavior hook is Phase 3 (see hook-wiring checklist)
        this.mek$noRepair = true;
        return (Item.Properties) (Object) this;
    }

    @Override
    public boolean mek_isNoRepair() {
        return mek$noRepair;
    }
}
