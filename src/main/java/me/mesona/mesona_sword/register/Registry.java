package me.mesona.mesona_sword.register;

import me.mesona.mesona_sword.config.MesonaConfig;
import me.mesona.mesona_sword.config.MesonaConfigScreen;
import me.mesona.mesona_sword.listener.BellMarkRenderer;
import me.mesona.mesona_sword.listener.EventHandle;
import me.mesona.mesona_sword.listener.SweepEffectRenderer;
import me.mesona.mesona_sword.listener.TooltipHandler;
import me.mesona.mesona_sword.network.BellMarkSyncPacket;
import me.mesona.mesona_sword.network.SweepEffectBatchPacket;
import me.mesona.mesona_sword.shader.CosmicClient;
import me.mesona.mesona_sword.shader.CosmicClientModBus;
import me.mesona.mesona_sword.shader.CosmicShaders;
import me.mesona.mesona_sword.util.BellMarkUtil;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

public class Registry {

    public static void init(IEventBus modEventBus, ModContainer modContainer) {
        ModItem.ITEMS.register(modEventBus);
        ModCreativeTab.CREATIVE_MODE_TABS.register(modEventBus);
        ModDataComponent.COMPONENTS.register(modEventBus);
        BellMarkUtil.ATTACHMENT_TYPES.register(modEventBus);
        modEventBus.addListener(Registry::registerPackets);

        modContainer.registerConfig(ModConfig.Type.CLIENT, MesonaConfig.SPEC);
        modContainer.registerExtensionPoint(IConfigScreenFactory.class, MesonaConfigScreen.createFactory());

        NeoForge.EVENT_BUS.register(EventHandle.class);

        if (FMLEnvironment.dist == Dist.CLIENT) {
            modEventBus.addListener(CosmicShaders::onRegisterShaders);
            modEventBus.addListener(CosmicClientModBus::registerGeometryLoaders);
            modEventBus.addListener(CosmicClientModBus::onTextureAtlasStitched);

            NeoForge.EVENT_BUS.register(CosmicClient.class);
            NeoForge.EVENT_BUS.register(TooltipHandler.class);
            NeoForge.EVENT_BUS.register(SweepEffectRenderer.class);
            NeoForge.EVENT_BUS.register(BellMarkRenderer.class);
        }
    }

    private static void registerPackets(RegisterPayloadHandlersEvent event) {
        event.registrar("0.4")
                .playToClient(SweepEffectBatchPacket.TYPE, SweepEffectBatchPacket.STREAM_CODEC, SweepEffectBatchPacket::handle)
                .playToClient(BellMarkSyncPacket.TYPE, BellMarkSyncPacket.STREAM_CODEC, BellMarkSyncPacket::handle);
    }
}