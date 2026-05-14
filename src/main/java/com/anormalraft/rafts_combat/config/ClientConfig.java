package com.anormalraft.rafts_combat.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class ClientConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.IntValue MAX_ALPHA = BUILDER
            .comment("The alpha value at which the edge of the range indicator shall be at 100% charge. Between 0 (transparent) and 255 (opaque). \nDefault 80")
            .defineInRange("max_alpha", 80, 0, 255);

    public static final ModConfigSpec.DoubleValue QUAD_HEIGHT = BUILDER
            .comment("Specifies a ratio that will be used to determine the height of the indicator. Does not make the hit detection taller, it is only a visual effect. \nDefault 0.005")
            .defineInRange("quad_height", 0.005, 0, 1);

    //Has to be at the end
    public static final ModConfigSpec SPEC = BUILDER.build();
}
