# Mekanism Fabric Port — Framework & Procedure

This branch (`fabric/1.21.x`) is an unofficial Fabric port of Mekanism, structured for
**repeatable re-porting of upstream releases** (Create: Fabric model): keep the diff against
upstream minimal, concentrate loader differences in bridge libraries + a small shim layer,
and make mechanical rewrites scripted and replayable.

## Branch model

- `1.21.x` — pristine upstream tracking branch (never commit port work here).
- `fabric/1.21.x` — the port. History is organized as:
  1. **Scripted commits** — output of `fabric-port/` transforms, replayable (`[scripted]` prefix).
  2. **Hand-edit commits** — shims, build system, mixins, adaptations (`[port]` prefix).
- Future: `fabric/1.20.x` — 1.20.1 Fabric track, based on upstream `1.20.x` (v10.4.x, Forge-era),
  same pipeline with a Forge→Fabric mapping table. New upstream features/fixes get cherry-picked
  and adapted; candidates tracked per release below. Stand up after the 1.21.1 first milestone.

## Upstream release merge procedure

1. `git fetch upstream && git checkout fabric/1.21.x`
2. `git merge <upstream-release-tag>` — resolve conflicts:
   - `build.gradle`, `settings.gradle`: keep ours; review upstream for dependency/source-set changes.
   - Scripted-rewrite files (e.g. FluidStack imports): take **theirs**, then re-run transforms (step 3).
3. Re-run transforms: `python fabric-port/remap.py` (import/type remaps),
   `python fabric-port/at2aw.py` (regenerate access widener from upstream's `accesstransformer.cfg`).
4. Fix residuals: `gradlew build` → work down compile errors; consult the residual checklist below.
5. Verify: dev client boots, smoke test (machine place/GUI/energy), gametest subset.
6. Tag `v<upstream-version>+fabric`.

### Residual checklist (things transforms can't catch)

- [ ] New `@SubscribeEvent` handlers → wire into the event glue (`mekanism.fabric_shim.event`)
- [ ] New `@EventBusSubscriber` classes → the annotation is inert on Fabric; add an explicit
      `bus.register(TheClass.class)` to the bootstrap's registration list
- [ ] New capabilities registered via `RegisterCapabilitiesEvent` → register in the Fabric lookup registrar
- [ ] New packets → confirm they flow through `PacketHandler` funnel (they should)
- [ ] New config options → no action (Forge Config API Port), but verify spec loads
- [ ] New NeoForge-only integration hooks → gate behind `ModList`-shim `isLoaded` checks
- [ ] New client extensions (`IClientItemExtensions` etc.) → add Fabric renderer registrations
- [ ] AT changes upstream → regenerate access widener; check for AW-inexpressible entries

## Bridge dependencies (verified 2026-07-04)

| Dependency | Coordinates | Version | Repo |
|---|---|---|---|
| Fabric Loader | `net.fabricmc:fabric-loader` | 0.19.3 | maven.fabricmc.net |
| Fabric API | `net.fabricmc.fabric-api:fabric-api` | 0.116.1+1.21.1 | maven.fabricmc.net |
| Fabric Loom | `fabric-loom` (plugin) | 1.17 (needs Gradle ≥9.5) | maven.fabricmc.net |
| Porting Lib | `io.github.fabricators_of_create.Porting-Lib:<module>` | 3.1.0 (1.21.1 branch) | mvn.devos.one/releases |
| Forge Config API Port | `fuzs.forgeconfigapiport:forgeconfigapiport-fabric` | 21.1.6 | raw.githubusercontent.com/Fuzss/modresources/main/maven |
| team-reborn Energy | `teamreborn:energy` | 4.1.0 | maven.modmuss50.me |
| NeoForge event bus (standalone, JiJ) | `net.neoforged:bus` | 8.0.5 (matches NeoForge 21.1.200) | maven.neoforged.net/releases |
| TypeTools (bus's listener-type resolution) | `net.jodah:typetools` | 0.6.3 | Maven Central |

Using the real (loader-independent) NeoForge bus keeps all `net.neoforged.bus.api.*` imports
(IEventBus/Event/EventPriority/SubscribeEvent) unchanged — no remap, priorities and untyped
`addListener` behave exactly as upstream expects. Verified at runtime in dev (untyped
method-reference listeners resolve; registry lifecycle posts through the bus).

Porting Lib modules of interest: `obj_loader` (transmitter OBJ models), `transfer`,
`fake_players`, `tool_actions`, `client_events`, `level_events`, `attributes`.
Exact module names/availability to be confirmed when Phase 2/4 wire them in.

Mappings: **Mojang official** (via `loom.officialMojangMappings()`) — upstream code stays
textually identical; no Yarn remap.

## Port status

- [x] Phase 0a: branch + framework scaffolding
- [x] Phase 0b: Loom build boots an empty mod (api/main source sets dormant) — dev server boot verified 2026-07-05
- [x] Phase 0c: AT→AW generation wired into build (`fabric-port/at2aw.py`; 92 live entries → 94 AW lines, validated by Loom; 2 stale upstream AT entries skip-listed)
- [ ] Phase 1: entry points, registries, config
  - [x] 1a: `mekanism.fabric_shim` skeleton — DeferredRegister/DeferredHolder/RegistryBuilder +
        RegisterEvent/NewRegistryEvent replayed on the real NeoForge bus (JiJ'd `net.neoforged:bus`)
        in NeoForge's registration order; environment shims (FMLEnvironment/Dist/OnlyIn/ModList/
        ServerLifecycleHooks/FakePlayer). Dev-server verified 2026-07-05.
  - [x] 1b: custom-registry path — RegistryBuilder shim (Fabric-backed, NewRegistryEvent fill),
        DataPackRegistryEvent over Fabric DynamicRegistries (RobitSkin), data-map types + store.
        End-to-end verification happens when main's registration code activates (1c).
  - [x] 1e: **src/api compiles on Fabric** (265 files) — scripted remap (59 mappings, 346 files,
        commit-replayable) + shims: FluidStack family (NeoForge-format codecs), ingredient family
        (item + fluid, bridged into Fabric custom ingredients under NeoForge type ids), capability
        tokens over Fabric API Lookup, data maps, FML lifecycle + IMC, fabric-port/extra.aw for
        vanilla members NeoForge ATs (Ingredient values/fromValues). Dev-server verified 2026-07-06.
  - [x] 1c: entry-point driver — no split needed: Mekanism.java's (ModContainer, IEventBus)
        constructor stays textually intact via fml shims (Mod annotation, ModContainer, FMLPaths,
        ModConfigEvent, EventBusSubscriber, ArtifactVersion/ComparableVersion). MekanismFabric
        drives FML's order: construct → registry events → ticket controllers → FMLCommonSetupEvent
        → IMC enqueue → IMC process. The actual `new Mekanism(...)` call is staged in
        MekanismFabric behind 1f. Dev-server verified 2026-07-06.
  - [x] 1d: config via Forge Config API Port — FCAP ships net.neoforged.fml.config.* and
        ModConfigSpec under their original package names (verified against the 21.1.6 jar), so
        config classes need NO remap; the ModContainer shim registers specs through FCAP's
        NeoForgeConfigRegistry and bridges FCAP's config callbacks onto the mod bus as
        ModConfigEvents (bridge only Mekanism's own container — see ModContainer#bridgeConfigEvents).
        AttachmentType builder shim done (Fabric data-attachment wiring lands with Phase 3 usage
        sites). MekanismSavedData already fixed via shim ServerLifecycleHooks.
  - [x] 1f: **src/main compiles and the mod constructs on Fabric** (census 9,060 errors
        2026-07-06 → 0 on 2026-07-11; plan + step log in fabric-port/design/main-compile-plan.md).
        src/main/java + src/fabric_client_stub/java + src/main/resources are in the default build
        (ServiceLoader impls under META-INF/services made the API's service lookups work).
        MekanismFabric constructs ModContainer → bridgeConfigEvents → `new Mekanism(...)` like
        FML's @Mod ctor. Dev-server verified 2026-07-11: "Done (3.460s)!", world loads, zero
        `neoforge:*` empty-registry errors, all 8 config TOMLs written under run/config/Mekanism/.
        Boot-surfaced shim rules (bus is strict about event-class shape — see gotchas):
        ModConfigEvent must be concrete like FML's; EntityEvent must be abstract like NeoForge's;
        FluidIngredient's built-in type registration must run after its codec fields initialize.

### Phase 1 shim semantics deviations (revisit in later phases)

| Deviation | Where | Revisit |
|---|---|---|
| `neoforge:swim_speed`/`creative_flight` attributes registered but behaviorless (nothing applies them to players; vanilla entities lack them in attribute maps) | NeoForgeMod shim | Phase 3 (default-attribute injection + movement/ability hooks) |
| TicketController → vanilla forced chunks: `ticking` flag ignored, no per-owner persistence, validation callbacks never invoked | common/world/chunk shims | Phase 3 (own SavedData with owners) |
| AddReloadListenerEvent runs synchronous listeners only (Mekanism's only listener is synchronous) | ShimGameEvents | Phase 3 if an async listener appears |
| ServerStartingEvent posted immediately before ServerStartedEvent (Fabric has no post-level-load pre-ready hook) | ShimGameEvents | acceptable |
| `NeoForgeMod.MILK` is an absent holder: `is()` matches the id, `value()`/`get()` throws | NeoForgeMod shim | Phase 2 (audit fluid tank bucket/cauldron sites) |
| `Entity.getMaxHeightFluidType()` returns the empty type (never the fluid the entity is submerged in) → mekasuit hydrostatic-repulsor swim boost stays inert | MekEntityExt inject shim | Phase 3/4 (movement/submersion hooks) |
| Non-Mekanism fluids resolve to a generic `DEFAULT` FluidType (only vanilla water/lava/empty are special-cased); Mekanism fluids carry their real type via BaseFlowingFluid. WATER/LAVA built-in FluidType property values are best-effort | fluids shim (FluidTypes) | Phase 2 (refine if a consumer needs accurate cross-mod attributes) |
| FluidType client render data (still/flowing/overlay textures, tint) + `IClientFluidTypeExtensions.getTintColor` return neutral defaults | fluids/client-extensions shim | Phase 4 (register real client fluid extensions) |
| Clientbound + configuration packet handlers are parked in `PendingClientReceivers` (wire codecs register, but no receiver dispatch); only serverbound play packets are handled live | network/registration shim (PayloadRegistrar) | Phase 4 (client entry point drains them into `ClientPlayNetworking` receivers) |
| `RegisterConfigurationTasksEvent` is not posted (NeoForge fires it per connecting client during config); `SyncAllSecurityData` is therefore never sent during the configuration phase | network/event shim + bootstrap | Phase 3 (drive config tasks off `ServerConfigurationConnectionEvents`) |
| ~~Game-bus event classes are compile-only~~ resolved 2026-07-11: tick/living/entity/block/chunk families + datapack-sync/tab-contents/attributes/spawn-placements all fire (ShimGameplayEvents + behavior mixins + bootstrap posts). Remaining timing deviations: player/entity tick events fire at the world-tick boundary (not inside each entity's tick); `EntityTickEvent.Pre` is not posted (tick-cancelling unsupported); `EntityJoinLevelEvent` fires post-add (handlers discard the entity themselves, effective same tick) | ShimGameplayEvents + LivingEntity/Entity/ChunkSerializer/ChunkMap mixins | verify timing at QA |
| `ItemAttributeModifierEvent` never fires — per-stack attribute computation needs an ItemStack mixin (`forEachModifier`); gear-module attribute tweaks (gravitational modulator speed, soul surfer, servo, disassembler/free-runner modifiers) are inert | event shims | Phase 4 (with gear/client work) |
| `ModifyDefaultComponentsEvent` + `PlayerInteractEvent` still unposted — nothing in core `src/main` listens to them (verified by census); bridge when a consumer appears | event shims | Phase 5 (integrations may listen) |
| Spawn-placement operation-merging (AND/OR/REPLACE with pre-existing entries) unsupported: duplicates log + skip — only matters when modifying other mods'/vanilla entities' placements, which core Mekanism never does | RegisterSpawnPlacementsEvent shim | acceptable |
| `common/base/holiday/ClientHolidayInfo.java` excluded from compile (client-only holiday renderer living in the common package; depends on excluded client render classes) | build.gradle 1f deferrals | Phase 4 (client) |
| ~~Empty `neoforge:*` shim registries log "Registry was empty after loading" errors~~ resolved: 1f boot shows zero empty-registry errors | NeoForgeRegistries | done (verified 2026-07-11) |
| ModConfigEvent.Loading may fire during registerConfigs (FCAP loads at registration), before Mekanism's listener subscribes — harmless: caches are lazy; listener matters for reloads. 1f boot: configs load + all TOMLs written, no related errors | ModContainer shim | verified 2026-07-11 |
| `CreativeModeTab.Builder.withSearchBar()`/`withTabFactory()` are injected no-ops: tabs are plain CreativeModeTab instances (never MekanismCreativeTab), so the search bar and custom label color are absent | MekCreativeModeTabBuilderExt | Phase 4 (mixin Builder.build() to honor the factory + search bar) |
| FluidBucketWrapper has no milk special-case (milk has no registered fluid on the port); milk buckets read as an empty fluid handler | fluids/capability/wrappers shim | Phase 2 (with the NeoForgeMod.MILK audit above) |
| Registry aliases (DeferredRegister#addAlias — mekanism:gases→chemicals et al for data components + items) are collected but not applied; old-world data under alias ids won't resolve | DeferredRegister shim (logs a WARN at boot) | Phase 3 (registry alias support) |
| Datapack registry configurators (RobitSkin's RegistryBuilder tweaks) are ignored by DataPackRegistryEvent (logs a WARN at boot) | DataPackRegistryEvent shim | Phase 3 if skin registration misbehaves |
| `mekanism:incorrect_for_disassembler`/`incorrect_for_meka_tool` block tags missing at boot (datagen output not yet imported) | resources | Phase 6 (datagen import) |
| Item-context capabilities are not cross-bridged: shim `FluidHandler.ITEM` (Void context) and Fabric's `FluidStorage.ITEM` (ContainerItemContext) don't see each other — Mekanism's own item caps work, but e.g. filling another mod's tank *item* in a Mekanism machine slot won't. Container-swap propagation has no clean mapping | transfer bridge | Phase 5/6 (revisit with FluidUtil-style usage sites + QA) |
| Bounding blocks (multiblock spill-over positions) are not exposed on the Fabric-standard lookups (the tile isn't the api handler; its shim providers proxy to the main tile) — external Fabric pipes must target the main block | TransferFallbacks expose guards | revisit if QA flags it (register per-BE-type providers that follow the proxy) |

### Hand-edit patterns for NeoForge patches to vanilla classes (recur in main)

| NeoForge patch | Replacement |
|---|---|
| `Registry#getKeyOrNull(v)` | vanilla `getKey(v)` |
| `Holder#getKey()` | `unwrapKey().orElse(null)` |
| `Holder#getData(type)` | `DataMaps.getData(holder, type)` |
| `Level/ItemStack/Entity#getCapability(...)` | `cap.getCapability(...)` on the shim token |
| `Fluid#getFluidType()` descriptions | `FluidAttributes.getDescription/Id(...)` |
| `Ingredient#isSimple/hasNoItems/getValues` | `CustomIngredients` helpers / AW'd `values` field |
| `ItemTags.create(rl)` | `TagKey.create(Registries.ITEM, rl)` |
| `new ListTag(size)` | `new ListTag()` |
| `RecipeOutput#accept(..., ICondition...)` | drop conditions arg (datagen on NeoForge branch) |
- [ ] Phase 2: capabilities + transfer/energy bridge (critical path)
  - [x] 2-core: fluid bridge implemented + guardrail-tested ahead of time
        (`mekanism.fabric_shim.transfer`: TransferUnits, ExtendedFluidTankStorage,
        StorageFluidHandler; 19 tests in `src/fabric_test`, run via `gradlew test`).
        Design locked in `fabric-port/design/transfer-bridge.md` — read it before any
        Phase 2 work; item/energy adapters follow the same patterns.
  - [x] 2-item/energy (2026-07-11): InventorySlotStorage (unchecked setter as ctor
        Consumer), StorageItemHandler (per-slot honesty: untargetable views reject),
        EnergyContainerStorage (J↔unit via IEnergyConversion, whole-unit clamping mirrors
        ForgeEnergyIntegration), StorageEnergyHandler (no unit math; Mekanism's own
        ForgeStrictEnergyHandler converts on top). 40 guardrail tests total.
  - [x] 2-consume (2026-07-11): TransferFallbacks — shim neoforge:* BLOCK lookups fall
        back to FluidStorage.SIDED / ItemStorage.SIDED / team-reborn EnergyStorage.SIDED,
        wrapped in the Storage*Handler adapters; BlockCapabilityCache now backed by
        Fabric BlockApiCache. Mekanism's own providers (side-aware proxies) have been
        registering live via TileEntityTypeDeferredRegister/ItemRegistryObject since 1f.
  - [x] 2-expose (2026-07-11, user-approved "route through proxies" design): Proxied
        Fluid/Item/EnergyStorage — operations go through the tile's own side-aware
        capability proxy (per-side permissions + configured J↔FE conversion are
        Mekanism's own code), rollback snapshots the side's containers (fluid/energy via
        unchecked setters, item slots via their NBT round-trip which Mekanism slots
        implement unchecked). Registered as guarded fallbacks on FluidStorage.SIDED /
        ItemStorage.SIDED / EnergyStorage.SIDED; the consume fallbacks now skip Mekanism
        BEs (NeoForge null-semantics + breaks the fallback↔fallback cycle). 50 guardrail
        tests. Remaining Phase 2 tails are tabled as deviations (item-context bridging,
        bounding-block exposure) — revisit at QA.
- [ ] Phase 3: events + networking
- [ ] Phase 4: client (models, renderers, shaders)
- [ ] Phase 5: integrations + API cleanup
- [ ] Phase 6: datagen import, gametests, parity QA

Full assessment and phase details: see the approved port plan (session artifact) and
`fabric-port/README.md` for transform tooling usage.
