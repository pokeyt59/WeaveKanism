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
