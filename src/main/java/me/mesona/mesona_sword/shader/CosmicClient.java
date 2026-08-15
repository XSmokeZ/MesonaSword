package me.mesona.mesona_sword.shader;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.TextureAtlasStitchedEvent;

import static me.mesona.mesona_sword.MesonaSword.MODID;

@EventBusSubscriber(modid = MODID, value = Dist.CLIENT)
public class CosmicClient {

    public static boolean inventoryRender = false;

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

    @SubscribeEvent
    public static void onRenderFramePre(RenderFrameEvent.Pre event) {
        // Only render here if Iris is not active
        if (!IrisCompat.isIrisActive()) {
            CosmicQueue.renderAll();
        }
    }

    @SubscribeEvent
    public static void onRenderFramePost(RenderFrameEvent.Post event) {
        // Only render here if Iris is not active
        if (!IrisCompat.isIrisActive()) {
            CosmicQueue.renderAll();
        }
    }

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        // When Iris is active, render at AFTER_LEVEL stage
        // This ensures the cosmic layer renders after Iris has finished its pipeline
        if (IrisCompat.isIrisActive() && event.getStage() == RenderLevelStageEvent.Stage.AFTER_LEVEL) {
            CosmicQueue.renderAll();
        }
    }
}