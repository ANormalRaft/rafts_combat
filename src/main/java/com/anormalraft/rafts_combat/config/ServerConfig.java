package com.anormalraft.rafts_combat.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class ServerConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.DoubleValue KNOCKBACK_THRESHOLD = BUILDER
            .comment("The % between 0 and 1 of the current charge threshold under which knockback shall not be applied. If 0.6 is specified, then if the charge meter is under 60% no knockback shall be applied. Default 0.6")
            .defineInRange("knockback_threshold", 0.6,0, 1);

    //Has to be at the end
    public static final ModConfigSpec SPEC = BUILDER.build();
}
