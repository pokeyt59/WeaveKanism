package mekanism.fabric_shim.internal;

import mekanism.fabric_shim.common.NeoForge;
import mekanism.fabric_shim.event.BuildCreativeModeTabContentsEvent;
import mekanism.fabric_shim.event.OnDatapackSyncEvent;
import mekanism.fabric_shim.event.entity.EntityJoinLevelEvent;
import mekanism.fabric_shim.event.entity.living.LivingDeathEvent;
import mekanism.fabric_shim.event.level.BlockEvent;
import mekanism.fabric_shim.event.level.ChunkEvent;
import mekanism.fabric_shim.event.tick.EntityTickEvent;
import mekanism.fabric_shim.event.tick.LevelTickEvent;
import mekanism.fabric_shim.event.tick.PlayerTickEvent;
import mekanism.fabric_shim.event.tick.ServerTickEvent;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

/**
 * Phase 3 gameplay-event glue: posts the shim game-bus events from their Fabric API equivalents.
 * Sibling to {@link ShimGameEvents} (server lifecycle/level/tags/commands/reload, wired in Phase
 * 1); this class covers the tick/entity/living/block/chunk families that were compile-only until
 * now. Events with no Fabric equivalent (fall/jump/incoming-damage/invulnerability, chunk NBT
 * data) are posted from the port's behavior mixins instead.
 *
 * <p>Documented timing deviations (PORTING.md): player and entity tick events fire at the world
 * tick boundary rather than inside each entity's own tick; {@code EntityTickEvent.Pre} is not
 * posted (cancelling entity ticks is unsupported); {@code EntityJoinLevelEvent} fires after the
 * entity is added (Fabric has no pre-add hook) — Mekanism's handler discards the entity itself,
 * so cancellation still takes effect the same tick.
 */
public final class ShimGameplayEvents {

    private ShimGameplayEvents() {
    }

    public static void init() {
        //--- Tick family ---
        ServerTickEvents.START_SERVER_TICK.register(server ->
              NeoForge.EVENT_BUS.post(new ServerTickEvent.Pre(server::haveTime, server)));
        ServerTickEvents.END_SERVER_TICK.register(server ->
              NeoForge.EVENT_BUS.post(new ServerTickEvent.Post(server::haveTime, server)));
        ServerTickEvents.START_WORLD_TICK.register(level -> {
            NeoForge.EVENT_BUS.post(new LevelTickEvent.Pre(level.getServer()::haveTime, level));
            for (ServerPlayer player : level.players()) {
                NeoForge.EVENT_BUS.post(new PlayerTickEvent.Pre(player));
            }
        });
        ServerTickEvents.END_WORLD_TICK.register(level -> {
            NeoForge.EVENT_BUS.post(new LevelTickEvent.Post(level.getServer()::haveTime, level));
            for (ServerPlayer player : level.players()) {
                NeoForge.EVENT_BUS.post(new PlayerTickEvent.Post(player));
            }
            for (Entity entity : level.getAllEntities()) {
                if (!(entity instanceof Player)) {
                    NeoForge.EVENT_BUS.post(new EntityTickEvent.Post(entity));
                }
            }
        });

        //--- Entity / living family ---
        ServerEntityEvents.ENTITY_LOAD.register((entity, level) ->
              NeoForge.EVENT_BUS.post(new EntityJoinLevelEvent(entity, level)));
        ServerLivingEntityEvents.ALLOW_DEATH.register((entity, source, amount) -> {
            //Fires before drops so listeners (flamethrower ignite) can affect the loot, like NeoForge's
            LivingDeathEvent event = NeoForge.EVENT_BUS.post(new LivingDeathEvent(entity, source));
            return !event.isCanceled();
        });

        //--- Block / chunk ---
        PlayerBlockBreakEvents.BEFORE.register((level, player, pos, state, blockEntity) -> {
            BlockEvent.BreakEvent event = NeoForge.EVENT_BUS.post(new BlockEvent.BreakEvent(level, pos, state, player));
            return !event.isCanceled();
        });
        ServerChunkEvents.CHUNK_LOAD.register((level, chunk) ->
              NeoForge.EVENT_BUS.post(new ChunkEvent.Load(chunk, level, false)));
        ServerChunkEvents.CHUNK_UNLOAD.register((level, chunk) ->
              NeoForge.EVENT_BUS.post(new ChunkEvent.Unload(chunk, level)));

        //--- Sync / tab population ---
        //NeoForge fires one event with player (join) or null (reload, all players); per-player
        //firing covers both — getRelevantPlayers() resolves to the same player set
        ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.register((player, joined) ->
              NeoForge.EVENT_BUS.post(new OnDatapackSyncEvent(player.server.getPlayerList(), player)));
        //Fires per tab during (client-side) tab population, like NeoForge's; the Fabric entries
        //collector is itself a CreativeModeTab.Output, so it is the event's entry sink
        ItemGroupEvents.MODIFY_ENTRIES_ALL.register((group, entries) ->
              BuiltInRegistries.CREATIVE_MODE_TAB.getResourceKey(group).ifPresent(key ->
                    ShimBuses.MOD_BUS.post(new BuildCreativeModeTabContentsEvent(key, entries.getContext(), entries))));
    }
}
