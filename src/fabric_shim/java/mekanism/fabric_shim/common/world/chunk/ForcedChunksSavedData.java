package mekanism.fabric_shim.common.world.chunk;

import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.saveddata.SavedData;

/**
 * Per-level owner-tracked forced chunks: the persistence NeoForge's ForcedChunkManager provides for
 * {@link TicketController}s, reimplemented over vanilla region tickets (own on-disk format, not
 * NeoForge's — the port has never shipped, so there is no old data to migrate). This store is the
 * single source of truth: the region tickets themselves are non-persistent custom
 * {@link TicketType}s that {@link ShimChunkManager} re-applies on level load, after the
 * controllers' {@link LoadingValidationCallback}s have pruned stale owners.
 *
 * <p>Ticket levels mirror the vanilla/NeoForge split: ticking tickets sit at entity-ticking level
 * (distance 2 → level 31, what vanilla forced chunks use), non-ticking at block-ticking level
 * (distance 3 → level 32). Since equal tickets dedup inside the distance manager, a chunk's region
 * ticket is only released once no owner of any controller still holds it in that ticking class.
 */
public class ForcedChunksSavedData extends SavedData {

    private static final String NAME = "mekanism_shim_forced_chunks";
    //Non-timeout ticket types, like vanilla's "forced"
    private static final TicketType<ChunkPos> NON_TICKING = TicketType.create("mekanism_shim_forced", java.util.Comparator.comparingLong(ChunkPos::toLong));
    private static final TicketType<ChunkPos> TICKING = TicketType.create("mekanism_shim_forced_ticking", java.util.Comparator.comparingLong(ChunkPos::toLong));
    private static final int TICKING_DISTANCE = 2;
    private static final int NON_TICKING_DISTANCE = 3;

    static final class ControllerData {

        final Map<BlockPos, TicketSet> blockTickets = new HashMap<>();
        final Map<UUID, TicketSet> entityTickets = new HashMap<>();

        boolean isEmpty() {
            return blockTickets.isEmpty() && entityTickets.isEmpty();
        }
    }

    private final Map<ResourceLocation, ControllerData> controllers = new LinkedHashMap<>();

    public static ForcedChunksSavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(new SavedData.Factory<>(ForcedChunksSavedData::new, ForcedChunksSavedData::load, null), NAME);
    }

    boolean isEmpty() {
        return controllers.isEmpty();
    }

    Set<ResourceLocation> controllerIds() {
        return Set.copyOf(controllers.keySet());
    }

    boolean forceBlock(ServerLevel level, ResourceLocation controller, BlockPos owner, long chunk, boolean add, boolean ticking) {
        return force(level, controller, data -> data.blockTickets, owner.immutable(), chunk, add, ticking);
    }

    boolean forceEntity(ServerLevel level, ResourceLocation controller, UUID owner, long chunk, boolean add, boolean ticking) {
        return force(level, controller, data -> data.entityTickets, owner, chunk, add, ticking);
    }

    private <O> boolean force(ServerLevel level, ResourceLocation controller, Function<ControllerData, Map<O, TicketSet>> ownerMap, O owner, long chunk,
          boolean add, boolean ticking) {
        if (add) {
            TicketSet set = ownerMap.apply(controllers.computeIfAbsent(controller, id -> new ControllerData()))
                  .computeIfAbsent(owner, o -> new TicketSet(new LongOpenHashSet(), new LongOpenHashSet()));
            boolean alreadyHeld = anyOwnerHolds(chunk, ticking);
            if (!(ticking ? set.ticking() : set.nonTicking()).add(chunk)) {
                return false;
            }
            if (!alreadyHeld) {
                addRegionTicket(level, chunk, ticking);
            }
        } else {
            ControllerData data = controllers.get(controller);
            TicketSet set = data == null ? null : ownerMap.apply(data).get(owner);
            if (set == null || !(ticking ? set.ticking() : set.nonTicking()).remove(chunk)) {
                return false;
            }
            pruneOwner(data, ownerMap, owner, controller);
            releaseIfUnheld(level, chunk, ticking);
        }
        setDirty();
        return true;
    }

    /** Releases every ticket the owner holds for this controller (validation-callback cleanup). */
    <O> void removeAllTickets(ServerLevel level, ResourceLocation controller, Function<ControllerData, Map<O, TicketSet>> ownerMap, O owner) {
        ControllerData data = controllers.get(controller);
        TicketSet set = data == null ? null : ownerMap.apply(data).remove(owner);
        if (set == null) {
            return;
        }
        pruneController(controller, data);
        for (LongIterator it = set.nonTicking().iterator(); it.hasNext(); ) {
            releaseIfUnheld(level, it.nextLong(), false);
        }
        for (LongIterator it = set.ticking().iterator(); it.hasNext(); ) {
            releaseIfUnheld(level, it.nextLong(), true);
        }
        setDirty();
    }

    private <O> void pruneOwner(ControllerData data, Function<ControllerData, Map<O, TicketSet>> ownerMap, O owner, ResourceLocation controller) {
        Map<O, TicketSet> owners = ownerMap.apply(data);
        TicketSet set = owners.get(owner);
        if (set != null && set.nonTicking().isEmpty() && set.ticking().isEmpty()) {
            owners.remove(owner);
        }
        pruneController(controller, data);
    }

    private void pruneController(ResourceLocation controller, ControllerData data) {
        if (data.isEmpty()) {
            controllers.remove(controller);
        }
    }

    private boolean anyOwnerHolds(long chunk, boolean ticking) {
        for (ControllerData data : controllers.values()) {
            for (TicketSet set : data.blockTickets.values()) {
                if ((ticking ? set.ticking() : set.nonTicking()).contains(chunk)) {
                    return true;
                }
            }
            for (TicketSet set : data.entityTickets.values()) {
                if ((ticking ? set.ticking() : set.nonTicking()).contains(chunk)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void releaseIfUnheld(ServerLevel level, long chunk, boolean ticking) {
        if (!anyOwnerHolds(chunk, ticking)) {
            ChunkPos pos = new ChunkPos(chunk);
            level.getChunkSource().removeRegionTicket(ticking ? TICKING : NON_TICKING, pos, ticking ? TICKING_DISTANCE : NON_TICKING_DISTANCE, pos);
        }
    }

    private static void addRegionTicket(ServerLevel level, long chunk, boolean ticking) {
        ChunkPos pos = new ChunkPos(chunk);
        level.getChunkSource().addRegionTicket(ticking ? TICKING : NON_TICKING, pos, ticking ? TICKING_DISTANCE : NON_TICKING_DISTANCE, pos);
    }

    /** Applies region tickets for everything currently stored (level load, after validation). */
    void applyAll(ServerLevel level) {
        for (ControllerData data : controllers.values()) {
            for (TicketSet set : data.blockTickets.values()) {
                applySet(level, set);
            }
            for (TicketSet set : data.entityTickets.values()) {
                applySet(level, set);
            }
        }
    }

    private static void applySet(ServerLevel level, TicketSet set) {
        for (LongIterator it = set.nonTicking().iterator(); it.hasNext(); ) {
            addRegionTicket(level, it.nextLong(), false);
        }
        for (LongIterator it = set.ticking().iterator(); it.hasNext(); ) {
            addRegionTicket(level, it.nextLong(), true);
        }
    }

    /** Deep-copied helper so callback iteration is immune to its own removals (NeoForge hands out copies too). */
    TicketHelper createHelper(ServerLevel level, ResourceLocation controller) {
        ControllerData data = controllers.getOrDefault(controller, new ControllerData());
        Map<BlockPos, TicketSet> blocks = new HashMap<>();
        data.blockTickets.forEach((pos, set) -> blocks.put(pos, copy(set)));
        Map<UUID, TicketSet> entities = new HashMap<>();
        data.entityTickets.forEach((uuid, set) -> entities.put(uuid, copy(set)));
        return new TicketHelper(this, level, controller, Map.copyOf(blocks), Map.copyOf(entities));
    }

    private static TicketSet copy(TicketSet set) {
        return new TicketSet(new LongOpenHashSet(set.nonTicking()), new LongOpenHashSet(set.ticking()));
    }

    //--- NBT ---

    public static ForcedChunksSavedData load(CompoundTag tag, HolderLookup.Provider provider) {
        ForcedChunksSavedData data = new ForcedChunksSavedData();
        for (Tag controllerTag : tag.getList("controllers", Tag.TAG_COMPOUND)) {
            CompoundTag controller = (CompoundTag) controllerTag;
            ResourceLocation id = ResourceLocation.tryParse(controller.getString("id"));
            if (id == null) {
                continue;
            }
            ControllerData controllerData = new ControllerData();
            for (Tag blockTag : controller.getList("blocks", Tag.TAG_COMPOUND)) {
                CompoundTag block = (CompoundTag) blockTag;
                controllerData.blockTickets.put(BlockPos.of(block.getLong("pos")), loadTicketSet(block));
            }
            for (Tag entityTag : controller.getList("entities", Tag.TAG_COMPOUND)) {
                CompoundTag entity = (CompoundTag) entityTag;
                controllerData.entityTickets.put(entity.getUUID("owner"), loadTicketSet(entity));
            }
            if (!controllerData.isEmpty()) {
                data.controllers.put(id, controllerData);
            }
        }
        return data;
    }

    private static TicketSet loadTicketSet(CompoundTag tag) {
        return new TicketSet(new LongOpenHashSet(tag.getLongArray("non_ticking")), new LongOpenHashSet(tag.getLongArray("ticking")));
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        ListTag controllerList = new ListTag();
        controllers.forEach((id, data) -> {
            CompoundTag controller = new CompoundTag();
            controller.putString("id", id.toString());
            ListTag blocks = new ListTag();
            data.blockTickets.forEach((pos, set) -> {
                CompoundTag block = saveTicketSet(set);
                block.putLong("pos", pos.asLong());
                blocks.add(block);
            });
            controller.put("blocks", blocks);
            ListTag entities = new ListTag();
            data.entityTickets.forEach((uuid, set) -> {
                CompoundTag entity = saveTicketSet(set);
                entity.putUUID("owner", uuid);
                entities.add(entity);
            });
            controller.put("entities", entities);
            controllerList.add(controller);
        });
        tag.put("controllers", controllerList);
        return tag;
    }

    private static CompoundTag saveTicketSet(TicketSet set) {
        CompoundTag tag = new CompoundTag();
        tag.putLongArray("non_ticking", set.nonTicking().toLongArray());
        tag.putLongArray("ticking", set.ticking().toLongArray());
        return tag;
    }
}
