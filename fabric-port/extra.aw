# Vanilla members Mekanism's source references that NeoForge widens via its OWN access
# transformers (not Mekanism's AT). Appended verbatim to the generated access widener by
# fabric-port/at2aw.py. Keep entries commented with the code that needs them.

# mekanism.api.recipes.ingredients.ItemStackIngredient + creator: Ingredient.Value/TagValue
# pattern-matching and values introspection (NeoForge makes these public and adds getValues())
accessible class net/minecraft/world/item/crafting/Ingredient$Value
accessible class net/minecraft/world/item/crafting/Ingredient$TagValue
accessible class net/minecraft/world/item/crafting/Ingredient$ItemValue
accessible field net/minecraft/world/item/crafting/Ingredient values [Lnet/minecraft/world/item/crafting/Ingredient$Value;
accessible method net/minecraft/world/item/crafting/Ingredient fromValues (Ljava/util/stream/Stream;)Lnet/minecraft/world/item/crafting/Ingredient;

# Step 6 residue: vanilla members NeoForge widens that Mekanism references directly.
# Player.closeContainer (tile GUI close), SimpleParticleType(boolean) ctor (particle registration),
# BucketItem.content (electric pump reads the bucket's fluid), CommandSourceStack.source,
# ItemStackLinkedSet.TYPE_AND_TAG (QIO dedup strategy), ResourceLocation.validNamespaceChar.
accessible method net/minecraft/world/entity/player/Player closeContainer ()V
accessible method net/minecraft/core/particles/SimpleParticleType <init> (Z)V
accessible field net/minecraft/world/item/BucketItem content Lnet/minecraft/world/level/material/Fluid;
accessible field net/minecraft/commands/CommandSourceStack source Lnet/minecraft/commands/CommandSource;
accessible field net/minecraft/world/item/ItemStackLinkedSet TYPE_AND_TAG Lit/unimi/dsi/fastutil/Hash$Strategy;
accessible method net/minecraft/resources/ResourceLocation validNamespaceChar (C)Z
# Private vanilla fields Mekanism reads directly (NeoForge widens these).
accessible field net/minecraft/world/effect/MobEffectInstance hiddenEffect Lnet/minecraft/world/effect/MobEffectInstance;
accessible field net/minecraft/world/level/block/state/BlockBehaviour$BlockStateBase destroySpeed F
accessible field net/minecraft/server/network/ServerGamePacketListenerImpl aboveGroundTickCount I
accessible field net/minecraft/server/level/ChunkMap playerMap Lnet/minecraft/server/level/PlayerMap;
accessible field net/minecraft/world/level/chunk/ChunkGenerator featuresPerStep Ljava/util/function/Supplier;
accessible field net/minecraft/world/entity/LivingEntity effectsDirty Z
accessible field net/minecraft/world/level/material/MapColor MATERIAL_COLORS [Lnet/minecraft/world/level/material/MapColor;
# Step 6 residue batch 3: BlockStateHelper reads Properties.lightEmission, WrappedShapedRecipe reads
# ShapedRecipe.pattern, TileEntitySolarNeutronActivator reads Biome.climateSettings (no biome
# modifiers on the port, base == modified), and MekanismCreativeTab subclasses CreativeModeTab via
# the canonical ctor + Builder state reads (NeoForge patches a protected Builder ctor instead).
accessible field net/minecraft/world/level/block/state/BlockBehaviour$Properties lightEmission Ljava/util/function/ToIntFunction;
accessible field net/minecraft/world/item/crafting/ShapedRecipe pattern Lnet/minecraft/world/item/crafting/ShapedRecipePattern;
accessible class net/minecraft/world/level/biome/Biome$ClimateSettings
accessible field net/minecraft/world/level/biome/Biome climateSettings Lnet/minecraft/world/level/biome/Biome$ClimateSettings;
accessible method net/minecraft/world/item/CreativeModeTab <init> (Lnet/minecraft/world/item/CreativeModeTab$Row;ILnet/minecraft/world/item/CreativeModeTab$Type;Lnet/minecraft/network/chat/Component;Ljava/util/function/Supplier;Lnet/minecraft/world/item/CreativeModeTab$DisplayItemsGenerator;)V
accessible field net/minecraft/world/item/CreativeModeTab$Builder row Lnet/minecraft/world/item/CreativeModeTab$Row;
accessible field net/minecraft/world/item/CreativeModeTab$Builder column I
accessible field net/minecraft/world/item/CreativeModeTab$Builder displayName Lnet/minecraft/network/chat/Component;
accessible field net/minecraft/world/item/CreativeModeTab$Builder iconGenerator Ljava/util/function/Supplier;
accessible field net/minecraft/world/item/CreativeModeTab$Builder displayItemsGenerator Lnet/minecraft/world/item/CreativeModeTab$DisplayItemsGenerator;
accessible field net/minecraft/world/item/CreativeModeTab$Builder type Lnet/minecraft/world/item/CreativeModeTab$Type;
# SyncAllSecurityData (config-phase task) completes itself on the listener impl.
accessible method net/minecraft/server/network/ServerConfigurationPacketListenerImpl finishCurrentTask (Lnet/minecraft/server/network/ConfigurationTask$Type;)V
# Phase 3 tick-event bridges pass the server's haveTime supplier into ServerTickEvent/LevelTickEvent
# (NeoForge's patched tick loop passes the same reference).
accessible method net/minecraft/server/MinecraftServer haveTime ()Z
# RegisterSpawnPlacementsEvent applies registrations through vanilla's private register.
accessible method net/minecraft/world/entity/SpawnPlacements register (Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/world/entity/SpawnPlacementType;Lnet/minecraft/world/level/levelgen/Heightmap$Types;Lnet/minecraft/world/entity/SpawnPlacements$SpawnPredicate;)V

# Phase 4 client batch: vanilla client members NeoForge ATs that Mekanism's client code
# reads directly (census 2026-07-12; descriptors javap-verified).
accessible class net/minecraft/client/resources/model/ItemOverrides$BakedOverride
accessible field net/minecraft/client/gui/screens/inventory/AbstractContainerScreen quickCraftingType I
accessible field net/minecraft/client/gui/screens/inventory/AbstractContainerScreen clickedSlot Lnet/minecraft/world/inventory/Slot;
accessible field net/minecraft/client/gui/screens/inventory/AbstractContainerScreen isSplittingStack Z
accessible field net/minecraft/client/gui/screens/inventory/AbstractContainerScreen draggingItem Lnet/minecraft/world/item/ItemStack;
accessible field net/minecraft/client/gui/screens/inventory/AbstractContainerScreen snapbackStartY I
accessible field net/minecraft/client/gui/screens/inventory/AbstractContainerScreen snapbackStartX I
accessible field net/minecraft/client/gui/screens/inventory/AbstractContainerScreen snapbackItem Lnet/minecraft/world/item/ItemStack;
accessible field net/minecraft/client/gui/screens/inventory/AbstractContainerScreen skipNextRelease Z
accessible field net/minecraft/client/gui/screens/inventory/AbstractContainerScreen lastClickTime J
accessible field net/minecraft/client/gui/screens/inventory/AbstractContainerScreen lastClickSlot Lnet/minecraft/world/inventory/Slot;
accessible field net/minecraft/client/gui/screens/inventory/AbstractContainerScreen lastClickButton I
accessible field net/minecraft/client/gui/screens/inventory/AbstractContainerScreen snapbackTime J
accessible field net/minecraft/client/gui/screens/inventory/AbstractContainerScreen snapbackEnd Lnet/minecraft/world/inventory/Slot;
accessible field net/minecraft/client/gui/screens/inventory/AbstractContainerScreen quickCraftingButton I
accessible field net/minecraft/client/gui/screens/inventory/AbstractContainerScreen lastQuickMoved Lnet/minecraft/world/item/ItemStack;
accessible field net/minecraft/client/gui/screens/inventory/AbstractContainerScreen doubleclick Z
accessible field net/minecraft/client/model/AgeableListModel bodyYOffset F
accessible field net/minecraft/client/model/AgeableListModel babyBodyScale F
accessible field net/minecraft/client/model/AgeableListModel scaleHead Z
accessible field net/minecraft/client/model/AgeableListModel babyZHeadOffset F
accessible field net/minecraft/client/model/AgeableListModel babyYHeadOffset F
accessible field net/minecraft/client/model/AgeableListModel babyHeadScale F
accessible field net/minecraft/client/gui/screens/Screen deferredTooltipRendering Lnet/minecraft/client/gui/screens/Screen$DeferredTooltipRendering;
accessible field net/minecraft/client/gui/screens/Screen renderables Ljava/util/List;
accessible field net/minecraft/client/gui/components/Tooltip cachedTooltip Ljava/util/List;
accessible field net/minecraft/client/gui/Gui subtitleOverlay Lnet/minecraft/client/gui/components/SubtitleOverlay;
accessible field net/minecraft/client/gui/Gui overlayMessageTime I
accessible field net/minecraft/client/sounds/SoundManager soundEngine Lnet/minecraft/client/sounds/SoundEngine;
accessible field net/minecraft/client/sounds/SoundEngine listeners Ljava/util/List;
accessible field net/minecraft/client/sounds/SoundEngine instanceToChannel Ljava/util/Map;
accessible field net/minecraft/client/renderer/entity/layers/HumanoidArmorLayer outerModel Lnet/minecraft/client/model/HumanoidModel;
accessible field net/minecraft/client/renderer/entity/layers/HumanoidArmorLayer innerModel Lnet/minecraft/client/model/HumanoidModel;
accessible field net/minecraft/client/gui/components/EditBox highlightPos I
accessible field net/minecraft/client/gui/components/EditBox maxLength I
accessible field net/minecraft/client/gui/components/EditBox canLoseFocus Z
accessible field net/minecraft/client/model/PlayerModel cloak Lnet/minecraft/client/model/geom/ModelPart;
accessible field net/minecraft/client/model/PlayerModel ear Lnet/minecraft/client/model/geom/ModelPart;
accessible field net/minecraft/client/model/geom/ModelPart children Ljava/util/Map;
accessible field net/minecraft/client/model/geom/ModelPart cubes Ljava/util/List;
accessible field net/minecraft/client/gui/components/SubtitleOverlay audibleSubtitles Ljava/util/List;
accessible field net/minecraft/client/gui/components/AbstractWidget tooltip Lnet/minecraft/client/gui/components/WidgetTooltipHolder;
accessible field net/minecraft/client/gui/components/AbstractWidget PERIOD_PER_SCROLLED_PIXEL D
accessible field net/minecraft/client/gui/components/AbstractWidget MIN_SCROLL_PERIOD D
accessible field net/minecraft/client/model/ArmorStandModel shoulderStick Lnet/minecraft/client/model/geom/ModelPart;
accessible field net/minecraft/client/model/ArmorStandModel rightBodyStick Lnet/minecraft/client/model/geom/ModelPart;
accessible field net/minecraft/client/model/ArmorStandModel leftBodyStick Lnet/minecraft/client/model/geom/ModelPart;
accessible field net/minecraft/client/renderer/entity/LivingEntityRenderer layers Ljava/util/List;
accessible field net/minecraft/client/KeyMapping isDown Z
accessible field net/minecraft/world/level/block/LiquidBlock fluid Lnet/minecraft/world/level/material/FlowingFluid;
accessible field net/minecraft/world/entity/item/ItemEntity age I
accessible field net/minecraft/client/model/geom/ModelPart$Cube polygons [Lnet/minecraft/client/model/geom/ModelPart$Polygon;
accessible field net/minecraft/client/KeyMapping key Lcom/mojang/blaze3d/platform/InputConstants$Key;
# IClientItemExtensions.DEFAULT returns the vanilla shared BEWLR (private field, no accessor).
accessible field net/minecraft/client/renderer/entity/ItemRenderer blockEntityRenderer Lnet/minecraft/client/renderer/BlockEntityWithoutLevelRenderer;
