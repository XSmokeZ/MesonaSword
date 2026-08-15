package me.mesona.mesona_sword.shader;

import net.minecraft.world.item.ItemDisplayContext;

public class IrisCompat {

    private static final boolean IRIS_AVAILABLE;
    private static final boolean IRIS_API_V0_AVAILABLE;

    static {
        boolean available = false;
        boolean apiV0 = false;
        try {
            // Try Iris internal API first (used by Iris itself)
            Class.forName("net.irisshaders.iris.Iris");
            available = true;
        } catch (Throwable ignored) {
            try {
                // Fallback to IrisApi v0
                Class.forName("net.irisshaders.iris.api.v0.IrisApi");
                available = true;
                apiV0 = true;
            } catch (Throwable ignored2) {
                System.out.println("MesonaSword: Iris API v0 not found");
            }
        }
        IRIS_AVAILABLE = available;
        IRIS_API_V0_AVAILABLE = apiV0;
    }

    public static boolean isIrisActive() {
        if (!IRIS_AVAILABLE) return false;
        try {
            // Try Iris internal API first
            Class<?> irisClass = Class.forName("net.irisshaders.iris.Iris");
            return (boolean) irisClass.getMethod("isPackInUseQuick").invoke(null);
        } catch (Throwable ignored) {
            try {
                // Fallback to IrisApi v0
                Class<?> irisApi = Class.forName("net.irisshaders.iris.api.v0.IrisApi");
                Object inst = irisApi.getMethod("getInstance").invoke(null);
                return (boolean) irisApi.getMethod("isShaderPackInUse").invoke(inst);
            } catch (Throwable ignored2) {
                return false;
            }
        }
    }

    public static boolean shouldDefer(ItemDisplayContext ctx) {
        if (!isIrisActive()) return false;
        // Defer all non-GUI renders when Iris is active
        return switch (ctx) {
            case FIRST_PERSON_LEFT_HAND, FIRST_PERSON_RIGHT_HAND,
                 THIRD_PERSON_LEFT_HAND, THIRD_PERSON_RIGHT_HAND,
                 GROUND, FIXED -> true;
            default -> false;
        };
    }
}