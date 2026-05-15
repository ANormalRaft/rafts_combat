package com.anormalraft.rafts_combat.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class ServerConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    //Verbose defaults are already provided for IntValue and DoubleValue
    public static final ModConfigSpec.DoubleValue KNOCKBACK_THRESHOLD = BUILDER
            .comment("The % between 0 and 1 of the current charge threshold under which knockback shall not be applied. If 0.6 is specified, then if the charge meter is under 60% no knockback shall be applied")
            .defineInRange("knockback_threshold", 0.6,0, 1);

    public static final ModConfigSpec.DoubleValue WIDTH_RATIO = BUILDER
            .comment("Rato which determines how wide the default range indicator shall be at max charge")
            .defineInRange("width_ratio", 0.4, 0, 1);

    public static final ModConfigSpec.ConfigValue<String> CUSTOM_RATIOS = BUILDER
            .comment("String that stores information about what width ratios are applied to which items (either RegEx or strictly formatted list). Follow the syntax of the default value closely. \nDefault:  \"{\\\"0.2\\\": \\\"trident$\\\", \\\"0.5\\\": \\\"[minecraft:mace, minecraft:netherite_axe]\\\"}\"")
            .define("bindings", "{\"0.2\": \"trident$\", \"0.5\": \"[minecraft:mace, minecraft:netherite_axe]\"}");

    //Has to be at the end
    public static final ModConfigSpec SPEC = BUILDER.build();
}
