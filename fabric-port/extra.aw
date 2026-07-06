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
