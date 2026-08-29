package com.anormalraft.rafts_combat.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class ClientConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    //Verbose defaults are already provided for IntValue and DoubleValue
    public static final ModConfigSpec.IntValue MAX_ALPHA = BUILDER
            .comment("The alpha value at which the edges of the range indicator bars touching the fullness indicators shall be at 100% charge. \nBetween 0 (transparent) and 255 (opaque)")
            .defineInRange("max_alpha", 100, 0, 255);

    public static final ModConfigSpec.IntValue MIN_FULLNESS_INDICATORS_ALPHA = BUILDER
            .comment("The minimum alpha value at which the fullness indicators will be displayed at 0%  charge. \nBetween 0 (transparent) and 255 (opaque)")
            .defineInRange("min_fullness_indicators_alpha", 30, 0, 255);

    public static final ModConfigSpec.IntValue BAR_HEIGHT = BUILDER
            .comment("Half of the size in GUI units used to determine the height of the range indicator bar. \nDoes not increase hit detection, it's only a visual effect")
            .defineInRange("bar_height", 1, 0, 100);

    public static final ModConfigSpec.BooleanValue CROSSHAIR_COLOR = BUILDER
            .comment("Should the default crosshair also change color when at least one entity is within range? \nDefault: true")
            .define("crosshair_color", true);

    public static final ModConfigSpec.ConfigValue<String> NONE_IN_RANGE_COLOR = BUILDER.comment("Color hexadecimal of the GUI elements when no mobs are detected within range. \nDefault: \\\"FFFFFF\\\"").define("none_in_range_color", "FFFFFF");

    public static final ModConfigSpec.ConfigValue<String> MOB_IN_RANGE_COLOR = BUILDER.comment("Color hexadecimal of the GUI elements when mobs are detected within range. \nDefault: \\\"FF0000\\\"").define("mob_in_range_color", "FF0000");

    //Has to be at the end
    public static final ModConfigSpec SPEC = BUILDER.build();
}
