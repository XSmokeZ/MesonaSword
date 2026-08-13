package me.mesona.mesona_sword.shader;

import net.minecraft.world.item.ItemDisplayContext;

public class IrisCompat {

    private static final boolean ENABLED;

    static {
        boolean v;
        try {
            Class<?> api = Class.forName("net.irisshaders.iris.api.v0.IrisApi");
            Object inst = api.getMethod("getInstance").invoke(null);
            v = (boolean) api.getMethod("isShaderPackInUse").invoke(inst);
        } catch (Throwable ignored) {
            v = false;
        }
        ENABLED = v;
    }

    public static boolean shouldDefer(ItemDisplayContext ctx) {
        if (!ENABLED) return false;
        return switch (ctx) {
            case FIRST_PERSON_LEFT_HAND, FIRST_PERSON_RIGHT_HAND,
                 THIRD_PERSON_LEFT_HAND, THIRD_PERSON_RIGHT_HAND -> true;
            default -> false;
        };
    }
}