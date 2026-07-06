package mekanism.fabric_shim.event;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.neoforged.bus.api.Event;
import org.jetbrains.annotations.ApiStatus;

/**
 * Collects attribute-modifier changes for an item stack (stand-in for NeoForge's
 * ItemAttributeModifierEvent; same surface). On this port there is no global hook patching
 * getAttributeModifiers — instead the shim uses this event to recompute the vanilla
 * ATTRIBUTE_MODIFIERS data component when module/mode state changes (Phase 3).
 */
public class ItemAttributeModifierEvent extends Event {

    private final ItemStack stack;
    private final ItemAttributeModifiers defaultModifiers;
    private List<ItemAttributeModifiers.Entry> entries;

    @ApiStatus.Internal
    public ItemAttributeModifierEvent(ItemStack stack, ItemAttributeModifiers defaultModifiers) {
        this.stack = stack;
        this.defaultModifiers = defaultModifiers;
    }

    public ItemStack getItemStack() {
        return stack;
    }

    public ItemAttributeModifiers getDefaultModifiers() {
        return defaultModifiers;
    }

    public List<ItemAttributeModifiers.Entry> getModifiers() {
        return entries != null ? Collections.unmodifiableList(entries) : defaultModifiers.modifiers();
    }

    private List<ItemAttributeModifiers.Entry> editable() {
        if (entries == null) {
            entries = new ArrayList<>(defaultModifiers.modifiers());
        }
        return entries;
    }

    /**
     * @return true if the modifier was added; false if a modifier with the same id was already present for the attribute
     */
    public boolean addModifier(Holder<Attribute> attribute, AttributeModifier modifier, EquipmentSlotGroup slot) {
        for (ItemAttributeModifiers.Entry entry : getModifiers()) {
            if (entry.attribute().equals(attribute) && entry.modifier().id().equals(modifier.id())) {
                return false;
            }
        }
        return editable().add(new ItemAttributeModifiers.Entry(attribute, modifier, slot));
    }

    public boolean removeModifier(Holder<Attribute> attribute, ResourceLocation id) {
        return editable().removeIf(entry -> entry.attribute().equals(attribute) && entry.modifier().id().equals(id));
    }

    public void replaceModifier(Holder<Attribute> attribute, AttributeModifier modifier, EquipmentSlotGroup slot) {
        removeModifier(attribute, modifier.id());
        addModifier(attribute, modifier, slot);
    }

    public boolean removeIf(Predicate<ItemAttributeModifiers.Entry> condition) {
        return editable().removeIf(condition);
    }

    public boolean removeAllModifiersFor(Holder<Attribute> attribute) {
        return editable().removeIf(entry -> entry.attribute().equals(attribute));
    }

    public void clearModifiers() {
        editable().clear();
    }

    /**
     * Builds the resulting modifier component.
     */
    public ItemAttributeModifiers build() {
        if (entries == null) {
            return defaultModifiers;
        }
        return new ItemAttributeModifiers(List.copyOf(entries), defaultModifiers.showInTooltip());
    }
}
