package me.mesona.mesona_sword.shader;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.TextureAtlasStitchedEvent;

import static me.mesona.mesona_sword.MesonaSword.MODID;

public class CosmicClientModBus {

    @SubscribeEvent
    public static void registerGeometryLoaders(ModelEvent.RegisterGeometryLoaders event) {
        event.register(CosmicLoader.ID, CosmicLoader.INSTANCE);
    }

    @SubscribeEvent
    public static void onTextureAtlasStitched(TextureAtlasStitchedEvent event) {
        if (!event.getAtlas().location().getPath().equals("textures/atlas/blocks.png")) return;

        for (int i = 0; i < 10; i++) {
            ResourceLocation loc = ResourceLocation.fromNamespaceAndPath(MODID, "misc/cosmic/cosmic_" + i);
            TextureAtlasSprite sprite = event.getAtlas().getSprite(loc);

            if (sprite == null) continue;
            CosmicShaders.COSMIC_UVS[i * 4] = sprite.getU0();
            CosmicShaders.COSMIC_UVS[i * 4 + 1] = sprite.getV0();
            CosmicShaders.COSMIC_UVS[i * 4 + 2] = sprite.getU1();
            CosmicShaders.COSMIC_UVS[i * 4 + 3] = sprite.getV1();
            CosmicShaders.COSMIC_SPRITES[i] = sprite;
        }
    }
}