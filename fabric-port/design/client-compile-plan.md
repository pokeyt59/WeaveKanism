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

| Step | What | Status / counts |
|---|---|---|
| 0 | javac census with -PportClient | **DONE 2026-07-12: 1,948** |
| 1 | AW batch (52 javap-verified lines) + JEI/EMI → Phase 5 exclusion | **DONE: 1,948 → 1,532** (0487efee56) |
| 2a | Keybind conflict/modifier system (cluster 5) | **DONE: 1,532 → 1,426** (cbf5eff06c; key/** = 0) |
| 2b | Client extensions + item decorators (cluster 4) + TerrainParticle ctor hand-edits | **DONE: 1,426 → 1,326** (d08f328038) |
| 3a | Client mod-bus registration event shims (cluster 1): EntityRenderersEvent, RegisterColorHandlersEvent, IGeometryLoader, ModelEvent(×4), RegisterGuiLayersEvent+VanillaGuiLayers, RegisterMenuScreensEvent (SCREENS AW), RegisterClientReloadListenersEvent, RegisterParticleProvidersEvent (SpriteParticleRegistration AW), RegisterShadersEvent, TextureAtlasStitchedEvent, sound/{SoundEvent,SoundEngineLoadEvent,PlaySoundEvent}; ClientRegistrationUtil.setPropertyOverride→ClampedItemPropertyFunction | **DONE: 1,326 → 1,166.** All mod-bus event imports resolve (0 step-3a events flagged). Note: client/(root) landed 94 not the predicted ~40 — the cluster co-locates step-3b game-bus (ClientTickHandler/MekanismClient/RenderTickHandler), step-6 stub (ClientRegistration→stub MekanismModelCache: setup/onBake at 482/495), and step-4 model residue (SeparateTransformsModel/LightedBakedModel), none in 3a scope. Surfaced-not-regressed: MekanismShaders:33 ShaderInstance(RL→String) patched-ctor=step5; MekanismStatusOverlay Gui.leftHeight/rightHeight AW=later |
| 3b-types | Client game-bus event TYPE shims (cluster 2): ClientTickEvent(Pre/Post), ClientPlayerNetworkEvent(LoggingIn/LoggingOut/Clone), InputEvent.MouseScrollingEvent, RecipesUpdatedEvent, RenderLivingEvent(Pre/Post, generic), ViewportEvent(ComputeFogColor/RenderFog), ScreenEvent(Opening/Render.Post), RenderLevelStageEvent(+Stage), RenderGuiLayerEvent.Pre, RenderArmEvent, RenderHighlightEvent.Block — call-site surface, ICancellableEvent where NeoForge cancels, no IModBusEvent | **DONE: 1,166 → 1,102.** All 11 game-bus event symbols resolve (0 flagged). Consumer residue is later-step: MekaSuitArmor.renderArm (step-4 armor), IConfigScreenFactory (cluster-6 config screen — worklist item #9 "check FCAP jar"), SoundHandler restartSounds/clearQueued (step-6 stub) |
| 3b-wiring | Runtime bridge: `ShimClientGameplayEvents` over ClientTickEvents/ClientPlayConnectionEvents/WorldRenderEvents/ScreenEvents + client-array mixins (MouseHandler scroll, RecipesUpdated tail, LivingEntityRenderer pre/post, fog color+render, ItemInHandRenderer arm, SoundEngine play/load, atlas-stitch tail, HUD-layer pre) + MekanismClientEventSubscribers; posted from MekanismFabricClient. Boot-check runServer (client mixins must stay in the "client" array) | pending |
| 4-design | `client-models.md` written + decisions locked | **DONE.** Porting Lib 1.21.1 found on devos SNAPSHOTS but user chose hand-shim (no dep). ModelData via block-view-api-v2; render-layers via FRAPI per-quad material; OBJ stubbed (transmitters, non-milestone) |
| 4-geometry | Geometry interface shims: IGeometryBakingContext (getRenderType default), IUnbakedGeometry, StandaloneGeometryBakingContext, RenderTypeGroup, SimpleModelState | **DONE: 1,102 → 948** (ef26b1b6f1 + 02884df672). Cascaded -154 through model/data, model/energycube, render/obj, render/transmitter |
| 4-baked | MekBakedModelExt inject-interface (getQuads+ModelData/getRenderPasses/getRenderTypes/applyTransform) + BakedModelMixin; BakedModelWrapper/IDynamicBakedModel/ChunkRenderTypeSet shims; ItemRenderer.renderQuadList AW | **DONE: 948 → 750** (ac48638e4d + 091a4034f3). Cascaded -198: model/baked 68→10, energycube 74→6, data 60→4. Runtime (FRAPI emit + mixin) unverifiable until step 8; client-only so runServer unaffected |
| 4-data | ModelData FRAPI emit glue + RenderDataBlockEntity mixin on TileEntityUpdateable (runtime, unverifiable until step 8) | pending |
| 4-loader | loader→ModelLoadingPlugin bridge (ShimModelLoading, step-7 overlap) | pending |
| 4-obj | OBJ stubs (ObjModel/ObjLoader/ModelSettings → missing geometry) + ElementsModel stub + IQuadTransformer constants | **DONE: 750 → 690** (91b5fecf4a + 68f19a216c). Transmitters + robit render missing (PORTING.md deviation). QuadBakingVertexConsumer (Quad) is a real vertex-format impl → own increment |
| 4-quadbaker | render/lib vertex-pipeline cluster (14 errs, byte-exact vertex format — do fresh + careful). **Fully scoped:** (a) extend `IQuadTransformer` with the full offset set COLOR/UV0/UV1/UV2/NORMAL (compute from DefaultVertexFormat.BLOCK like Porting Lib, ref models/pipeline/QuadBakingVertexConsumer.java); (b) `QuadBakingVertexConsumer` faithful pack (VertexConsumer writing quadData int[STRIDE*4] per offset; setSprite/setDirection/setTintIndex/setShade/setHasAmbientOcclusion + misc + bakeQuad()→new BakedQuad(quadData,tint,dir,sprite,shade)); (c) `MekVertexConsumerExt` inject-iface on class_4588 — `putBulkData(Pose,BakedQuad,float,float,float,float,int,int,boolean)` (unpack a quad's int[]→emit vertices, the reverse of QBVC — BakedQuadUnpacker relies on it) + `misc(VertexFormatElement,int...)` no-op default; (d) `MekBakedQuadExt` inject-iface on BakedQuad (net/minecraft/class_777) — `hasAmbientOcclusion()` via a WeakHashMap side-store QBVC.bakeQuad populates, default true (vanilla BakedQuad has only isShade). Milestone-adjacent (machine overlays), un-compile-verifiable → runClient step 8 | **DONE: 690 → 670** (8c1f12fd9e + 1ede3a51f0). Quad/Vertex/Outlines compile; byte-exact packing per BLOCK format; 0 regressions. render/lib residue: BillboardingEffectRenderer (separate effect concern) |
| 5 | @Override strips + patched-vanilla-method residue (pattern table) — the 1f-style long tail in gui/** (~390 errs). Patterns: 108 cannot-find-symbol (patched GuiGraphics/Screen/Font method calls — hand-edit per pattern table), 72 @Override-strips (NeoForge extension hooks — `strip_overrides.py`, hand-verify hook-wiring), ~32 IBarInfoHandler/IProgressInfoHandler stub-vs-real (die at step 6 stub-swap), getYSize cascades | **IN PROGRESS.** 5a: at2aw.py protected-AT→extendable (d1285f1257), 670→660. 5b: IGuiWrapper geometry getGuiLeft/getGuiTop/getXSize/getYSize added to GuiMekanism (= NeoForge AbstractContainerScreen patch; javac names only getYSize but all 4 were missing) — **660→528, 66 screens**. **KEY (classify the census FIRST — `scratchpad/classify.py`): of the remaining 528, ~102 are step-6 STUB cascades (SpecialColors TSV row 247, RenderTickHandler row 218 → LazyRender anonymous @Override cascades in RenderEnergyCube/etc., MekaSuitArmor, SoundHandler, MekanismClient) — NOT step-5 fixable. `strip_overrides.py` MUST run LAST, after genuine roots clear, else it deletes VALID @Overrides (verified: RenderEnergyCube's LazyRender overrides match the real signature exactly — pure stub cascade). Genuine residue next: onClick AbstractWidget(double,double,int) patched (8), SeparateTransformsModel/DynamicFluidContainerModel model shims (8, step-4 residue), patched GuiGraphics drawString/renderTooltip float (3, inject-iface MekGuiGraphicsExt), AW BakedOverride/addLayer (2), color-ambiguous GuiConfigTypeTab (1). 5c: onClick 3-arg bridge on GuiElement (button captured in mouseClicked — button IS load-bearing: right-click branches + isValidClickButton overrides; 2-arg made final) + ClearingEditBox hand-edit (same capture, folded into the vanilla 2-arg override) + UNSET_FG_COLOR/packedFGColor/getFGColor declared on GuiElement (NeoForge AbstractWidget patch) — **528→442** (31 @Override + 8 super-calls + 4 FG-color, exactly predicted). Override map (scratchpad/map_overrides.py): remaining does-not-override = 13 LazyRender stub-anon (RenderEnergyCube/RenderIndustrialAlarm/BillboardingEffectRenderer — step 6) + 4 GuiRotaryCondensentrator IProgressInfoHandler stub-anon (step 6) + 7 getRenderBoundingBox (BER/Particle culling hooks — strip+checklist) + MekanismElytraLayer shouldRender/getElytraTexture (strip+checklist) + MekanismCurioRenderer render (curios Phase 5 — check referencers, likely exclude) + ExtensionOverrideBakedModel getOverrides (step-4/AW batch). 5d: **442→400.** AW batch (gen_aw workflow: ItemOverrides ctor + BakedOverride class, LivingEntityRenderer.addLayer, BlockModel.bakeFace, Flame/BubbleParticle ctors — all javap-verified; removed a stale client/resources/model/ItemOverrides$BakedOverride line that never matched). MekanismShaders: `new ShaderInstance(RP, ResourceLocation, fmt)` → `new FabricShaderProgram(...)` (NeoForge patches a RL ctor; fabric-rendering-v1's ShaderProgramMixin only rewrites namespaced ids for FabricShaderProgram instances). IClientFluidTypeExtensions: +getStillTexture(FluidStack)/getFlowingTexture(FluidStack) defaults. MekanismCurioRenderer excluded (curios Phase 5). Stripped 9 BER/particle/elytra hooks ([scripted] 528c5a722c). 5e: **400→376** (both predictions exact). getMinecraft() on GuiMekanism (NeoForge Screen patch; 9 sites in GuiQIOItemViewer/GuiRobitRepair/GuiSecurityDesk — the earlier 'QIO renderTooltip' read was wrong, those were all getMinecraft). **MekGuiGraphicsExt injected interface on GuiGraphics** (class_332 in fabric.mod.json, default methods + empty GuiGraphicsMixin in the client mixin array — pattern copied from MekItemStackExt/ItemStackMixin): float drawString (drawInBatch + AW'd flushIfUnmanaged, semantics documented by upstream's own GuiUtils#drawStringNoFlush) killed both IFancyFontRenderer sites with zero call-site edits; stack-aware renderTooltip (IGuiWrapper:73) delegates to vanilla's stack-less overload — the stack only feeds Neo tooltip events, no consumers in the port. Future patched-GuiGraphics overloads go in this interface. Fresh classification at 376 (188 unique): genuine ≈ 60 — ClientRegistration 8, BaseModelCache 8, MekanismModelCache 5, TransmitterBakedModel 5 + TransmitterModelConfiguration 4 (step-4 OBJ residue), RenderTickHandler 3, SeparateTransformsModel package ×5 + neoforge.client.model package ×2 + DynamicFluidContainerModel ×1 (step-4 model shims), 2-each: ClientRegistrationUtil/MekanismClient/SoundHandler/GuiUpgradeWindow/GuiRotaryCondensentrator/EnergyCubeGeometry/MekanismStatusOverlay/MekaSuitEnergyLevel/RenderTransmitterBase, singles: ModelBaker.bake, TankInfoProvider bounds, color-ambiguous; rest = stub cascades (IBooleanProgressInfoHandler ×5, SpecialColors, LazyRender — step 6). 5f: **376→348** (14 unique, exact). ClientHooks shim (posts shim events on game bus): playSound→PlaySoundEvent (SoundHandler muffling loop), onDrawHighlight→RenderHighlightEvent.Block (RenderTickHandler blasting highlight) + TSV rows ClientHooks/IConfigScreenFactory. IConfigScreenFactory shim + registerExtensionPoint/getCustomExtension stored on shim ModContainer; MekanismClient call site adapted (FCAP's ConfigurationScreen ctor takes mod id, NOT ModContainer — javap'd FCAP 21.1.6). Gui.leftHeight/rightHeight → GuiLayerHooks constant-59 accessors (PORTING.md deviation row; writes dropped). EnergyCubeGeometry: isIdentity→equals(identity()), rotateTransform→vanilla Direction.rotate(matrix,dir). Stub additions: RenderTickHandler.clearQueued, SoundHandler.restartSounds (no-ops, die at step 6). Remaining genuine after 5f: ClientRegistration 8, model-family (BaseModelCache 8, MekanismModelCache 5, TransmitterBakedModel 5, TransmitterModelConfiguration 4, RenderTransmitterBase 3 — standalone MRL/getModelBakery/TRANSMITTER_CONTENTS, ClientRegistrationUtil 2 + SeparateTransformsModel ×5 + neoforge.client.model ×2 + DynamicFluidContainerModel, ModelBaker.bake, EnergyCubeGeometry DONE), RenderTickHandler 233/248 (MekaSuitArmor stub-cast — step 6), singles TankInfoProvider bounds + color-ambiguous. 5g: **348→328** (10 unique, exact). SeparateTransformsModel shim (Baked delegating to baseModel; baseModel/perspectives FIELD NAMES are reflection surface for FieldReflectionHelper — keep) + DynamicFluidContainerModel shim (Colors ItemColor: layer-1 bucket tint via IClientFluidTypeExtensions.of(BucketItem.content — AW'd).getTintColor()); nothing constructs Baked until 4-loader (lightBakedModel instanceof just never matches; documented in shims). ClientRegistration setRenderLayer→BlockRenderLayerMap.INSTANCE.putFluid (javap'd fabric-blockrenderlayer-v1). ItemHDPEElytra += canElytraFly mirror (ElytraItem.isFlyEnabled — vanilla static, javap'd; checklist entry ADDED-not-stripped). Singles re-verified as stub cascades: GuiTankBar INFO-bounds (IBarInfoHandler stub-vs-real), GuiConfigTypeTab color-ambiguous (SpecialColors switch), CR:318 Layer (stub MekanismStatusOverlay doesn't implement LayeredDraw.Layer — REAL one fixed in 5f), CR:238/239/241/309/474/482/495/609-612 all stub (RenderTickHandler ctor/guiOpening, SoundHandler::onTilePlaySound, MekanismKeyHandler, DataBasedModelLoader.INSTANCE, MekanismModelCache setup/onBake, MekaSuitArmor statics). **Remaining genuine = model-cache/OBJ family ONLY (~13 unique): BaseModelCache 8 + MekanismModelCache 5 + ModelBaker.bake + TransmitterBakedModel 5 + TransmitterModelConfiguration 4 + RenderTransmitterBase 3 (standalone MRL → new MRL(rl,"standalone"), getModelBakery+ModelBakerImpl → needs shim ModelBaker decision, TRANSMITTER_CONTENTS is stub). Next = 5h: read all 6 files together, one design pass (they share the cache/bake pipeline), likely folds into 4-loader.** 5h: **328→308** (10 unique; landed at 314 first — see gotcha). Re-classified: MekanismModelCache ×5 (MekaSuitArmor.ModuleOBJModelData), TransmitterBakedModel/Configuration ×9 (TransmitterModelData type-split incl. sealed-class errors) are ALL stub cascades, NOT genuine. True genuine was BaseModelCache 8 + RenderTransmitterBase 2. Fixes: **MekModelManagerExt inject (class_1092) + ModelManagerMixin** capturing ModelBakery from apply(ReloadState AW'd accessible) — real NeoForge retention semantics, works every reload; **MekModelBakerExt inject (class_7775) + ModelBakerMixin (interface mixin, HolderMixin pattern)** — 3-arg bake delegates to vanilla 2-arg (identity: all call sites construct ModelBakerImpl with Material::sprite themselves), getTopLevelModel→null routes getUnbakedModel to its own ModelBakery#getModel fallback; MRL.standalone ×3 → new ModelResourceLocation(rl, "standalone") hand-edits; BlockModel.customData ×2 → ClientModelHooks.getCustomGeometry (WeakHashMap association, 4-loader bridge populates via setCustomGeometry). **GOTCHA (now in CLAUDE.md §8): injected-interface methods MUST be default — abstract ones don't resolve at compile time at all; mixin class override beats the throwing default at runtime.** **STEP 5 GENUINE RESIDUE = ZERO. All 308 remaining = stub cascades (step 6) + does-not-override on stub anons (step 6) + BillboardingEffectRenderer/RobitBakedModel etc. cascades. Next: verify with classify.py, then STEP 6 (atomic stub swap + gate drop) is unblocked.** |
| 6 | Stub swap [scripted]: delete TSV rows 211–225 + 247, apply reverse remap tree-wide, delete src/fabric_client_stub (single-state MekanismClient etc.); keep server-safety via upstream's own lazy-classloading guards — boot-verify runServer after. **Must be ONE commit with step 7's gate drop: reverse-remapped common code does not compile while the client exclusion is active** | pending |
| 7 | Wire MekanismFabricClient: register client subscribers, post client lifecycle events in NeoForge order, drop -PportClient gate, commit srcDirs | pending |
| 8 | runClient: title screen → world → place/open Metallurgic Infuser + Basic Energy Cube (**milestone**) | pending (title screen already verified pre-grind, 1aedb033de) |

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
