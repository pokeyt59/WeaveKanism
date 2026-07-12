# Phase 4 — client compile plan (mekanism/client/**, 475 files)

Same playbook as 1f (`main-compile-plan.md`): census → cluster → grind in ordered steps with
predicted count drops, `-PportClient` as the local grind switch (drop the gate when green).
**Milestone framing (approved plan): Phase 4 minimal first** — client boots, world loads, machines
placeable/openable with plain JSON models; fancy renderers can land as later steps.

## Pre-census facts (2026-07-12, import inventory)

- Only **39 of 475** client files import `net.neoforged.*` at all — 120 import lines. The GUI bulk
  (~300 files under `client/gui/**`) is vanilla-typed and should mostly compile once the hubs do.
  What imports can't show: calls to NeoForge-patched vanilla methods (GuiGraphics/Screen/Font
  additions) and lost-@Override hooks — the javac census is the ground truth for those (1f found
  ~850 of that class).
- `recipe_viewer/**` (JEI/EMI) imports resolve against the compileOnly jars already wired for
  Phase 5. If their NeoForge-surface usage turns out deep, exclude `recipe_viewer/{jei,emi}/**` to
  Phase 5 rather than shimming ahead of time.
- No `mekanism/client/integration` package exists; nothing further to exclude.

## The six clusters (from the 120 import lines)

1. **Client mod-bus registration events** (ClientRegistration + ClientRegistrationUtil +
   MekanismKeyHandler + MekanismShaders + MekanismRenderer + BaseModelCache/MekanismModelCache):
   EntityRenderersEvent.*, ModelEvent.{RegisterGeometryLoaders, RegisterAdditional,
   ModifyBakingResult, BakingCompleted}, RegisterClientReloadListenersEvent,
   RegisterColorHandlersEvent, RegisterGuiLayersEvent, RegisterItemDecorationsEvent,
   RegisterKeyMappingsEvent, RegisterMenuScreensEvent, RegisterParticleProvidersEvent,
   RegisterClientExtensionsEvent, RegisterShadersEvent, TextureAtlasStitchedEvent,
   SoundEngineLoadEvent, FMLClientSetupEvent (already shimmed).
   **Pattern**: shim event classes that APPLY into Fabric/vanilla registries (exactly like
   EntityAttributeCreationEvent → FabricDefaultAttributeRegistry), posted from
   MekanismFabricClient in NeoForge's client-init order; client @EventBusSubscriber classes get an
   explicit registration list (MekanismClientEventSubscribers — same merge-checklist rule as
   common). Fabric targets: EntityRendererRegistry/EntityModelLayerRegistry, ModelLoadingPlugin,
   ResourceManagerHelper(CLIENT_RESOURCES), ColorProviderRegistry, HudRenderCallback (+ ordering),
   KeyBindingHelper, vanilla MenuScreens.register, ParticleFactoryRegistry,
   CoreShaderRegistrationCallback. RegisterItemDecorationsEvent + TextureAtlasStitchedEvent +
   SoundEngineLoadEvent have no Fabric equivalent → small mixins (GuiGraphics item-decoration
   hook; atlas-stitched tail; SoundEngine ctor/reload tail).

2. **Client game-bus events** (ClientTickHandler, RenderTickHandler, SoundHandler,
   MekanismClient): ClientTickEvent.{Pre,Post}, ClientPlayerNetworkEvent.{LoggingIn,LoggingOut},
   InputEvent.MouseScrollingEvent, RecipesUpdatedEvent, RenderLivingEvent, ViewportEvent (fog),
   RenderArmEvent, RenderGuiLayerEvent, RenderHighlightEvent, RenderLevelStageEvent,
   ScreenEvent.*, PlaySoundEvent.
   **Pattern**: `ShimClientGameplayEvents` (client sibling of ShimGameplayEvents) over
   ClientTickEvents, ClientPlayConnectionEvents, WorldRenderEvents (BLOCK_OUTLINE ≈
   RenderHighlightEvent, AFTER_* stages ≈ RenderLevelStageEvent), Fabric ScreenEvents; mixins for
   the rest (MouseHandler scroll, ClientPacketListener recipes-updated tail, LivingEntityRenderer
   pre/post, fog, ItemInHandRenderer arm, SoundEngine play).

3. **Model geometry / OBJ stack** (BaseModelCache, render/obj/**, model/energycube/**,
   model/data/**, RobitModel, baked wrappers): IGeometryLoader, IUnbakedGeometry,
   IGeometryBakingContext, StandaloneGeometryBakingContext, ObjLoader/ObjModel(+ModelSettings),
   ElementsModel, SeparateTransformsModel, DynamicFluidContainerModel, SimpleModelState,
   BakedModelWrapper, IDynamicBakedModel, ChunkRenderTypeSet, RenderTypeGroup, IQuadTransformer,
   QuadBakingVertexConsumer.
   **Decision (pre-made in the dependency matrix): Porting Lib 3.1.0 `obj_loader`** for
   ObjModel/ObjLoader; wire as modImplementation + JiJ (matrix row exists; version pinned in
   gradle.properties). Shim the geometry interfaces (IGeometryLoader et al) over Fabric's
   ModelLoadingPlugin/UnbakedModel bake path; BakedModelWrapper/IDynamicBakedModel/
   ChunkRenderTypeSet as same-surface shims (render-type sets degrade to
   BlockRenderLayerMap registrations). ModelData/ModelProperty already shimmed (TSV rows 158/159)
   — model-data flow uses Fabric's FabricBakedModel/RenderContext or the blockEntityRenderData
   hook: design the exact ModelData handoff BEFORE grinding this cluster (it's the one
   design-sensitive area of Phase 4; write `fabric-port/design/client-models.md` first).

4. **Client extensions** (RenderPropertiesProvider, ISpecialGear, MekaSuitArmor,
   MekanismArmorLayer, RenderTickHandler): IClientItemExtensions, IClientBlockExtensions,
   IClientFluidTypeExtensions (already TSV row 41). **Pattern**: shim interfaces + a
   RegisterClientExtensionsEvent shim that routes: armor models → Fabric ArmorRenderer
   registrations; block effects (particles) → mixin hooks where consumed; the BEWLR path
   (IClientItemExtensions#getCustomRenderer) → BuiltinItemRendererRegistry.

5. **Keybind conflict system** (key/**): KeyConflictContext, IKeyConflictContext, KeyModifier +
   KeyMappingLookup behavior on vanilla KeyMapping. **Pattern**: same-surface fresh shims —
   the conflict/modifier logic is self-contained (NeoForge stores it per-KeyMapping via patched
   fields → shim keeps a side map); registration itself via KeyBindingHelper.

6. **Misc**: ClientHooks (2 call sites — inspect and inline replacements),
   ConfigurationScreen/IConfigScreenFactory (FCAP ships config screens on Fabric — check the
   21.1.x FCAP artifact; else no-op the registration, it's the ModMenu config hook),
   VanillaGuiLayers (constants shim), RecipesUpdatedEvent cache in ClientTickHandler.

## Step order (counts filled in from the javac census)

| Step | What | Predicted effect |
|---|---|---|
| 0 | javac census with -PportClient (blocked on tool outage at writing; numbers TBD) | ground truth |
| 1 | [scripted] TSV remaps for mechanically-mappable client imports (Dist, EventBusSubscriber variants already mapped; add client-event rows only where a same-surface shim will exist) | import-line errors fall |
| 2 | Keybinds + client extensions + misc shims (clusters 5, 4, 6 — small, unblock many files) | key/**, render/item/** compile |
| 3 | Client event shims: mod-bus registration events (cluster 1) applying into Fabric registries + ShimClientGameplayEvents + client mixins (cluster 2) | ClientRegistration + handlers compile |
| 4 | **Design doc first**, then the model/OBJ stack (cluster 3) over Porting Lib obj_loader | model/**, render/obj/** compile |
| 5 | @Override strips + patched-vanilla-method residue (pattern table) — expect the 1f-style long tail in gui/** | remaining errors → 0 |
| 6 | Stub swap [scripted]: delete TSV rows 211–225 + 247, apply reverse remap tree-wide, delete src/fabric_client_stub (single-state MekanismClient etc.); keep server-safety via upstream's own lazy-classloading guards — boot-verify runServer after | stubs gone |
| 7 | Wire MekanismFabricClient: register client subscribers, post client lifecycle events in NeoForge order, drop -PportClient gate, commit srcDirs | client in default build |
| 8 | runClient: title screen → world → place/open Metallurgic Infuser + Basic Energy Cube (**milestone**) | verified |

Ground rules identical to 1f: census count must drop as predicted per step — stop and investigate
if it doesn't; new shims are same-surface fresh implementations (signatures from sources jars,
never copied code); `[scripted]`/`[port]` split; PORTING.md + this doc updated per step.

## Stub-swap rationale (step 6)

Common code was remapped to the `mekanism.fabric_shim.client.*` stubs so the server could boot
without the client tree. Once the real classes compile, both variants existing means split state
(two MekanismClient security maps — the config-phase sync would write the stub while GUIs read
the real one). The reversal must be one atomic scripted commit, and it is safe on the dedicated
server for the same reason upstream is: common code only *classloads* client classes behind
dist/logical-side guards that were kept textually intact by the port.
