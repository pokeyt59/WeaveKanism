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
  - [ ] 1f: main source set compiling (the long tail; overlaps Phases 2-3). Enable
        'src/main/java' in build.gradle srcDirs, work the compile-error clusters down using the
        pattern table below + new shims; keep the srcDirs change uncommitted until green.
        Then uncomment the mod-construction block in MekanismFabric.onInitialize.

### Phase 1 shim semantics deviations (revisit in later phases)

| Deviation | Where | Revisit |
|---|---|---|
| `neoforge:swim_speed`/`creative_flight` attributes registered but behaviorless (nothing applies them to players; vanilla entities lack them in attribute maps) | NeoForgeMod shim | Phase 3 (default-attribute injection + movement/ability hooks) |
| TicketController → vanilla forced chunks: `ticking` flag ignored, no per-owner persistence, validation callbacks never invoked | common/world/chunk shims | Phase 3 (own SavedData with owners) |
| AddReloadListenerEvent runs synchronous listeners only (Mekanism's only listener is synchronous) | ShimGameEvents | Phase 3 if an async listener appears |
| ServerStartingEvent posted immediately before ServerStartedEvent (Fabric has no post-level-load pre-ready hook) | ShimGameEvents | acceptable |
| `NeoForgeMod.MILK` is an absent holder: `is()` matches the id, `value()` throws | NeoForgeMod shim | Phase 2 (audit fluid tank bucket/cauldron sites) |
| Empty `neoforge:*` shim registries log "Registry was empty after loading" errors | NeoForgeRegistries | self-resolves when main's registrations land (1f) |
| ModConfigEvent.Loading may fire during registerConfigs (FCAP loads at registration), before Mekanism's listener subscribes — harmless: caches are lazy; listener matters for reloads | ModContainer shim | verify during 1f boot |

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
- [ ] Phase 3: events + networking
- [ ] Phase 4: client (models, renderers, shaders)
- [ ] Phase 5: integrations + API cleanup
- [ ] Phase 6: datagen import, gametests, parity QA

Full assessment and phase details: see the approved port plan (session artifact) and
`fabric-port/README.md` for transform tooling usage.
