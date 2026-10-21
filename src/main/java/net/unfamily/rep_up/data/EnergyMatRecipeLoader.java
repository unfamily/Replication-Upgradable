package net.unfamily.rep_up.data;

import com.buuz135.replication.ReplicationRegistry;
import com.buuz135.replication.api.IMatterType;
import com.buuz135.replication.api.MatterType;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.unfamily.iskalib.load.LoadFilesystemBootstrap;
import net.unfamily.iskalib.load.LoadJson;
import net.unfamily.rep_up.Config;
import net.unfamily.rep_up.RepUp;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Loads energy_mat conversion data (matter id -> RF per unit) via Iskandert Library load JSON.
 * Sources: jar {@code data/rep_up/load/} (any nested folders), datapack bootstrap roots, and live
 * {@link ResourceManager} on reload. Matching is by root {@code type} {@code rep_up:energy_mat}.
 * Fallback RF for unknown matter comes from {@link Config#ENERGY_MAT_FALLBACK_RF}.
 */
public class EnergyMatRecipeLoader {

    private static final Logger LOGGER = LogUtils.getLogger();
    public static final String TYPE = "rep_up:energy_mat";

    private static final Map<ResourceLocation, Integer> RF_MAP = new HashMap<>();

    /** Call from server init or after datapack reload when Replication registry is ready. */
    public static void load() {
        load(null);
    }

    /** Prefer ResourceManager when available (TagsUpdated / reload); otherwise jar + filesystem. */
    public static void load(ResourceManager resourceManager) {
        RF_MAP.clear();

        Map<ResourceLocation, JsonElement> merged = new LinkedHashMap<>();
        // Entire load/ tree (cascades into nested folders); type filter applied below.
        merged.putAll(LoadJson.collectFromModJarPath(RepUp.MODID, RepUp.MODID, LoadJson.LOAD_FOLDER));
        LoadFilesystemBootstrap.mergeIntoForType(merged, TYPE);
        if (resourceManager != null) {
            merged.putAll(LoadJson.collectMergedJsonForTypes(resourceManager, Set.of(TYPE)));
        }

        boolean anyParsed = false;
        for (Map.Entry<ResourceLocation, JsonElement> entry : LoadJson.orderedEntries(merged)) {
            if (applyRoot(entry.getValue())) {
                anyParsed = true;
            }
        }

        if (!anyParsed || RF_MAP.isEmpty()) {
            loadDefaultsInMemory();
            LOGGER.info("[rep_up] energy_mat: using built-in defaults ({} matter types, fallbackRf={})",
                    RF_MAP.size(), getFallbackRf());
        } else {
            LOGGER.info("[rep_up] energy_mat: loaded {} matter RF entries (fallbackRf={})",
                    RF_MAP.size(), getFallbackRf());
        }
    }

    private static boolean applyRoot(JsonElement root) {
        if (root == null || !root.isJsonObject()) {
            return false;
        }
        JsonObject obj = root.getAsJsonObject();
        if (!TYPE.equals(obj.has("type") ? obj.get("type").getAsString() : "")) {
            return false;
        }
        int fallback = getFallbackRf();
        if (obj.has("entries") && obj.get("entries").isJsonArray()) {
            for (JsonElement entry : obj.get("entries").getAsJsonArray()) {
                if (!entry.isJsonObject()) continue;
                JsonObject o = entry.getAsJsonObject();
                String id = o.has("id") ? o.get("id").getAsString() : null;
                int rf = o.has("rf") ? o.get("rf").getAsInt() : fallback;
                if (id != null && !id.isEmpty()) {
                    try {
                        RF_MAP.put(ResourceLocation.parse(id), Math.max(1, rf));
                    } catch (Exception ignored) {
                    }
                }
            }
        }
        return true;
    }

    private static void loadDefaultsInMemory() {
        RF_MAP.clear();
        if (ReplicationRegistry.MATTER_TYPES_REGISTRY == null) {
            return;
        }
        int fallback = getFallbackRf();
        Map<String, Integer> defaults = Map.of(
                "earth", 12_000,
                "nether", 25_000,
                "organic", 15_000,
                "ender", 28_000,
                "metallic", 20_000,
                "precious", 35_000,
                "living", 22_000,
                "quantum", 40_000
        );
        for (IMatterType type : ReplicationRegistry.MATTER_TYPES_REGISTRY) {
            if (type == MatterType.EMPTY) continue;
            ResourceLocation id = ReplicationRegistry.MATTER_TYPES_REGISTRY.getKey(type);
            if (id != null) {
                String name = type.getName().toLowerCase();
                RF_MAP.put(id, defaults.getOrDefault(name, fallback));
            }
        }
    }

    /** RF cost per 1 unit of matter. Returns config fallback for unknown/custom matter. */
    public static int getRfForMatter(ResourceLocation matterId) {
        return RF_MAP.getOrDefault(matterId, getFallbackRf());
    }

    public static int getFallbackRf() {
        return Math.max(1, Config.ENERGY_MAT_FALLBACK_RF.get());
    }

    public static boolean isLoaded() {
        return !RF_MAP.isEmpty() || ReplicationRegistry.MATTER_TYPES_REGISTRY != null;
    }
}
