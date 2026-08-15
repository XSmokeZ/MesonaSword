package me.mesona.mesona_sword.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class MesonaConfig {

    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.BooleanValue STARFIELD_ENABLED;
    public static final ModConfigSpec.BooleanValue QUICK_DAMAGE;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        STARFIELD_ENABLED = builder
                .comment("Enable or disable the starry sky (cosmic) effect on the sword")
                .define("starfieldEnabled", false);
        QUICK_DAMAGE = builder
                .comment("Enable or disable the quick damage mode of the sword")
                .define("quickDamage", true);
        SPEC = builder.build();
    }
}