# Phase 1f battle plan — making src/main compile

Produced from a full-error census (9,060 javac errors, 2026-07-06) with `src/main/java` enabled
and `-Xmaxerrs 10000`. Regenerate the census anytime: add `'src/main/java'` to the main srcDirs
in build.gradle (KEEP THAT EDIT UNCOMMITTED until green), run
`.\gradlew compileJava > fabric-port\census-raw.log 2>&1`, cluster with a script.

Work the steps IN ORDER — each step removes a big cluster and shrinks the noise for the next.
Re-run the compile after each step and confirm the error count drops as predicted before moving
on. Commit per step ([port]/[scripted] split as usual).

## Step 1 — Exclude deferred areas (one build.gradle decision, ≈ −5,700 errors)

Add to the main source set:
```groovy
java {
    exclude 'mekanism/client/**'                    // Phase 4
    exclude 'mekanism/common/integration/**'        // Phase 5
}
```
Then repair the seams this cuts:
- `mekanism/common/integration/MekanismHooks.java` is referenced by core `Mekanism.java` — it must
  compile. Un-exclude it (`include` wins? no — keep integration excluded and move NOTHING; instead
  hand-edit MekanismHooks: comment out the per-mod imports + hook bodies behind
  `//TODO(Phase 5)` markers, keep the IntegrationInfo/isLoaded scaffolding. This is a [port] edit;
  add it to the PORTING.md residual checklist (it WILL conflict on upstream merges — that's
  expected and cheap to redo).
- `mekanism/common/integration/energy/**` (EnergyCompatUtils, IEnergyCompat, StrictEnergyCompat,
  BlockEnergyCapabilityCache) is used by core tiles — un-exclude just this subpackage
  (`exclude` list: use `mekanism/common/integration/**` then `include 'mekanism/common/integration/energy/**'`
  does NOT work in Gradle filters — instead enumerate excludes per subpackage: crafttweaker,
  computer, projecte, lookingat, curios, gender, framedblocks, recipestages, jsonthings, etc.,
  leaving `energy/` in). Inside energy/, `ForgeEnergyCompat` references
  `net.neoforged.neoforge.energy.IEnergyStorage` — shim that one interface (4 files use it) so the
  FE compat compiles; actual FE interop on Fabric arrives with the team-reborn adapter (Phase 2).
- Common→client references will surface as new errors after excluding client (packets and
  DistExecutor-style code). Expect a handful; resolve each by the smallest of: move the client
  call behind `FMLEnvironment.dist` + reflection-free indirection, shim the client class name, or
  defer the file with a documented exclude. List every such seam in PORTING.md.

## SCOREBOARD (update after every step)

| Step | State | Errors after |
|---|---|---|
| census | — | 9,060 |
| 1 — exclusions + seams | DONE (b97cafed46) | 3,576 |
| 2 — @Override strip | DONE (161e9b70a7; script: strip_overrides.py, 88 methods → hook-wiring-checklist.md) | 3,400 |
| 3 — component Holder bridge | DONE (2b69fcf24b; interface injection + first mixins, runtime-verified by DataComponentBridgeTest) | 2,868 |
| 4 — trivial shims | DONE (5ae79149e4 + df73f47756; 23 classes incl. hooks/events/permissions/scan stubs) | 2,166 |
| 5.2 — capabilities surface (live Fabric registration; bootstrap posts RegisterCapabilitiesEvent) | DONE (371b439c8f + c935583125) | 1,798 |
| 5.1 — FluidType + BaseFlowingFluid | pending (design notes in Step 5 below) | — |
| 5.3 — networking surface (read PacketHandler/BasePacketHandler first) | pending | — |
| 5.4 — remaining event classes (~90 errs; NeoForge sources jar for surfaces) | pending | — |
| 6 — residue (openMenu, invalidateCapabilities args, ListTag(int), getData sites, seams) | pending | target 0 |

Remaining top clusters at 2,166: FluidType ~250, capabilities (BlockCapabilityCache 124 +
RegisterCapabilitiesEvent + ICapabilityProvider + pkg ~330 incl. invalidateCapabilities 32),
networking (IPayloadContext 118 + PacketDistributor 44 + pkgs ~150), BaseFlowingFluid 100,
recipe-viewer seam RecipeViewerUtils 2 (+ downstream ~110), client/key seam ~50, remaining event
classes ~90 (tick/chunk/entity-join/creative-tab/datapack-sync/spawn-placement/attribute-creation,
LivingIncomingDamageEvent/LivingFallEvent/LivingDeathEvent/EntityTeleportEvent already exists),
Player.openMenu overload ~10, ListTag(int) 18, misc residue.

## Step 1 RESULTS (executed 2026-07-06 — baseline for the next steps: **3,576 errors**)

Step 1 is DONE and committed: build.gradle exclusion filters (client via a file-only spec closure
with an allowlist, integrations per-package), MekanismHooks Phase-5 hook bodies commented,
IEnergyStorage shim + remap. Findings that AMEND the original step text above:

- `integration/computer` CORE STAYS IN — its annotations (@ComputerMethod etc.) and wrappers are
  imported by 80+ core tiles. Only `computer/computercraft/**`, `computer/opencomputers2/**` and
  `computer/ComputerCapabilityHelper.java` (the one CC-touching core file) are excluded. The
  computer core needs the ModFileScanData/AnnotationData stubs (Step 4) and `ComputerConstants`
  (44 errs — check where it lives; likely excluded CC binding → relocate reference or stub) plus
  3 references to the excluded ComputerCapabilityHelper to seam out.
- `client/recipe_viewer/type/**` is allowlisted back in (its types appear in core tile method
  signatures — 30 files). Two allowlisted data classes still fail on
  `RecipeViewerUtils` (2 errors): either allowlist RecipeViewerUtils IF its own imports are clean,
  else hand-edit the two call sites to inline the helper. STOP RULE: if the allowlist would grow
  past ~8 files, hand-edit instead of allowlisting.
- Remaining common→client seams to fix during Steps 4-6 (from the census):
  `mekanism.client.key` MekanismKeyHandler/MekKeyHandler (8 common files; client/key needs only
  small KeyModifier/IKeyConflictContext/KeyConflictContext shims → shim those in Step 4, then
  allowlist client/key/**), `SoundHandler` (3 files), `MekanismClient` (3 files),
  `mekanism.client` root (8 files), `client.model.data`/`TransmitterModelData`/`QuadTransformation`
  (7 files — goes with the ModelData shim decision). Treat each: smallest of shim / allowlist /
  dist-guard; document every choice here.
- New pattern-table entries discovered: `Item.Properties#setNoRepair` (10 files — Step 3 injected
  interface + mixin storing a flag; repair-blocking behavior wires in Phase 3),
  `ItemStack#canPerformAction` (3 files — route through an ItemAbilityHooks shim static),
  `net.neoforged.neoforge.common.Tags` (11 files — shim with conventional `c:` tag constants).
- Post-Step-1 top clusters (baseline for Step 2+): FluidType 236+14, BlockCapabilityCache 124,
  IPayloadContext 118 (+ network pkgs 148), capabilities pkg 98 + RegisterCapabilitiesEvent 54 +
  ICapabilityProvider 56, recipe-viewer seam 118+114 (fixed by allowlist, see above), Lazy 78,
  IFluidHandlerItem 62, ItemAbilities 50, BaseFlowingFluid Source/Flowing 100, override-strip 192
  (much smaller than the pre-exclusion 854 — most were client/integration), data-component
  Holder overloads ~370, event classes ~90, ListTag(int) 18, computer-core stubs ~90.

## Step 2 — Scripted @Override strip for NeoForge extension hooks (≈ −190 errors post-Step-1)

Cluster: `method does not override or implement a method from a supertype` (854). Cause: Mekanism
classes override methods that only exist because NeoForge patches vanilla (IItemExtension,
IBlockExtension, IBlockEntityExtension, IFluidHandlerItem hooks, etc.).

Action: write `fabric-port/strip_overrides.py` — scripted, replayable (same contract as remap.py):
input = a reviewed list of (class-name-suffix optional, method name) pairs in
`fabric-port/mappings/neoforge-hook-methods.tsv`; transform = remove the `@Override` line
immediately preceding those method declarations in src/main (leave the method bodies intact!).
Build the method list FROM THE CENSUS (each error names the method), review it, commit the list +
script, then the `[scripted]` output commit.

IMPORTANT: every stripped method is a NeoForge hook that no longer gets CALLED. Maintain
`fabric-port/mappings/hook-wiring-checklist.md` mapping each method → the phase/mechanism that
will re-wire it (Fabric API event, mixin, or dead-on-Fabric). This checklist is Phase 2–4 input;
without it, features silently do nothing.

## Step 3 — Data-component Holder overloads via interface injection + mixin (≈ −400 errors)

Clusters: `method set/get/getOrDefault/remove/component ... cannot be applied` (~400). Cause:
NeoForge adds `Holder<DataComponentType<T>>`-accepting overloads to ItemStack,
DataComponentHolder, DataComponentInput (upstream calls them ~400 times with
MekanismDataComponents.X deferred holders); vanilla only takes the raw DataComponentType.

Action (keeps all 400 call sites textually unchanged — do NOT hand-edit them):
1. Interfaces `mekanism.fabric_shim.inject.MekDataComponentHolderExt` /
   `MekItemStackExt` / `MekItemPropertiesExt` / `MekDataComponentInputExt` declaring default
   methods with the Holder-taking signatures that unwrap (`holder.value()`) and delegate to the
   vanilla method (cast `this`).
2. Loom interface injection (fabric.mod.json `"custom": {"loom:injected_interfaces": {...}}`)
   so javac sees vanilla types implementing them at compile time.
3. The port's FIRST mixins: trivial `@Mixin(ItemStack.class) implements MekItemStackExt` (etc.)
   so the interface is real at runtime. Register a mixins json in fabric.mod.json.
Also covers `invalidateCapabilities` on Level (32 errors) if given a default no-op (Phase 2 turns
it into cache eviction) — decide when you get there; a shim static helper is also acceptable
per the PORTING.md pattern table.

## Step 4 — Trivial shim batch (≈ −600 errors, all mechanical)

Same-surface fresh implementations in `mekanism.fabric_shim.*`, one commit, TSV mappings + remap
run. From the census, with sizes:

| NeoForge class | errs/files | Note |
|---|---|---|
| `common.util.Lazy` | 86/13 | memoized Supplier; `Lazy.of(supplier)` |
| `fluids.capability.IFluidHandlerItem` | 70/13 | extends shim IFluidHandler + `getContainer()` |
| `common.ItemAbilities` | 50/4 | constants (AXE_STRIP, PICKAXE_DIG, SHEARS_*, …) over existing ItemAbility shim; names from NeoForge sources |
| `client.model.data.ModelData` + `ModelProperty` | 166/36 | plain typed-map classes, loader-agnostic; tiles keep getModelData(); Phase 4 bridges to fabric-rendering-data-attachment |
| `common.util.TriState` | 6/3 | enum TRUE/FALSE/DEFAULT |
| `common.util.FriendlyByteBufUtil` | 2/2 | writeCustomData helpers |
| `common.UsernameCache` | ~2/1 | map + lastKnownUsername lookup |
| `common.SoundActions` | 6/3 | constants; fluids-related |
| `items.ItemHandlerHelper` | 6/3 | insertItem/giveItemToPlayer statics |
| `common.util.RecipeMatcher`, `ItemStackMap` | 2×2 | small helpers |
| `energy.IEnergyStorage` | 8/4 | the FE interface (for integration/energy) |
| `server.permission.*` (PermissionAPI, PermissionNode, types) | 36/2 | default-allow stubs; real perms Phase 5 |
| `neoforgespi.language.ModFileScanData/AnnotationData` | 28/3 | empty-scan stubs so MekAnnotationScanner compiles (computer integration is excluded anyway) |
| `common.CommonHooks` / `event.EventHooks` | 10/7 | shim ONLY the members the errors name; check each usage first |

## Step 5 — Design shims (the real Phase-1f work, ≈ −900 errors)

1. **Fluids** (`FluidType` 274/46, `getFluidType` 26, `BaseFlowingFluid.Source/Flowing` 100/2,
   fluids pkg 94/46): shim `FluidType` as a data holder with NeoForge's `FluidType.Properties`
   builder surface (descriptionId, density, viscosity, temperature, lightLevel, rarity,
   sound actions, canDrink/canExtinguish/…, BUCKET_VOLUME=1000) and shim `BaseFlowingFluid`
   (+`.Properties`, `.Source`, `.Flowing`) as a fresh configurable FlowingFluid implementing
   vanilla's abstract methods from the Properties. Goal: `FluidDeferredRegister` and
   `MekanismFluids` compile with minimal diff. Registration of fluid types goes to a shim
   registry under `neoforge:fluid_type` ONLY if RegisterEvent ordering needs it — otherwise keep
   FluidTypes out of registries entirely (they're only consulted via our FluidAttributes shim and
   Phase 4 client extensions). Milk: MekanismFluids may reference NeoForgeMod.MILK — absent
   holder already exists; audit call sites.
2. **Capabilities compile surface** (112/50 + BlockCapabilityCache 128/21 + ICapabilityProvider
   70/13 + RegisterCapabilitiesEvent 62/21 + `getCapability` 34/10): complete
   `mekanism.fabric_shim.capabilities`: `RegisterCapabilitiesEvent` (mod-bus event; collect
   registrations, apply to Fabric lookups — the working half of Phase 2), `Capabilities` class
   with `ItemHandler/FluidHandler/EnergyStorage` token constants (BLOCK/ITEM/ENTITY variants as
   NeoForge has them) typed against shim IItemHandler/IFluidHandler + team-reborn EnergyStorage,
   `ICapabilityProvider` functional interface, `BlockCapabilityCache`
   (`create(cap, level, pos, ctx)` + `getCapability()`; implementation = lazy re-query via the
   token's lookup each call for 1f — the eviction/caching optimization is Phase 2 with a
   benchmark), `ICapabilityInvalidationListener` stub. `level.getCapability(...)`/`itemStack
   .getCapability(...)` call sites → pattern table (token-first rewrite), possibly scriptable
   with a careful transform; hand-edit is fine too.
3. **Networking compile surface** (IPayloadContext 118/55, PacketDistributor 44/14,
   PayloadRegistrar/RegisterPayloadHandlersEvent/IContainerFactory ~40): shim `IPayloadContext`
   (player(), enqueueWork(), reply(), disconnect(), connection(), flow()),
   `PacketDistributor` statics over ServerPlayNetworking/ClientPlayNetworking (sendToPlayer,
   sendToAllPlayers, sendToPlayersTrackingChunk, sendToPlayersTrackingEntityAndSelf, sendToServer),
   `PayloadRegistrar` over PayloadTypeRegistry + play receivers, `RegisterPayloadHandlersEvent`
   (mod-bus), `IContainerFactory` (extends MenuType.MenuSupplier reading buf). Mekanism funnels
   ALL packets through PacketHandler/BasePacketHandler, so the shim only needs what that funnel
   touches — read those two files first and shim to fit. This makes Phase 3 mostly "test it".
4. **Remaining event classes** the census names (client.event excluded; check `BlockEvent`,
   `ServerTickEvent`, `LevelTickEvent`, `PlayerEvent` family, `EntityJoinLevelEvent`,
   `BuildCreativeModeTabContentsEvent`, `ModifyDefaultComponentsEvent`, `OnDatapackSyncEvent`,
   `BlockDropsEvent`, chunk/level events, `LivingEvent` family, spawn placement, entity
   attribute creation): create the shim event classes (game-bus family) with NeoForge surfaces;
   FIRING them is Phase 3 — a compile-only event class with no glue is acceptable during 1f but
   MUST be listed in the hook-wiring checklist (Step 2) so it isn't forgotten.

## Step 6 — Residue hand-edits (pattern table, ≈ −300 errors)

`new ListTag(int)` (20), `getData`/attachment access sites (26/9 — route through a shim
AttachmentHooks helper or Fabric AttachmentTarget casts; decide with transfer-bridge-style care
for persistence), `getAndValidateNotEmpty` ambiguity (26 — inspect, likely an overload collision
with a shimmed type; fix by explicit typing at call sites), `BlockRegistryObject` generics
mismatches (40 — likely shim DeferredHolder vs upstream MekanismDeferredHolder variance; fix in
the registration wrappers, not per call site), `AbstractWidget.onClick` signature (14 — vanilla
signature difference: NeoForge adds a button param? check vanilla 1.21.1 and adjust GUI code or
defer with client), misc singletons.

## Definition of done for 1f

- `.\gradlew compileJava` green WITH `src/main/java` in srcDirs (commit the srcDirs change only now).
- `.\gradlew test` green.
- Mod construction uncommented in `MekanismFabric.onInitialize` (ModContainer + bridgeConfigEvents
  + `new Mekanism(...)`), dev server boots: expect config TOMLs written under `run/config/Mekanism/`,
  the `neoforge:*` "empty registry" errors GONE (registries now populated), and Mekanism's
  registration log lines present.
- PORTING.md: 1f checked; every deferral/exclusion documented; hook-wiring checklist committed.
- Census artifacts (`census-raw.log`) are throwaway — do not commit them.
