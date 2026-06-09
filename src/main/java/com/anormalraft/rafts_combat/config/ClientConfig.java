package com.anormalraft.rafts_combat.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class ClientConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    //Verbose defaults are already provided for IntValue and DoubleValue
    public static final ModConfigSpec.IntValue MAX_ALPHA = BUILDER
            .comment("The alpha value at which the edge of the range indicator shall be at 100% charge. Between 0 (transparent) and 255 (opaque)")
            .defineInRange("max_alpha", 100, 0, 255);

    public static final ModConfigSpec.IntValue BAR_HEIGHT = BUILDER
            .comment("Half of the size in GUI units used to determine the height of the range indicator bar. Does not increade hit detection, it's only a visual effect")
            .defineInRange("bar_height", 1, 0, 100);

    public static final ModConfigSpec.BooleanValue CROSSHAIR_COLOR = BUILDER
            .comment("Should the default crosshair also change color when at least one entity is within range? \nDefault: true")
            .define("crosshair_color", true);

    //Has to be at the end
    public static final ModConfigSpec SPEC = BUILDER.build();
}
