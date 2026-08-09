package me.mesona.mesona_sword.client;

import me.mesona.mesona_sword.MesonaSword;
import me.mesona.mesona_sword.api.client.render.CosmicRenderQueue;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderFrameEvent;

@EventBusSubscriber(modid = MesonaSword.MODID, value = Dist.CLIENT)
public class MesonaSwordGameClient {

    @SubscribeEvent
    public static void onRenderFrame(RenderFrameEvent.Post event) {
        CosmicRenderQueue.renderAll();
    }
}