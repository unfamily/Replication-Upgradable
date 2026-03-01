package net.unfamily.rep_up;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Rep Up mod configuration.
 * Registered in RepUp: modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
 */
public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    private static final ModConfigSpec.Builder IMPROVED_REPLICATOR = BUILDER.comment("Improved Replicator settings").push("improved_replicator");

    /** Max progress for rep_imp; in-game bar shows double. Half of default Replication replicator (100). */
    public static final ModConfigSpec.IntValue REP_IMP_MAX_PROGRESS = IMPROVED_REPLICATOR
            .comment("Rep Imp replicator max progress (duration). In-game progress bar shows double. Default 50 = half of classic replicator.")
            .defineInRange("maxProgress", 50, 1, Integer.MAX_VALUE);

    public static final ModConfigSpec.IntValue REP_IMP_POWER_TICK = IMPROVED_REPLICATOR
            .comment("Energy consumed per tick while replicating. Same as Replication default.")
            .defineInRange("powerTick", 80, 1, Integer.MAX_VALUE);

    static {
        IMPROVED_REPLICATOR.pop();
    }

    private static final ModConfigSpec.Builder MATTER_TANKS = BUILDER.comment("Matter tank capacity multipliers (relative to base Replication matter tank).").push("matter_tanks");

    /** Deep Matter Tank capacity = base capacity * this. Default 2. */
    public static final ModConfigSpec.IntValue DEEP_MATTER_TANK_MULTIPLIER = MATTER_TANKS
            .comment("Deep Matter Tank capacity multiplier. Capacity = Replication base capacity × this. Default 2.")
            .defineInRange("deepMultiplier", 2, 1, 100);

    /** Abyssal Matter Tank capacity = base capacity * this. Default 4. */
    public static final ModConfigSpec.IntValue ABYSSAL_MATTER_TANK_MULTIPLIER = MATTER_TANKS
            .comment("Abyssal Matter Tank capacity multiplier. Capacity = Replication base capacity × this. Default 4.")
            .defineInRange("abyssalMultiplier", 4, 1, 100);

    static {
        MATTER_TANKS.pop();
    }

    private static final ModConfigSpec.Builder DEV = BUILDER.comment("Dev / external scripts. Base path for rep_up external data (e.g. energy_mat.json).").push("dev");

    /** Base directory for rep_up external scripts; energy_mat uses <path>/energy_mat.json. Reusable for other features later. */
    public static final ModConfigSpec.ConfigValue<String> EXTERNAL_SCRIPTS_PATH = DEV
            .comment("Directory for rep_up external scripts. Energy Materializer: <this>/energy_mat.json. Default: kubejs/external_scripts/rep_up")
            .define("externalScriptsPath", "kubejs/external_scripts/rep_up");

    static {
        DEV.pop();
    }

    private static final ModConfigSpec.Builder ENERGY_MATERIALIZER = BUILDER.comment("Energy Materializer (energy_mat): RF to matter conversion.").push("energy_materializer");

    /** Energy storage capacity (RF). Default very high. */
    public static final ModConfigSpec.IntValue ENERGY_MAT_ENERGY_CAPACITY = ENERGY_MATERIALIZER
            .comment("Energy storage capacity (RF) for the Energy Materializer. Default: 100000000.")
            .defineInRange("energyCapacity", 100000000, 1, Integer.MAX_VALUE);

    /** Max production multiplier (1 = base rate; higher = more RF consumed and more matter per tick). Default 100. */
    public static final ModConfigSpec.IntValue ENERGY_MAT_MAX_PRODUCTION = ENERGY_MATERIALIZER
            .comment("Maximum production multiplier for the Energy Materializer (1–100). Production multiplies both energy cost and matter output per tick.")
            .defineInRange("maxProduction", 100, 1, 100);

    static {
        ENERGY_MATERIALIZER.pop();
    }

    public static final ModConfigSpec SPEC = BUILDER.build();
}
