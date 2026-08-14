package me.mesona.mesona_sword.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class MesonaConfig {

    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.BooleanValue STARFIELD_ENABLED;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        STARFIELD_ENABLED = builder
                .comment("Enable or disable the starry sky (cosmic) effect on the sword")
                .define("starfieldEnabled", false);
        SPEC = builder.build();
    }
}