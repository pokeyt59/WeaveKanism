# Phase 4 step 4 — client model / OBJ design (`mekanism/client/model/**`, render/obj, baked wrappers)

**Status: DRAFT for review — CLAUDE.md §7 design-sensitive area.** This doc must settle the model
pipeline before the step-4 grind starts. Three decisions need the user (see §7); the rest is
specified here. Written 2026-07-13 after census 1,102 (steps 3a+3b-types done).

## 1. Scope & the milestone cut

Cluster 3 of `client-compile-plan.md` (~450 errs): `render/obj/**`, `model/energycube/**`,
`model/data/**`, `model/baked/**`, `model/robit/**`, `BaseModelCache`, plus the quad utils in
`render/lib/**`. The approved milestone (step 8) is **Metallurgic Infuser + Basic Energy Cube
placeable/openable** with "plain JSON models; fancy renderers can land as later steps."

Split the cluster by milestone-criticality:

| Path | Model kind | Milestone-critical? |
|---|---|---|
| `model/energycube/**` (EnergyCubeGeometry/BakedModel/ModelLoader) | custom loader → IUnbakedGeometry → IDynamicBakedModel + ModelData | **YES** (Basic Energy Cube) |
| `model/data/**` (DataBased{Geometry,BakedModel,ModelLoader}) | custom loader → IUnbakedGeometry → BakedModelWrapper + ModelData | **YES** (machine side-config overlays; the infuser is a `data_based` model) |
| `model/baked/**` (ExtensionBakedModel, ModelDataBakedModel) | BakedModelWrapper base classes | **YES** (base types for the above) |
| geometry interfaces (IGeometryLoader/IUnbakedGeometry/IGeometryBakingContext/StandaloneGeometryBakingContext) | the loading pipeline | **YES** (everything above rides them) |
| BakedModel extensions (getQuads+ModelData, getRenderTypes/ChunkRenderTypeSet, IDynamicBakedModel) | vanilla-BakedModel additions | **YES** |
| ModelData handoff (BE `getModelData()` → render) | data flow | **YES** (energy cube LED/port sides, machine overlays) |
| `render/obj/**` + `obj.ObjModel`/`ObjLoader` (transmitters) | OBJ models | **NO** — transmitters aren't in the milestone |
| `model/robit/**` (RobitModel, ElementsModel) | robit entity model | **NO** — robit isn't in the milestone |
| `render/lib/**` quad utils (IQuadTransformer/Outlines, QuadBakingVertexConsumer/Quad) | machine outline/bolt render | **NO** — cosmetic overlays |

## 2. NeoForge model surface Mekanism uses (inventory, file map)

| NeoForge type | Used by | Role |
|---|---|---|
| `IGeometryLoader<T>` (shimmed type-only in 3a) | the 4 loaders | JSON → IUnbakedGeometry |
| `IUnbakedGeometry<T>` | DataBasedGeometry, EnergyCubeGeometry, TransmitterModel | `bake(ctx, ModelBaker, spriteGetter, ModelState, ItemOverrides)` + `resolveParents(modelGetter, ctx)` |
| `IGeometryBakingContext` | BaseModelCache + 5 geometry/config classes (11 imports) | baking config: `useBlockLight()`, `useAmbientOcclusion()`, `getModelName()`, `isGui3d()`, transforms |
| `StandaloneGeometryBakingContext` | RenderTransmitterBase | a ready-made IGeometryBakingContext for manual bakes |
| `ObjModel` / `ObjLoader` / `ObjModel.ModelSettings` | BaseModelCache (OBJModelData), render/obj/** | OBJ parse+bake |
| `ElementsModel` | RobitModel | vanilla-elements geometry |
| `SeparateTransformsModel` / `DynamicFluidContainerModel` | ClientRegistration(Util) | NeoForge built-in loaders (registered but Mekanism-owned use is thin) |
| `BakedModelWrapper<T>` | ExtensionBakedModel, ModelDataBakedModel, DataBasedBakedModel, TransmitterBakedModel | delegating BakedModel base |
| `IDynamicBakedModel` | EnergyCubeBakedModel | BakedModel whose plain `getQuads` forwards to the ModelData overload |
| `ChunkRenderTypeSet` / `RenderTypeGroup` | DataBasedBakedModel, EnergyCubeBakedModel, TransmitterBakedModel | multi-render-layer per model |
| `SimpleModelState` | EnergyCubeGeometry, TransmitterBakedModel | a ModelState |
| `IQuadTransformer` / `QuadBakingVertexConsumer` | Outlines, Quad (render/lib) | quad math |

BakedModel extension methods actually called (from EnergyCubeBakedModel / DataBasedBakedModel):
`getQuads(state, side, rand, ModelData, renderType)`, `getRenderTypes(state, rand, ModelData)` →
`ChunkRenderTypeSet`, `getRenderTypes(ItemStack, boolean)` → `List<RenderType>`, plus standard
`useAmbientOcclusion/isGui3d/getParticleIcon/getOverrides/getTransforms`. `IDynamicBakedModel.super.*`
default methods are relied on (2 sites).

ModelData production (source side): `TileEntityUpdateable.getModelData()` / `requestModelDataUpdate()`
(NeoForge BlockEntity extensions); e.g. `TileEntityEnergyCube.getModelData()` returns
`ModelData.of(SIDE_STATE_PROPERTY, CubeSideState[])`. `ModelData`/`ModelProperty` already shimmed (1f,
TSV 158/159).

## 3. Fabric target APIs (present on the classpath)

- `fabric-model-loading-api-v1` **2.0.0** — `ModelLoadingPlugin`, `ModelResolver`, custom `UnbakedModel`
  registration. The bake-path hook for the geometry loaders.
- `fabric-renderer-api-v1` **3.4.0** — `FabricBakedModel.emitBlockQuads/emitItemQuads`, `RenderContext`,
  `QuadEmitter`, `RenderMaterial`. The dynamic/data-aware quad path.
- `fabric-block-view-api-v2` **1.0.11** — `RenderDataBlockEntity` (BE implements it → `getRenderData()`)
  and `BlockRenderView.getBlockEntityRenderData(BlockPos)` (render thread reads it). **The ModelData
  carrier.**
- `fabric-rendering-data-attachment-v1` 0.3.49 — older sibling of the above (deprecated in favour of
  block-view-v2); listed only as fallback.
- `fabric-rendering-v1` **5.0.5** — `BlockRenderLayerMap` (per-block render layer), `ColorProviderRegistry`.

## 4. Design — the geometry loading + baking bridge

NeoForge auto-loads a model JSON containing `"loader": "mekanism:energy_cube"` by calling the
registered `IGeometryLoader.read()` → `IUnbakedGeometry`, then `bake()` inside the vanilla bake pass.
Fabric has no `"loader"` field. Bridge:

1. **Geometry interface shims** (`fabric_shim/client/model/geometry/`): `IUnbakedGeometry<T>`,
   `IGeometryBakingContext`, `StandaloneGeometryBakingContext` — same surface as NeoForge (signatures
   from the Mekanism call sites; §2). `IGeometryLoader` already exists (3a). These are pure interfaces
   + a small concrete `StandaloneGeometryBakingContext` (fields: name, useBlockLight, useAO, gui3d,
   transforms, ModelState) mirroring NeoForge's builder.
2. **The loader→ModelLoadingPlugin bridge** (`ShimModelLoading`, registered from `MekanismFabricClient`
   at step 7): a `ModelLoadingPlugin` whose `ModelResolver` intercepts the model ids Mekanism registers
   (`ClientModelHooks.loaders()` keys + `additionalModels()`), reads the raw JSON, detects the
   `"loader"` field, looks up the `IGeometryLoader` from `ClientModelHooks`, and returns a custom
   `UnbakedModel` wrapper. That wrapper's `bake()` calls the loader's `IUnbakedGeometry.bake(ctx, baker,
   spriteGetter, state, overrides)` with a `StandaloneGeometryBakingContext` built from the JSON's
   flags. `resolveParents` maps to `UnbakedModel.resolveParents`.
3. **Manual bake path unchanged**: `BaseModelCache.bake(IGeometryBakingContext)` and
   `registerJSONAndBake` already drive baking themselves through `ModelBakery.ModelBakerImpl` (vanilla).
   Those keep working once `IGeometryBakingContext`/`IUnbakedGeometry` resolve — no bridge needed there.

## 5. Design — BakedModel NeoForge extensions + the ModelData handoff (the §7 core)

NeoForge patches vanilla `BakedModel` with data-aware overloads. On Fabric these become an
**inject-interface** `MekBakedModelExt` (fabric interface injection + a mixin, exactly like the 1f
`MekKeyMappingExt` pattern) carrying default methods:

- `List<BakedQuad> getQuads(BlockState, Direction, RandomSource, ModelData, RenderType)` — default
  forwards to vanilla `getQuads(state, side, rand)` (ModelData ignored) so non-Mekanism models still work.
- `ChunkRenderTypeSet getRenderTypes(BlockState, RandomSource, ModelData)` — default = the model's
  single vanilla chunk layer.
- `List<RenderType> getRenderTypes(ItemStack, boolean)` — default = item layer.

`IDynamicBakedModel` = shim interface `extends BakedModel, MekBakedModelExt` whose plain
`getQuads(state, side, rand)` default-forwards to the ModelData overload with `ModelData.EMPTY`.
`BakedModelWrapper<T extends BakedModel>` = shim delegating every BakedModel + MekBakedModelExt method
to a wrapped model.

**ModelData handoff** (block-view-api-v2, the modern path):
- A mixin makes Mekanism's block entities (via `TileEntityUpdateable`) implement Fabric
  `RenderDataBlockEntity`; `getRenderData()` returns `getModelData()` (the shim ModelData object).
  `requestModelDataUpdate()` → mark the chunk section dirty (block-view-v2 re-reads on rebuild).
- **Rendering path**: a `FabricBakedModel.emitBlockQuads(blockView, state, pos, randSupplier, context)`
  default on `MekBakedModelExt` reads `blockView.getBlockEntityRenderData(pos)`, casts to `ModelData`
  (else `ModelData.EMPTY`), calls the shim `getQuads(state, side, rand, data, layer)` per culling face,
  and pushes each `BakedQuad` through the `QuadEmitter`. Item path via `emitItemQuads` → `getQuads(...,
  ModelData.EMPTY, null)`.

This keeps Mekanism's upstream `getQuads(...,ModelData,...)` bodies **unchanged** — the FRAPI shim is
the only new glue.

## 6. Design — render-type sets, quad utils, OBJ

- **`ChunkRenderTypeSet` / `RenderTypeGroup`** shims: same-surface holders. On Fabric a model can't
  freely pick per-quad chunk layers the way NeoForge does; `MekBakedModelExt.getRenderTypes(...)` is
  consumed by the FRAPI emit path which sets the `RenderMaterial` blend mode per layer. Coarser
  fallback: `BlockRenderLayerMap.putBlock(block, layer)` for the model's primary layer (energy cube =
  cutout). See §7 decision C.
- **Quad utils**: `IQuadTransformer` (Outlines) and `QuadBakingVertexConsumer` (Quad) are self-contained
  vertex math over vanilla `BakedQuad`/`VertexFormat`; fresh same-surface shims. Not milestone-critical
  → land last.
- **OBJ (`ObjModel`/`ObjLoader`)**: **blocked — see §7 decision A.** Porting Lib has no 1.21.1 release
  (mvn.devos.one caps every module at `2.3.15+1.20.1`; the pinned `porting_lib_version=3.1.0` matches
  nothing there). Transmitters are the only OBJ users and are outside the milestone.

## 7. OPEN DECISIONS (need the user)

**A. OBJ / Porting Lib strategy (pivotal).** Porting Lib 1.21.1 is not on mvn.devos.one (verified:
`base/core/transfer/fluids/obj_loader` all cap at `2.3.15+1.20.1`; `Porting-Lib` is the only artifact
under the group). Options:
- **A1 (recommended): defer OBJ.** Stub `ObjModel`/`ObjLoader`/`ObjModel.ModelSettings` so
  `ObjLoader.INSTANCE.loadModel(...)` returns a placeholder/`missing` geometry; transmitters render as
  the missing model. Milestone (infuser + energy cube) is unaffected; wire real OBJ in a later phase.
  Aligns with "Phase 4 minimal first."
- **A2: port a minimal OBJ loader ourselves** (fresh impl — parse `.obj`/`.mtl`, bake to `BakedQuad`s).
  Large, LGPL-clean effort; only transmitters need it.
- **A3: hunt for Porting Lib 1.21.1 elsewhere** (Modrinth maven / Jared's maven / a different devos
  path). Uncertain it exists; the 1.20.1 cap suggests the model modules stalled.
- **A4: vendor Porting Lib's `obj_loader` source** (LGPL — legally includable with attribution, but the
  port's ground rule is "don't copy LGPL bodies"; would be an explicit exception).

**B. ModelData handoff mechanism.** Recommended **block-view-api-v2** `RenderDataBlockEntity` +
`getBlockEntityRenderData` (modern, present). Alternative: `fabric-rendering-data-attachment-v1` (older,
deprecated) or a Mekanism-owned side map. Recommendation: block-view-v2.

**C. Multi-render-layer fidelity (`ChunkRenderTypeSet`).** Recommended **FRAPI per-quad material** in
the emit shim (honours the model's `getRenderTypes` set; needed because the energy cube mixes
solid+cutout LEDs). Coarser alternative: `BlockRenderLayerMap` per-block (one layer/block — would flatten
the energy cube's multi-layer look). Recommendation: FRAPI material path, BlockRenderLayerMap only if the
emit path proves too costly for the milestone.

## 8. Build / verify plan (once A–C are settled)

1. Geometry interface shims (`IUnbakedGeometry`, `IGeometryBakingContext`, `StandaloneGeometryBakingContext`)
   + TSV + remap → census drop for `model/data`, `model/energycube` import errors.
2. `MekBakedModelExt` inject-interface + mixin + `IDynamicBakedModel`/`BakedModelWrapper`/`ChunkRenderTypeSet`/
   `RenderTypeGroup`/`SimpleModelState` shims → clears `model/baked`, `getQuads`/`getRenderTypes` residue.
3. ModelData handoff mixin (`RenderDataBlockEntity`) + FRAPI emit glue on `MekBakedModelExt`.
4. Loader→ModelLoadingPlugin bridge (`ShimModelLoading`), posted from MekanismFabricClient (step 7 overlap).
5. OBJ per decision A; quad utils (`IQuadTransformer`/`QuadBakingVertexConsumer`) last.
6. Verify: census drops per step; `compileJava test` green; the **energy cube + infuser** models load at
   step 8 runClient (expect missing datagen models = Phase 6, cosmetic).

Every new shim: signatures from the Mekanism call sites / vanilla javap (never copied bodies); TSV +
remap `[scripted]`, shims `[port]`. ModelData semantics that shape render output get a guardrail test
only if they encode WORLD/WIRE data — model glue is pure client compile/render, so no test (per §7).
