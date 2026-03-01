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

    public static final ModConfigSpec SPEC = BUILDER.build();
}
