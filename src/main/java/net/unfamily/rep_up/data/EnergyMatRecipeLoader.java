package net.unfamily.rep_up.data;

import com.buuz135.replication.Replication;
import com.buuz135.replication.ReplicationRegistry;
import com.buuz135.replication.api.IMatterType;
import com.buuz135.replication.api.MatterType;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.unfamily.rep_up.Config;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * Loads energy_mat conversion data (matter id -> RF per unit) from external JSON.
 * Path: &lt;config externalScriptsPath&gt;/energy_mat.json (default: kubejs/external_scripts/rep_up/energy_mat.json).
 * If file does not exist, creates directory and writes default file (does not overwrite existing).
 */
public class EnergyMatRecipeLoader {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final String TYPE = "rep_up:energy_mat";
    private static final int DEFAULT_FALLBACK_RF = 10_000;

    private static final Map<ResourceLocation, Integer> RF_MAP = new HashMap<>();
    private static int fallbackRf = DEFAULT_FALLBACK_RF;

    /** Call from server init or common setup after Replication registry is ready. */
    public static void load() {
        RF_MAP.clear();
        String basePath = Config.EXTERNAL_SCRIPTS_PATH.get();
        if (basePath == null || basePath.trim().isEmpty()) {
            basePath = "kubejs/external_scripts/rep_up";
        }
        Path dir = Paths.get(basePath);
        Path file = dir.resolve("energy_mat.json");

        if (!Files.exists(file)) {
            try {
                Files.createDirectories(dir);
                loadDefaultsInMemory();
                writeDefaultFile(file);
                LOGGER.info("[rep_up] Created default energy_mat.json at {}", file);
            } catch (IOException e) {
                LOGGER.warn("[rep_up] Could not create default energy_mat.json: {}", e.getMessage());
                loadDefaultsInMemory();
                return;
            }
        }

        try (Reader reader = Files.newBufferedReader(file)) {
            JsonElement root = GSON.fromJson(reader, JsonElement.class);
            if (root == null || !root.isJsonObject()) {
                LOGGER.warn("[rep_up] energy_mat.json invalid, using defaults");
                loadDefaultsInMemory();
                return;
            }
            JsonObject obj = root.getAsJsonObject();
            if (!TYPE.equals(obj.has("type") ? obj.get("type").getAsString() : "")) {
                LOGGER.warn("[rep_up] energy_mat.json wrong type, using defaults");
                loadDefaultsInMemory();
                return;
            }
            fallbackRf = obj.has("fallback_rf") ? obj.get("fallback_rf").getAsInt() : DEFAULT_FALLBACK_RF;
            fallbackRf = Math.max(1, fallbackRf);
            if (obj.has("entries") && obj.get("entries").isJsonArray()) {
                for (JsonElement entry : obj.get("entries").getAsJsonArray()) {
                    if (!entry.isJsonObject()) continue;
                    JsonObject o = entry.getAsJsonObject();
                    String id = o.has("id") ? o.get("id").getAsString() : null;
                    int rf = o.has("rf") ? o.get("rf").getAsInt() : fallbackRf;
                    if (id != null && !id.isEmpty()) {
                        try {
                            ResourceLocation loc = ResourceLocation.parse(id);
                            RF_MAP.put(loc, Math.max(1, rf));
                        } catch (Exception ignored) {
                        }
                    }
                }
            }
            if (RF_MAP.isEmpty()) {
                loadDefaultsInMemory();
            }
        } catch (IOException e) {
            LOGGER.warn("[rep_up] Could not read energy_mat.json: {}, using defaults", e.getMessage());
            loadDefaultsInMemory();
        }
    }

    private static void loadDefaultsInMemory() {
        RF_MAP.clear();
        fallbackRf = DEFAULT_FALLBACK_RF;
        if (ReplicationRegistry.MATTER_TYPES_REGISTRY == null) {
            return;
        }
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
                RF_MAP.put(id, defaults.getOrDefault(name, DEFAULT_FALLBACK_RF));
            }
        }
    }

    private static void writeDefaultFile(Path file) throws IOException {
        JsonObject root = new JsonObject();
        root.addProperty("type", TYPE);
        root.addProperty("fallback_rf", fallbackRf);
        JsonArray entries = new JsonArray();
        for (Map.Entry<ResourceLocation, Integer> e : RF_MAP.entrySet()) {
            JsonObject o = new JsonObject();
            o.addProperty("id", e.getKey().toString());
            o.addProperty("rf", e.getValue());
            entries.add(o);
        }
        root.add("entries", entries);
        Files.writeString(file, GSON.toJson(root));
    }

    /** RF cost per 1 unit of matter. Returns fallback for unknown/custom matter. */
    public static int getRfForMatter(ResourceLocation matterId) {
        return RF_MAP.getOrDefault(matterId, fallbackRf);
    }

    public static int getFallbackRf() {
        return fallbackRf;
    }

    /** Whether any data is loaded (e.g. for GUI fallback display). */
    public static boolean isLoaded() {
        return !RF_MAP.isEmpty() || ReplicationRegistry.MATTER_TYPES_REGISTRY != null;
    }
}
