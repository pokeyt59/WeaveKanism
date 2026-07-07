# Transfer bridge design — READ BEFORE TOUCHING PHASE 2 CODE

This document locks the design for bridging Mekanism's simulate-based handlers to Fabric's
transaction-based Transfer API. The **fluid core is already implemented and tested** — do not
redesign it; extend it. If a case comes up that this document does not cover, **stop and ask the
user** rather than improvising: subtle mistakes here corrupt world content silently.

## What already exists (implemented + guardrail-tested)

| Class (`mekanism.fabric_shim.transfer`) | Direction | Notes |
|---|---|---|
| `TransferUnits` | — | mB↔droplet math; the ONLY place conversion happens |
| `ExtendedFluidTankStorage` | Mekanism tank → Fabric `SingleSlotStorage<FluidVariant>` | SnapshotParticipant over `IExtendedFluidTank` |
| `StorageFluidHandler` | Fabric `Storage<FluidVariant>` → Mekanism `IFluidHandler` | probe-then-aligned-op pattern |

Tests: `src/fabric_test/java/mekanism/fabric_test/` (`gradlew test`, 19 tests). **Any Phase 2 work
must keep this suite green and should add cases, never weaken them.** `FluidStackWireFormatTest`
additionally locks FluidStack's JSON/stream formats to NeoForge's — never update its expected
values without diffing against NeoForge's own serialization.

## The three invariants (memorize these)

1. **1 mB = exactly 81 droplets.** Mekanism never stores droplets. Every droplet amount that
   crosses the boundary is a multiple of 81 (`TransferUnits.floorToMbAligned`); therefore
   conversion is lossless and fluid is never created or destroyed. Flooring always rounds
   *against* the requester.
2. **Simulate = aborted transaction, never a committed one.**
   - Mekanism consuming a Fabric storage (`StorageFluidHandler`): `SIMULATE` → open
     transaction, operate, *abort*; `EXECUTE` → same, *commit*.
   - Fabric consuming a Mekanism tank (`ExtendedFluidTankStorage`): always `Action.EXECUTE`
     against the tank, `updateSnapshots(tx)` **before any mutation**; rollback happens via
     snapshot restore when the caller aborts.
3. **Only adapt what you can roll back.** The Mekanism→Fabric direction wraps
   `IExtendedFluidTank` / `IEnergyContainer` / Mekanism inventory slots — objects with setters,
   so `SnapshotParticipant` restores state *exactly*. **NEVER write an adapter that exposes a
   generic `IFluidHandler`/`IItemHandler` as a Fabric `Storage`** — generic handlers cannot be
   rolled back, and every "apply on commit" or "inverse operation on abort" scheme is wrong for
   some handler. If something only exists as a generic handler, wire it at a different level or
   ask.

## Patterns (copy these, don't invent)

**Fabric-facing storage over a Mekanism container** — see `ExtendedFluidTankStorage`:
snapshot = full copy of container contents; `readSnapshot` uses the *unchecked* setter
(`setStackUnchecked`) so restore is never re-validated; `updateSnapshots(tx)` before the mutation;
convert request droplets→mB with floor first, return `inserted_mb * 81`.

**Mekanism-facing handler over an external Fabric storage** — see `StorageFluidHandler`:
```
try (Transaction tx = Transaction.openOuter()) {
    probe in tx.openNested() (aborted) → what would the storage accept/yield?
    aligned = floorToMbAligned(probeResult); if 0 → return nothing
    real op with `aligned`; if result != aligned → abort, return nothing (storage is inconsistent)
    action.execute() ? tx.commit() : /* fall out = abort */;
}
```
The probe pass exists because arbitrary storages accept unaligned amounts (e.g. a 100-droplet
tank); committing an unaligned move would create/destroy fluid (tested by
`handlerAlignmentOnQuirkyCapacity` / `handlerDrainQuirkyAmount`).

## Remaining Phase 2 pieces and their decided shape

- **Items** (`IInventorySlot` ↔ `Storage<ItemVariant>`): counts map 1:1 — no unit conversion, so
  only invariants 2 and 3 apply. Wrap Mekanism's `IInventorySlot` (it has
  `setStack`/`setStackUnchecked`) with a SnapshotParticipant<ItemStack> exactly like the fluid
  adapter. For consuming external inventories, wrap `Storage<ItemVariant>` as the shim
  `IItemHandler` with the probe pattern (no alignment needed; probe still needed because
  `insert` may partially accept and `IItemHandler#insertItem` must return the exact remainder).
- **Energy** (`IEnergyContainer` ↔ team-reborn `EnergyStorage`): structurally identical to fluid
  (SnapshotParticipant<Long> snapshotting `getEnergy()`, restore via `setEnergy`). **OPEN ITEM —
  do not guess:** the J↔FE conversion rate lives in Mekanism's config
  (`MekanismConfig.general` energy conversion, EnergyUnit code in src/main); wire the actual
  rate only once main compiles, and reuse Mekanism's own conversion helpers rather than
  reimplementing the math. Invariant: floor in both directions, round-trip must never create
  energy. Add tests mirroring the fluid ones before wiring it to machines.
- **Capability registration** (the 11 `RegisterCapabilitiesEvent` sites): the shim capability
  tokens already wrap Fabric lookups; Phase 2 registers providers
  (`BlockApiLookup#registerForBlockEntity` etc.) that hand out these adapters. Sidedness comes
  through the lookup context (`Direction`), matching NeoForge's `getCapability(pos, side)` shape.
- **BlockCapabilityCache**: Fabric's `BlockApiCache` is pull-based; re-query lazily + evict on
  neighbor change. Benchmark large transmitter networks before optimizing further.

## Forbidden patterns (each one looks plausible and is wrong)

- Committing a transaction during a SIMULATE path.
- Rolling back by performing the inverse operation (extract what you inserted) — machines consume.
- Accepting/reporting droplet amounts not divisible by 81, "to be helpful".
- Converting units anywhere except through `TransferUnits`.
- Snapshotting *after* the first mutation in a transaction.
- Exposing a generic `IFluidHandler`/`IItemHandler` as a Fabric `Storage` (see invariant 3).
- Caching a `Transaction` or using one from another thread.

## Acceptance checklist for any bridge change

1. `gradlew test` green, including your new cases (positive + quirky-amount + abort cases).
2. No fluid/energy/item creation or destruction in any test (assert both sides' totals).
3. Simulate paths asserted to leave both sides untouched.
4. `gradlew compileJava` green and dev server boots.
