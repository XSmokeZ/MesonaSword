package me.mesona.mesona_sword.shader;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

public class CosmicClient {

    public static boolean inventoryRender = false;

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