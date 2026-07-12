# Phase 4 handoff (2026-07-12, Fable → successor session)

You are continuing the Phase 4 client compile grind mid-flight. **Read order: repo CLAUDE.md →
this file → `client-compile-plan.md` (the step scoreboard there is the source of truth for
progress).** This file exists so you don't have to ask the user anything that's already decided:
every open decision is either listed in §5 or belongs to the "stop and ask" categories in
CLAUDE.md §7. Everything else — derive it from ground truth using §2, don't ask.

## 1. Exact current state

- Branch `fabric/1.21.x`, ~14 unpushed commits (user pushes; NEVER push). Working tree clean
  except `fabric-port/census-raw.log` + `client-census-raw.log` (never commit these).
- Phases 0–3 complete. Phase 4: client entry point live (`MekanismFabricClient` drains
  PendingClientReceivers; first `runClient` reached the title screen). Compile grind at
  **census 1,326** (from 1,948) with steps 0–2b done — see the scoreboard.
- Task list: #24 tracks the grind. Guardrail tests: 54, all green. `-PportClient` = grind
  switch (includes mekanism/client/** in compileJava; the DEFAULT build keeps it excluded).
- Census snapshot (top clusters remaining): client root 178 (ClientRegistration 98 +
  ClientTickHandler + MekanismClient), gui/element 176, render/obj 154, gui/machine 86,
  model 82, render 80, model/energycube 74, gui 74, model/baked 68, model/data 60,
  render/tileentity 60, render/armor 48, render/lib 36 — then a long tail.

## 2. Ground-truth sources (use these before writing ANY code)

| Question | Source | How |
|---|---|---|
| Vanilla 1.21.1 signature/field/descriptor | mojmap merged jar | `'/c/Program Files/Adobe/Adobe Animate 2024/jre/bin/javap.exe' -p -s -cp "C:/Users/Newpi/.gradle/caches/fabric-loom/minecraftMaven/net/minecraft/minecraft-merged/1.21.1-loom.mappings.1_21_1.layered+hash.2198-v2/minecraft-merged-1.21.1-loom.mappings.1_21_1.layered+hash.2198-v2.jar" <fqcn>` (no JDK on PATH — this Adobe jre javap is the one) |
| NeoForge API shape (signatures ONLY — LGPL, never copy bodies) | neoforge-26.1.2.76-sources.jar in `~/.gradle/caches/modules-2/files-2.1/net.neoforged/neoforge/...` | unzip the package you need to the scratchpad. 26.1 ≠ 21.1 in places (fog methods, Identifier vs ResourceLocation) — when they differ, **Mekanism's own overrides/call sites are the authoritative 21.1 surface** |
| What slice of a NeoForge surface to shim | Mekanism's call sites | grep mekanism/client for the members actually used; shim exactly that, nothing more |
| Fabric API surface for the pinned version | remapped module sources in `D:/mc/MODEVS/Mekanism/.gradle/loom-cache/remapped_mods/remapped/net/fabricmc/fabric-api/<module>/.../*-sources.jar` | unzip to scratchpad; also check the module's `*.mixins.json` inside the impl jar to learn which vanilla classes Fabric already hooks |
| Ambiguous bytecode-level questions (param roles, ctor literals) | `javap -c` the vanilla class | e.g. how updateChunkScheduling's slots map, what literal setChunkForced passes |

## 3. The method (copy this reasoning, step by step)

Per grind step, in order — this loop produced every count drop so far:

1. **Pick the next cluster from the scoreboard.** Run
   `./gradlew compileJava -PportClient --console=plain > census.log 2>&1`, then
   `python fabric-port/census_cluster.py census.log` (histograms) and
   `... --pkg /client/<pkg>/` (per-cluster detail with context lines).
2. **Enumerate the missing surface** from the error list + Mekanism call-site greps. Write the
   list down before coding. Predict the count drop.
3. **Choose the established pattern** (don't invent new ones — find the analogous existing
   artifact and mirror its structure):
   - Missing NeoForge class → same-surface fresh shim in `fabric_shim` (+ TSV row +
     `python fabric-port/remap.py` = its own `[scripted]` commit; import-line-only rewrites).
     Examples: `client/settings/*`, `client/extensions/*`.
   - NeoForge-patched vanilla *method* → inject interface `fabric_shim/inject/Mek*Ext` (default
     methods) + row in fabric.mod.json `loom:injected_interfaces` (INTERMEDIARY class name — get
     it from linkie/yarn or an existing row) + interface-target mixin in
     `mekanism.fabric.mixin` (client-only classes go in the mixins.json `"client"` array!).
     State goes in a `*Hooks` side store (WeakHashMap keyed by instance). Examples:
     MekKeyMappingExt + KeyMappingHooks + KeyMappingMixin; MekRegistryExt.
   - NeoForge-patched vanilla *constructor* → cannot be injected; hand-edit the subclass
     `super(...)` call sites to vanilla ctor + setters, `//fabric-port:` comment. Example:
     MekKeyBinding.
   - NeoForge-patched private-access widening → AW batch: census "has private access in X"
     lines → `fabric-port/gen_aw.py members.txt` (javap-verified descriptors) → append to
     `fabric-port/extra.aw` → `python fabric-port/at2aw.py`. NEVER edit the generated
     accesswidener, never hand-write descriptors.
   - NeoForge registration event → shim event whose register() APPLIES into the Fabric/vanilla
     registry immediately (RegisterKeyMappingsEvent→KeyBindingHelper pattern) or records into a
     Hooks store when dispatch needs a later hook (RegisterItemDecorationsEvent). Posted later
     from MekanismFabricClient in NeoForge's client-init order (step 7).
   - NeoForge patch that duplicates a vanilla overload → hand-edit to the vanilla form
     (TerrainParticle.updateSprite == pos-taking ctor). Check vanilla FIRST via javap — the
     cheapest fix is often "the vanilla API already does this".
   - Whole subsystem that's Phase 5 (JEI/EMI/curios/…) → build.gradle exclusion + stand-in
     records where a guarded call site needs types (guard must be provably false on the port,
     e.g. `hooks.curios.isLoaded()`).
4. **Stub-surface gaps**: common code imports `mekanism.fabric_shim.client.*` STUBS
   (fabric_client_stub source set), real client code needs more members on them → extend the
   stub with the REAL upstream behavior (client classes are on the compile classpath; these
   only run on the physical client). Examples: MekanismClient.updateKey, SoundHandler.playSound.
5. **Verify**: re-run the census — the cluster you targeted must be ~0 and the total must drop
   by roughly your prediction. Off by a lot → STOP, investigate, don't proceed on top of a
   surprise. Then `./gradlew compileJava test` (DEFAULT build, no -PportClient) must be green —
   fabric_shim/fabric_client_stub changes affect it even when client code is excluded.
6. **Commit**: `[scripted]` remap commits separate from `[port]` hand/shim commits (a file with
   both goes in [port], note it). Trailer per CLAUDE.md. Update the scoreboard in
   client-compile-plan.md as part of the step's [port] commit.
7. **Boot checks** only when the default build's runtime surface changed (mixins.json, shim
   init paths, resources): `./gradlew runServer` in background, watch for `Done \(` (never kill
   on the string "Error" — known-benign noise exists). **Kill the server before ANY other
   boot** — a leftover java process holds the world session.lock and the next boot fails with
   "Failed to start" (it happened twice this session):
   PowerShell: `Get-CimInstance Win32_Process -Filter "Name = 'java.exe'" | Where-Object { $_.CommandLine -match 'DevLaunchInjector' } | ForEach-Object { Stop-Process -Id $_.ProcessId -Force }`

## 4. Refinement process (how to match the previous results)

- **Calibrate against existing artifacts before writing**: open the analogous committed shim /
  mixin / event and mirror its javadoc style ("Same surface as …; fresh implementation"),
  comment density (comments state constraints, not narration), naming (Mek*Ext, *Hooks,
  Shim*, mekanism$ mixin prefixes), and structure. If your draft looks structurally different
  from its nearest committed neighbor, that's a signal to re-check.
- **Prediction discipline**: every step declares an expected census drop first. The two numbers
  so far: predicted ~1540 after step 1 → got 1532; predicted key/**→0 → got 0. If your actual
  misses by >~20%, treat it as a bug in your understanding, not noise.
- **Surface minimalism check**: after a cluster clears, grep your new shim for public members
  never referenced by mekanism/** — if any exist, you shimmed speculation; trim it.
- **Descriptor/name paranoia**: any AW line, mixin target, intermediary name, or method
  descriptor must come from javap/extracted sources, not memory. The one compile failure this
  session came from a remembered-but-wrong accessor (`getBlockEntityRenderer()`).
- **Semantics tests**: when a shim encodes behavior that shapes WORLD DATA or WIRE bytes
  (serializers, formats), add a guardrail test in src/fabric_test pinning it (see
  ForcedChunksSavedDataTest / AttachmentPersistenceCodecTest for the shape). Pure compile-glue
  needs no test.
- **End-of-step ledger**: scoreboard row updated, PORTING.md deviation rows for any semantic
  gap you deliberately left (find the table, mirror row style), memory updated only at
  session-scale milestones.

### Per-commit refinement checklist (run it literally, every time)

1. Did every vanilla member I referenced come from a javap/extracted-source check this session?
   (One remembered accessor = the one build break so far.)
2. Does the DEFAULT `compileJava test` pass — not just the -PportClient census compile? New
   fabric_shim files are in the default build even though client code isn't.
3. Is the census total ≥ my prediction's ballpark, and is my target cluster at ~0? If the
   cluster still has errors, they're either (a) missed surface — extend the shim now, or
   (b) provably another cluster's types — name that cluster in the commit message.
4. [scripted] and [port] separated? Nothing staged from census logs? Trailer present?
5. Would the nearest committed neighbor (same pattern) look structurally like my new file?
   If I invented a pattern, is there truly no committed precedent? (Search first.)
6. If I touched mixins.json / bootstrap init paths / resources: did I boot-check runServer —
   and kill the previous java process first?

### When something surprises you

Two failed attempts at the same fix = stop; re-read the relevant plan section + re-run the
census detail for that file; the answer is usually a wrong assumption about which cluster the
error belongs to, or a NeoForge-vs-vanilla surface difference javap settles in one call. Do NOT
widen scope to "fix nearby things" mid-surprise. If the surprise is a genuine design gap the
docs don't cover (CLAUDE.md §7 areas), write the question down in the scoreboard row and ask
the user in one batched message instead of piecemeal.

## 5. Open decisions & traps specific to the next steps

- **Step 3 (client events, ~350-400 errs)**: mirror the COMMON side — ShimGameplayEvents /
  MekanismEventSubscribers / MekanismFabric's ordered posts are the template. Client
  @EventBusSubscriber classes get an explicit `MekanismClientEventSubscribers` list
  (upstream-merge checklist item, like the common one). Registration events post from
  MekanismFabricClient; game-bus client events bridge in a `ShimClientGameplayEvents` over
  ClientTickEvents / ClientPlayConnectionEvents / WorldRenderEvents / ScreenEvents, with small
  client mixins for the hookless ones (MouseHandler scroll, RecipesUpdated tail,
  LivingEntityRenderer pre/post, fog, ItemInHandRenderer arm, SoundEngine play). Events whose
  only listeners are Phase-5 integrations: census the listeners first; if none in core, don't
  post — deviation row instead (ModifyDefaultComponents precedent).
- **Step 4 (model/OBJ, ~450 errs)**: **write `fabric-port/design/client-models.md` FIRST** —
  this is the one design-sensitive area of Phase 4 (CLAUDE.md §7 rule applies: if the doc can't
  settle a case, ask the user). Fixed points already decided: Porting Lib `obj_loader` 3.1.0
  (dependency matrix row exists; maven repo `mvn.devos.one` already in build.gradle; the exact
  artifact version string must be verified against the maven listing before wiring — expect
  `io.github.fabricators_of_create.Porting-Lib:obj_loader:<version>+1.21.1`); NeoForge geometry
  interfaces (IGeometryLoader/IUnbakedGeometry/IGeometryBakingContext/…) become shims over
  Fabric's ModelLoadingPlugin; BakedModel NeoForge extensions (getRenderPasses/getRenderTypes/
  getQuads+ModelData/ChunkRenderTypeSet) need an inject-interface + the ModelData handoff
  design (ModelData/ModelProperty shims already exist from 1f). MekanismISTER +
  RenderEnergyCubeItem residues belong here.
- **Step 5 (gui tail ~500)**: expect 1f-style residue — @Override strips (strip_overrides.py
  exists but hand-verify hook-wiring entries), PATCHED GuiGraphics/Screen/AbstractContainerScreen
  METHOD calls (hand-edit per the PORTING.md pattern table; add new patterns to that table),
  IBarInfoHandler-style stub-vs-real type mismatches that only fully die at step 6.
- **Step 6+7 (atomic!)**: the stub swap (reverse TSV rows 211–225 + 247, delete
  fabric_client_stub, single-state MekanismClient) MUST be one commit with the -PportClient
  gate drop — reverse-remapped common code doesn't compile while the client exclusion is
  active. runServer boot-verify right after (server must never classload client classes —
  upstream's own guards were kept textually intact and are the protection).
- **Step 8 milestone**: runClient → create world → place/open Metallurgic Infuser + Basic
  Energy Cube. GUI opening exercises menus + networking; energy transfer exercises the Phase 2
  bridge. Expect missing datagen models (116 known) — cosmetic, Phase 6.
- `common/base/holiday/ClientHolidayInfo` (4 errs) rides whichever step clears
  QuadTransformation (model cluster).

## 6a. Step 3a worklist — MID-FLIGHT, resume here (2026-07-12)

Step 3a = all client MOD-BUS event shims so ClientRegistration/Util + MekanismShaders/
MekanismRenderer/SoundHandler's mod-bus imports resolve. DONE so far (d95851448e):
`client/event/EntityRenderersEvent` (3 nested), `client/event/RegisterColorHandlersEvent`
(Block/Item → ColorProviderRegistry), `client/model/geometry/IGeometryLoader` (type only).

Remaining files to write (every fact below is already verified — do not re-derive):

1. `client/event/ModelEvent.java` — abstract outer + 4 nested (all IModBusEvent):
   - `RegisterGeometryLoaders`: `register(ResourceLocation, IGeometryLoader<?>)` → store in a
     new `client/model/ClientModelHooks` (Map<ResourceLocation, IGeometryLoader<?>>).
   - `RegisterAdditional`: `register(ModelResourceLocation)` → ClientModelHooks set (step 4
     feeds ModelLoadingPlugin.addModels).
   - `ModifyBakingResult`: ctor-injected `Map<ModelResourceLocation, BakedModel> getModels()`
     (`ModelResourceLocation.id()` is vanilla — a record component — no shim needed there).
   - `BakingCompleted`: ctor-injected getModels() + `ModelBakery getModelBakery()` (check
     MekanismModelCache.onBake/BaseModelCache for the exact members it reads before finalizing).
2. `client/event/RegisterGuiLayersEvent.java` — `registerBelowAll(ResourceLocation,
   LayeredDraw.Layer)` + `registerAbove(ResourceLocation vanillaId, ResourceLocation,
   LayeredDraw.Layer)` (LayeredDraw.Layer is vanilla 1.21.1) → ordered store (new small
   GuiLayerHooks or a section in ClientExtensionsHooks); dispatch = HudRenderCallback at step 7
   (vanilla-relative ordering approximated by registration order — deviation row).
   Also `client/gui/VanillaGuiLayers.java` — constants shim; Mekanism uses ARMOR_LEVEL,
   SELECTED_ITEM_NAME, SUBTITLE_OVERLAY (ResourceLocation.withDefaultNamespace of those paths).
3. `client/event/RegisterMenuScreensEvent.java` — `<M extends AbstractContainerMenu, S extends
   Screen & MenuAccess<M>> register(MenuType<? extends M>, MenuScreens.ScreenConstructor<M, S>)`.
   **Vanilla has NO public MenuScreens.register** — AW the private static `SCREENS` map
   (`accessible field net/minecraft/client/gui/screens/MenuScreens SCREENS Ljava/util/Map;`)
   and put directly (raw cast).
4. `client/event/RegisterClientReloadListenersEvent.java` — `registerReloadListener(
   PreparableReloadListener)` → ResourceManagerHelper.get(PackType.CLIENT_RESOURCES)
   .registerReloadListener wrapping in an IdentifiableResourceReloadListener with a generated
   `mekanism:client_reload_<n>` id (mirror ShimGameEvents' server-side wrapper).
5. `client/event/RegisterParticleProvidersEvent.java` — `registerSpriteSet(ParticleType<T>,
   ParticleEngine.SpriteParticleRegistration<T>)` → Fabric
   `ParticleFactoryRegistry.getInstance().register(type, registration::create)` (Fabric's
   FabricSpriteProvider extends vanilla SpriteSet, so the method ref adapts directly).
6. `client/event/RegisterShadersEvent.java` — data-carrying: ctor `ResourceProvider`;
   `getResourceProvider()`; `registerShader(ShaderInstance, Consumer<ShaderInstance>)` collects
   into a list the poster consumes (post from CoreShaderRegistrationCallback at step 7; close
   replaced instances on re-post). MekanismShaders is the only user.
7. `client/event/TextureAtlasStitchedEvent.java` — ctor-injected `TextureAtlas getAtlas()`
   (game-bus? NeoForge fires on MOD bus — check MekanismRenderer's subscription: it's
   @SubscribeEvent in a @EventBusSubscriber(modid, value=Dist.CLIENT) class → MOD bus,
   IModBusEvent). Posted from an atlas-upload tail mixin at step 3b/7.
8. `client/event/sound/SoundEngineLoadEvent.java` — ctor-injected `SoundEngine getEngine()`,
   MOD bus. `client/event/sound/PlaySoundEvent.java` — GAME bus: fields engine, String name,
   SoundInstance originalSound, @Nullable SoundInstance sound; getSound/setSound/
   getOriginalSound/getName (SoundHandler lines ~255-285 are the exact consumer).
9. `MekanismClient`'s ConfigurationScreen/IConfigScreenFactory imports: **FCAP 21.1.6 ships both
   under `net.neoforged.neoforge.client.gui.*` on Fabric — NO remap, NO shim** (verified in the
   remapped FCAP jar). If the census still flags them, check IConfigScreenFactory really is in
   the jar before doing anything else.
10. TSV rows for all of the above under their NeoForge names (client.event.*,
    client.event.sound.*, client.gui.VanillaGuiLayers, client.model.geometry.IGeometryLoader)
    → remap.py → separate [scripted] commit. Then re-census: predict client root ~178 → ~40
    (residue = SeparateTransformsModel/DynamicFluidContainerModel + model-typed errors that
    belong to step 4).
11. Hand-edit: ClientRegistrationUtil.setPropertyOverride's param type ItemPropertyFunction →
    ClampedItemPropertyFunction (vanilla ItemProperties.register wants the clamped type;
    NeoForge widens it — call-site lambdas fit both). One line + //fabric-port comment.

Then step 3b (game-bus client events + mixins): ClientTickEvent.Pre/Post →
ClientTickEvents; ClientPlayerNetworkEvent.LoggingIn/LoggingOut → ClientPlayConnectionEvents
JOIN/DISCONNECT; RenderHighlightEvent.Block → WorldRenderEvents.BLOCK_OUTLINE;
RenderLevelStageEvent → WorldRenderEvents stages; ScreenEvent.Opening (RenderTickHandler
guiOpening) → ScreenEvents; mixins for MouseScrollingEvent (MouseHandler), RecipesUpdatedEvent
(ClientPacketListener.handleUpdateRecipes tail), RenderLivingEvent.Pre/Post
(LivingEntityRenderer, cancellable Pre), ViewportEvent fog (check ClientTickHandler's exact
subevent first), RenderArmEvent (ItemInHandRenderer, cancellable), RenderGuiLayerEvent.Pre
(check RenderTickHandler usage), PlaySoundEvent (SoundEngine.play), TextureAtlasStitchedEvent
(atlas upload tail), SoundEngineLoadEvent (SoundEngine ctor/reload tail). All client-only
mixins go in the mixins.json "client" array.

## 6. Standing rules that bit before (don't relearn)

Everything in CLAUDE.md §3/§7/§8 plus, from this session: config-cache forbids `project.*`
inside task-execution closures (capture to a local at configuration time); Fabric interface
injection covers compile only — the runtime needs the interface-target mixin too; client-only
mixins in the `"client"` array or the dedicated server dies; equal region tickets dedup in
vanilla's SortedArraySet (refcount before removing); `Codec.PASSTHROUGH` + Dynamic(NbtOps) is
the pattern for Tag-shaped codecs; a serializer whose write() returns null means "skip" — never
turn that into an encode error (envelope pattern in AttachmentHooks). The permission classifier
occasionally goes down for minutes — do read-only prep (Grep/Read/javap extraction lists) and
retry rather than idling.
