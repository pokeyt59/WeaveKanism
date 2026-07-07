# Mekanism → Fabric port — Claude working brief

You are continuing an in-progress port. **Read this file, then `PORTING.md`, before writing any
code.** The full phase roadmap lives in the approved plan file:
`C:\Users\Newpi\.claude\plans\take-a-deep-look-delegated-liskov.md` (read it once per session when
planning larger slices).

## 1. Goal

Port **core Mekanism** (`src/api` + `src/main`; Additions/Generators/Tools later) from
NeoForge/MC 1.21.1 to **Fabric**, on branch `fabric/1.21.x`, structured as a **repeatable porting
framework** (the "Create: Fabric" model): upstream is a third party we don't control, so every
upstream release must be re-portable via `git merge` + re-run scripted transforms + fix a bounded
set of residuals. This means:

- **Minimal diff against upstream.** Never restructure upstream files; prefer shims that keep
  NeoForge class names so changes are import-line-only.
- `1.21.x` is the pristine upstream branch — **never commit port work there**.
- Mechanical rewrites are **scripted** (`fabric-port/remap.py` + mapping TSV) and committed
  separately with the `[scripted]` prefix so they replay after merges. Hand edits use `[port]`.
- Shims live in `src/fabric_shim/java/mekanism/fabric_shim/` and are **fresh implementations of
  NeoForge API surfaces** (NeoForge is LGPL; do not copy its code — reference sources may be
  consulted for *signatures* only).

First milestone: Fabric dev client boots, world loads, a Metallurgic Infuser + Basic Energy Cube
can be placed, opened, and store/transfer energy.

## 2. Current state (2026-07-06) and TODO

Done (see `PORTING.md` "Port status" — that section is the source of truth, keep it updated):

- Phase 0 complete: Loom build (Mojang mappings), AT→AW generation, transform pipeline.
- Phase 1 a–e complete: full shim registration lifecycle on the **real NeoForge event bus**
  (`net.neoforged:bus:8.0.5` JiJ'd — `net.neoforged.bus.api.*` imports need NO remap);
  `src/api` (265 files) compiles and a dev server boots clean.
- Phase 1 c/d complete: FML shims (Mod/ModContainer/FMLPaths/ModConfigEvent/…), config via Forge
  Config API Port, game-event glue (`ShimGameEvents`), chunk tickets, attachment-type surface.
  `MekanismFabric.onInitialize` drives FML's full lifecycle order.

TODO, in order:

1. **Phase 1f — make `src/main` compile** (~470 files still import `net.neoforged.*`). This is
   the current task. Workflow in §4 below. When it compiles, uncomment the mod-construction block
   in `src/fabric/java/mekanism/fabric/MekanismFabric.java`.
2. **Phase 2 — capabilities + transfer/energy bridge** (critical path): the fluid bridge CORE IS
   ALREADY IMPLEMENTED AND TESTED (`mekanism.fabric_shim.transfer.*`, suite in
   `src/fabric_test`). Remaining: item/energy adapters (follow the identical patterns), the 11
   `RegisterCapabilitiesEvent` sites via Fabric API Lookup, `BlockCapabilityCache`. **Read
   `fabric-port/design/transfer-bridge.md` first — it locks the design; do not redesign.**
3. **Phase 3 — events + networking**: `PacketHandler` funnel → Fabric play networking (57
   packets untouched); remaining event glue + small mixins (`ChunkTicketLevelUpdatedEvent`);
   data-map JSON loader; registry alias support; chunk-ticket owner persistence; attachment
   data access wiring (`getData`/`setData` sites) over `fabric-data-attachment-api-v1`.
4. **Phase 4 — client**: model loaders (OBJ via Porting Lib), BEWLR → `BuiltinItemRendererRegistry`,
   core shaders, `FluidRenderHandlerRegistry`, keybinds, HUD. Entry point
   `mekanism.fabric.client.MekanismFabricClient` (declared in fabric.mod.json, not written yet).
5. **Phase 5 — integrations** (JEI/EMI compileOnly deps already wired), **Phase 6 — datagen
   import + gametests + parity QA**. Then the 1.20.1 track (see PORTING.md branch model).

## 3. Golden rules

- Work on `fabric/1.21.x` only. **Never `git push`** (no remote for the port yet).
- Keep the build green: before every commit run `.\gradlew compileJava` **and `.\gradlew test`**
  (19+ guardrail tests in `src/fabric_test` — wire formats + transfer-bridge semantics);
  boot-verify with `runServer` for lifecycle-touching changes. Never commit a broken tree, never
  commit with failing or weakened tests.
- Commit messages: `[port]` or `[scripted]` prefix + trailer
  `Co-Authored-By: Claude Fable 5 <noreply@anthropic.com>`.
- New import remappings go in `fabric-port/mappings/neoforge-to-fabric.tsv` (tab-separated,
  old→new), then run `python fabric-port/remap.py`, commit the rewrites as their own
  `[scripted]` commit.
- If upstream ATs a vanilla member you need, add the AW line to `fabric-port/extra.aw` and re-run
  `python fabric-port/at2aw.py` (regenerates `src/fabric/resources/mekanism.accesswidener`).
- The user sometimes makes their own checkpoint commits ("scott codes") — leave them intact,
  never rebase/rewrite history.
- Do not add libraries without checking `PORTING.md`'s dependency matrix; versions are pinned in
  `gradle.properties`. Gradle is 9.6 — all version strings need 3 parts; config cache is ON.

## 4. Phase 1f workflow (the compile grind)

**The full battle plan, with the error census already taken and every cluster decision made,
is `fabric-port/design/main-compile-plan.md` — follow its 6 steps in order.** Summary: the census
(9,060 errors, 2026-07-06) showed ~5,700 are Phase 4/5 areas to exclude (client/**,
common/integration/** minus the energy core), ~850 fall to a scripted @Override strip (NeoForge
extension hooks — maintain the hook-wiring checklist!), ~400 to a data-component Holder-overload
bridge (interface injection + the port's first mixins), and the rest to a listed trivial-shim
batch, three design shims (FluidType/capabilities-surface/networking-surface), and pattern-table
residue.

Ground rules while grinding:
- Add `'src/main/java'` to srcDirs LOCALLY; commit that line only when compile is fully green.
- Re-run the compile after each step; the count must drop as the plan predicts — if it doesn't,
  stop and investigate before continuing.
- New shims: same-surface fresh implementations (signatures from the NeoForge sources jar —
  never copy code) + TSV mapping + remap.py run committed `[scripted]`.
- Verify at the end: compile green → `gradlew test` green → dev server boots with mod
  construction uncommented → commit → PORTING.md + memory updated.

## 5. Build & verify commands

- Compile: `.\gradlew compileJava --console=plain -q`
- Dev server (headless check): `.\gradlew runServer`, watch `run/` logs for `Done (` and for
  `ERROR`/`Exception`. Kill it after the check — it doesn't exit on its own. Known-benign noise:
  FCAP night-config mixin WARN; "Registry 'neoforge:…' was empty" until 1f lands.
- Dev client (needs user, GUI): `.\gradlew runClient`.
- Transforms: `python fabric-port/remap.py`, `python fabric-port/at2aw.py`.

## 6. Safe-without-asking (user-granted standing permissions)

Ready-to-merge permission rules live in `fabric-port/claude-settings.recommended.json` — the user
merges them into `.claude/settings.local.json` (Claude must not edit its own permission settings).
In terms of intent:

- Reading/searching anything in this repo; reading the NeoForge/Fabric reference jars in the
  scratchpad; writing/editing files **inside this repo** and the session scratchpad.
- `git status/diff/log/show/add/commit` on `fabric/1.21.x`; `gradlew compileJava/runServer`;
  the two `fabric-port/*.py` scripts.
- NOT without asking: `git push`, changing branches, rewriting history, deleting files you did
  not create, editing anything under `1.21.x`, adding new remote dependencies.

## 7. Design-sensitive areas — follow the specs, don't improvise

In these areas a plausible-looking guess is usually subtly wrong and expensive to unwind. The
rule: **implement exactly what the written spec/tests say; if a case isn't covered, stop and ask
the user instead of choosing.**

| Area | Spec / guardrail |
|---|---|
| Transfer bridge (fluid/item/energy semantics, ×81, simulate) | `fabric-port/design/transfer-bridge.md` + `src/fabric_test` suite |
| Serialized formats (FluidStack codecs, ingredient JSON, packet bytes) | must match NeoForge byte-for-byte; golden tests in `FluidStackWireFormatTest` — never "fix" an expected value without diffing against NeoForge's serialization |
| Registry lifecycle order | `ShimRegistryEvents` javadoc; NeoForge's GameData order — don't reorder |
| Energy J↔FE conversion rate | OPEN item — reuse Mekanism's own EnergyUnit helpers from src/main once it compiles; do not hardcode a rate |
| Access wideners | generated — edit `fabric-port/extra.aw` + rerun at2aw.py, never the generated file |

Pre-commit checklist: `compileJava` green → `test` green (no weakened assertions) → boot check if
lifecycle/registration touched → `[port]`/`[scripted]` split correct → PORTING.md updated.

## 8. Gotchas that cost time before (don't rediscover these)

- FCAP ships `net.neoforged.fml.config.*` + `ModConfigSpec` under original names → config
  classes need NO remap. Only `ModConfigEvent` is shimmed.
- The NeoForge bus is a plain library; untyped `addListener(this::method)` resolution via
  typetools works under Knot — verified. Don't remap `net.neoforged.bus.api.*`.
- Shim registration lifecycle order matters: `NewRegistryEvent` → fill → datapack registries →
  data maps → `RegisterEvent` per registry (ATTRIBUTE, DATA_COMPONENT_TYPE, ARMOR_MATERIAL first,
  then root-registry insertion order). All inside `onInitialize` (Fabric freezes registries after).
- `Edit` tool requires `Read` first — `grep`/`cat` output doesn't count.
- remap.py rewrites files on disk — re-`Read` any file you touched before editing it again.
- Windows: `javap` isn't on PATH — find it under `%USERPROFILE%\.gradle\jdks` or Program Files.
- Don't let log-watching scripts kill the server on the string "Error" — FCAP logs a benign
  mixin WARN containing it; match `Done \(` / crash markers instead.
- `ItemStackIngredient.logMissingTags` and friends use the AW'd `Ingredient#values` field —
  if AW regeneration drops `fabric-port/extra.aw` entries, api stops compiling.
- Phase 1 shim semantic deviations are tabled in PORTING.md — check that table before debugging
  "missing" behavior (milk fluid, swim-speed attribute, chunk ticket validation are known gaps).
