package me.mesona.mesona_sword.shader;

import net.minecraft.world.item.ItemDisplayContext;

public class IrisCompat {

    private static final boolean IRIS_ENABLED;
    private static final boolean IRIS_API_V0_AVAILABLE;

    static {
        boolean iris = false;
        boolean apiV0 = false;
        try {
            // Try Iris internal API first (used by Iris itself)
            Class<?> irisClass = Class.forName("net.irisshaders.iris.Iris");
            iris = (boolean) irisClass.getMethod("isPackInUseQuick").invoke(null);
        } catch (Throwable ignored) {
            try {
                // Fallback to IrisApi v0
                Class<?> irisApi = Class.forName("net.irisshaders.iris.api.v0.IrisApi");
                Object inst = irisApi.getMethod("getInstance").invoke(null);
                iris = (boolean) irisApi.getMethod("isShaderPackInUse").invoke(inst);
                apiV0 = true;
            } catch (Throwable ignored2) {
            }
        }
        IRIS_ENABLED = iris;
        IRIS_API_V0_AVAILABLE = apiV0;
    }

    public static boolean shouldDefer(ItemDisplayContext ctx) {
        if (!IRIS_ENABLED) return false;
        // Defer all non-GUI renders when Iris is active
        return switch (ctx) {
            case FIRST_PERSON_LEFT_HAND, FIRST_PERSON_RIGHT_HAND,
                 THIRD_PERSON_LEFT_HAND, THIRD_PERSON_RIGHT_HAND,
                 GROUND, FIXED -> true;
            default -> false;
        };
    }

    public static boolean isShaderActive() {
        return IRIS_ENABLED;
    }

    public static boolean isIrisActive() {
        return IRIS_ENABLED;
    }

    public static boolean isApiV0Available() {
        return IRIS_API_V0_AVAILABLE;
    }
}