package me.mesona.mesona_sword.shader;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;

public class SodiumCompat {

    private static final boolean SODIUM_AVAILABLE;
    private static Object spriteUtilInstance;

    static {
        boolean available = false;
        try {
            Class<?> spriteUtilClass = Class.forName("net.caffeinemc.mods.sodium.api.texture.SpriteUtil");
            spriteUtilInstance = spriteUtilClass.getField("INSTANCE").get(null);
            available = true;
        } catch (Throwable ignored) {
            System.out.println("MesonaSword: Sodium not found, animation tracking unavailable");
        }
        SODIUM_AVAILABLE = available;
    }

    public static boolean isSodiumAvailable() {
        return SODIUM_AVAILABLE;
    }

    public static void markSpriteActive(TextureAtlasSprite sprite) {
        if (!SODIUM_AVAILABLE || sprite == null) return;
        try {
            spriteUtilInstance.getClass()
                    .getMethod("markSpriteActive", TextureAtlasSprite.class)
                    .invoke(spriteUtilInstance, sprite);
        } catch (Throwable ignored) {
        }
    }

    public static void markSpritesActive(TextureAtlasSprite[] sprites) {
        if (!SODIUM_AVAILABLE || sprites == null) return;
        for (TextureAtlasSprite sprite : sprites) {
            markSpriteActive(sprite);
        }
    }
}