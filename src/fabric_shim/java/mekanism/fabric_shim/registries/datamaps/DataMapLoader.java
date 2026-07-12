package mekanism.fabric_shim.registries.datamaps;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import java.io.Reader;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import mekanism.fabric_shim.common.NeoForge;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Loads NeoForge's {@code data/<ns>/data_maps/<registry>/<id>.json} format into {@link DataMaps}
 * (fresh implementation of DataMapLoader's observable behavior), so upstream datagen output works
 * unchanged. Two stages, because on first server start the reload listeners run before any
 * {@code RegistryAccess} exists and before tags bind:
 * <ol>
 *   <li><b>Reload listener</b> (registered from {@code ShimGameEvents}): locate + parse the JSON
 *       resource stacks for every registered data map type; stash them.</li>
 *   <li><b>{@link #applyPending}</b> (called from the {@code TAGS_LOADED} bridge, before
 *       {@code TagsUpdatedEvent} posts — NeoForge applies data maps before that event too):
 *       decode values with registry-aware ops, expand tag keys against the now-bound tags,
 *       fold the pack stack, commit to {@link DataMaps} and post {@link DataMapsUpdatedEvent}
 *       per registry (Mekanism's chemicals cache their attributes in that handler).</li>
 * </ol>
 *
 * <p>Format semantics mirrored from NeoForge: file-level {@code replace} clears earlier packs'
 * entries; {@code values} entries apply in JSON order, keys are ids or {@code #tags} (a missing id
 * logs an error and continues, an unbound tag expands to nothing), values are either the raw codec
 * JSON or wrapped as {@code {"value": ..., "replace": ...}}; {@code remove} lists ids/tags;
 * within/across files the last write per entry wins (NeoForge's default merger — advanced
 * mergers/removers are not supported, Mekanism registers none); a file that fails to decode is
 * skipped whole, other packs' files still apply.
 *
 * <p>Deviations (PORTING.md): {@code neoforge:conditions} in entries are not evaluated (a
 * conditional entry fails that file's decode; Mekanism ships none), files for unregistered types
 * only warn when their registry has at least one registered type, and every typed registry is
 * rebuilt + gets its updated event even when no files exist for it (NeoForge skips fileless
 * registries, which can leave stale values across a /reload that removed a pack).
 */
public final class DataMapLoader implements SimpleSynchronousResourceReloadListener {

    private static final Logger LOGGER = LoggerFactory.getLogger("MekanismShim");
    public static final String PATH = "data_maps";
    private static final DataMapLoader INSTANCE = new DataMapLoader();

    //Written by the reload listener, consumed on the game thread at TAGS_LOADED (same thread for
    // server datapack loads; volatile covers the belt-and-suspenders case)
    @Nullable
    private volatile Map<DataMapType<?, ?>, List<PendingFile>> pending;

    private DataMapLoader() {
    }

    public static void init() {
        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(INSTANCE);
    }

    /**
     * Commits the last reload's parsed files, or no-ops when there is nothing pending (e.g. the
     * client-side TAGS_LOADED after a dedicated server sync — client data maps arrive via the
     * Phase 4 sync packet instead).
     */
    public static void applyPending(RegistryAccess registryAccess) {
        INSTANCE.apply(registryAccess);
    }

    @Override
    public ResourceLocation getFabricId() {
        return ResourceLocation.fromNamespaceAndPath("mekanism", "data_maps");
    }

    /**
     * NeoForge's folder convention: {@code minecraft:damage_type} → {@code damage_type},
     * {@code mekanism:chemical} → {@code mekanism/chemical}.
     */
    public static String getFolderLocation(ResourceLocation registryId) {
        return (registryId.getNamespace().equals(ResourceLocation.DEFAULT_NAMESPACE) ? "" : registryId.getNamespace() + "/") + registryId.getPath();
    }

    @Override
    public void onResourceManagerReload(ResourceManager manager) {
        Map<DataMapType<?, ?>, List<PendingFile>> collected = new IdentityHashMap<>();
        for (ResourceKey<? extends Registry<?>> registryKey : DataMaps.typedRegistries()) {
            Map<ResourceLocation, DataMapType<?, ?>> types = DataMaps.getTypes(registryKey);
            FileToIdConverter lister = FileToIdConverter.json(PATH + "/" + getFolderLocation(registryKey.location()));
            for (Map.Entry<ResourceLocation, List<Resource>> entry : lister.listMatchingResourceStacks(manager).entrySet()) {
                ResourceLocation dataMapId = lister.fileToId(entry.getKey());
                DataMapType<?, ?> type = types.get(dataMapId);
                if (type == null) {
                    LOGGER.warn("Found data map file for non-existent data map type '{}' on registry '{}'.", dataMapId, registryKey.location());
                    continue;
                }
                List<PendingFile> files = new ArrayList<>(entry.getValue().size());
                for (Resource resource : entry.getValue()) {
                    try (Reader reader = resource.openAsReader()) {
                        files.add(new PendingFile(JsonParser.parseReader(reader), resource.sourcePackId()));
                    } catch (Exception e) {
                        LOGGER.error("Could not read data map file {} of type {} from pack {}", entry.getKey(), dataMapId, resource.sourcePackId(), e);
                    }
                }
                collected.put(type, files);
            }
        }
        this.pending = collected;
    }

    private void apply(RegistryAccess registryAccess) {
        Map<DataMapType<?, ?>, List<PendingFile>> files = this.pending;
        if (files == null) {
            return;
        }
        this.pending = null;
        RegistryOps<JsonElement> ops = RegistryOps.create(JsonOps.INSTANCE, registryAccess);
        for (ResourceKey<? extends Registry<?>> registryKey : DataMaps.typedRegistries()) {
            @SuppressWarnings({"unchecked", "rawtypes"})
            Registry<?> registry = (Registry<?>) registryAccess.registry((ResourceKey) registryKey).orElse(null);
            if (registry == null) {
                LOGGER.error("Registry {} with registered data map types is missing from the registry access; skipping its data maps", registryKey.location());
                continue;
            }
            for (DataMapType<?, ?> type : DataMaps.getTypes(registryKey).values()) {
                DataMaps.setValues(type, build(registry, type, files.getOrDefault(type, List.of()), ops));
            }
            NeoForge.EVENT_BUS.post(new DataMapsUpdatedEvent(registry, DataMapsUpdatedEvent.UpdateCause.SERVER_RELOAD));
        }
    }

    private <R> Map<ResourceKey<?>, Object> build(Registry<R> registry, DataMapType<?, ?> type, List<PendingFile> files, RegistryOps<JsonElement> ops) {
        Map<ResourceKey<?>, Object> result = new HashMap<>();
        for (PendingFile file : files) {
            FileContents contents;
            try {
                //Parse the whole file before applying any of it: a bad entry drops the file
                // without partial application, like NeoForge's whole-file decode
                contents = parseFile(type, file.json().getAsJsonObject(), ops);
            } catch (Exception e) {
                LOGGER.error("Could not read data map of type {} for registry {} from pack {}", type.id(), registry.key().location(), file.source(), e);
                continue;
            }
            if (contents.replace()) {
                result.clear();
            }
            for (ParsedEntry entry : contents.entries()) {
                //Default merger semantics: the newest value wins outright
                resolve(registry, entry.key(), true, key -> result.put(key, entry.value()));
            }
            for (ParsedKey removal : contents.removals()) {
                resolve(registry, removal, false, result::remove);
            }
        }
        return result;
    }

    private FileContents parseFile(DataMapType<?, ?> type, JsonObject json, RegistryOps<JsonElement> ops) {
        boolean replace = GsonHelper.getAsBoolean(json, "replace", false);
        List<ParsedEntry> entries = new ArrayList<>();
        for (Map.Entry<String, JsonElement> e : GsonHelper.getAsJsonObject(json, "values").entrySet()) {
            ParsedKey key = parseKey(e.getKey());
            JsonElement valueJson = e.getValue();
            //NeoForge's entry codec is either({"value":..., "replace":...}, raw value), wrapped
            // form first — mirror that order, falling back to the raw form
            if (valueJson.isJsonObject() && valueJson.getAsJsonObject().has("value")) {
                JsonObject wrapped = valueJson.getAsJsonObject();
                DataResult<?> decoded = type.codec().parse(ops, wrapped.get("value"));
                if (decoded.result().isPresent()) {
                    entries.add(new ParsedEntry(key, decoded.result().get()));
                    continue;
                }
            }
            Object value = type.codec().parse(ops, valueJson)
                  .getOrThrow(msg -> new JsonParseException("Failed to decode data map value for key '" + e.getKey() + "': " + msg));
            entries.add(new ParsedEntry(key, value));
        }
        List<ParsedKey> removals = new ArrayList<>();
        if (json.has("remove")) {
            for (JsonElement el : GsonHelper.getAsJsonArray(json, "remove")) {
                removals.add(parseKey(GsonHelper.convertToString(el, "remove entry")));
            }
        }
        return new FileContents(replace, entries, removals);
    }

    private static ParsedKey parseKey(String key) {
        return key.startsWith("#") ? new ParsedKey(true, ResourceLocation.parse(key.substring(1)))
                                   : new ParsedKey(false, ResourceLocation.parse(key));
    }

    private <R> void resolve(Registry<R> registry, ParsedKey key, boolean required, Consumer<ResourceKey<R>> consumer) {
        if (key.tag()) {
            for (Holder<R> holder : registry.getTagOrEmpty(TagKey.create(registry.key(), key.id()))) {
                holder.unwrapKey().ifPresent(consumer);
            }
        } else {
            var holder = registry.getHolder(ResourceKey.create(registry.key(), key.id()));
            if (holder.isPresent()) {
                consumer.accept(holder.get().key());
            } else if (required) {
                LOGGER.error("Object with ID {} specified in data map for registry {} doesn't exist", key.id(), registry.key().location());
            }
        }
    }

    private record PendingFile(JsonElement json, String source) {}

    private record ParsedKey(boolean tag, ResourceLocation id) {}

    private record ParsedEntry(ParsedKey key, Object value) {}

    private record FileContents(boolean replace, List<ParsedEntry> entries, List<ParsedKey> removals) {}
}
