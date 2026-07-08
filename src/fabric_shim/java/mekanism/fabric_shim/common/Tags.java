package mekanism.fabric_shim.common;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;

/**
 * Same surface (the slice Mekanism uses) as net.neoforged.neoforge.common.Tags. On 1.21 NeoForge's
 * conventional tags live in the `c:` namespace, identical to Fabric's conventions, so ids match
 * both loaders. Extend member-by-member as compile errors name them (paths from NeoForge's
 * Tags.java — do not guess).
 */
public final class Tags {

    private Tags() {
    }

    public static final class Items {

        private Items() {
        }

        public static final TagKey<Item> ORES = tag("ores");
        public static final TagKey<Item> ORES_COAL = tag("ores/coal");
        public static final TagKey<Item> ORES_COPPER = tag("ores/copper");
        public static final TagKey<Item> ORES_DIAMOND = tag("ores/diamond");
        public static final TagKey<Item> ORES_EMERALD = tag("ores/emerald");
        public static final TagKey<Item> ORES_GOLD = tag("ores/gold");
        public static final TagKey<Item> ORES_IRON = tag("ores/iron");
        public static final TagKey<Item> ORES_LAPIS = tag("ores/lapis");
        public static final TagKey<Item> ORES_REDSTONE = tag("ores/redstone");

        private static TagKey<Item> tag(String path) {
            return TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", path));
        }
    }

    public static final class Blocks {

        private Blocks() {
        }

        public static final TagKey<Block> ORES = tag("ores");
        public static final TagKey<Block> HIDDEN_FROM_RECIPE_VIEWERS = tag("hidden_from_recipe_viewers");

        private static TagKey<Block> tag(String path) {
            return TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("c", path));
        }
    }

    public static final class Fluids {

        private Fluids() {
        }

        public static final TagKey<Fluid> GASEOUS = tag("gaseous");
        public static final TagKey<Fluid> HIDDEN_FROM_RECIPE_VIEWERS = tag("hidden_from_recipe_viewers");

        private static TagKey<Fluid> tag(String path) {
            return TagKey.create(Registries.FLUID, ResourceLocation.fromNamespaceAndPath("c", path));
        }
    }

    public static final class EntityTypes {

        private EntityTypes() {
        }

        public static final TagKey<EntityType<?>> TELEPORTING_NOT_SUPPORTED = tag("teleporting_not_supported");

        private static TagKey<EntityType<?>> tag(String path) {
            return TagKey.create(Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath("c", path));
        }
    }

    public static final class DamageTypes {

        private DamageTypes() {
        }

        public static final TagKey<DamageType> IS_TECHNICAL = tag("is_technical");
        public static final TagKey<DamageType> IS_PREVENTABLE_MAGIC = tag("is_preventable_magic");

        private static TagKey<DamageType> tag(String path) {
            return TagKey.create(Registries.DAMAGE_TYPE, ResourceLocation.fromNamespaceAndPath("c", path));
        }
    }
}
