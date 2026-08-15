package me.mesona.mesona_sword.register;

import me.mesona.mesona_sword.config.MesonaConfig;
import me.mesona.mesona_sword.config.MesonaConfigScreen;
import me.mesona.mesona_sword.network.BellMarkSyncPacket;
import me.mesona.mesona_sword.network.SweepEffectBatchPacket;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

public class Registry {

    public static void init(IEventBus modEventBus, ModContainer modContainer) {
        GrassSwordItem.ITEMS.register(modEventBus);
        ModDataComponent.COMPONENTS.register(modEventBus);
        BellMarkAttachment.ATTACHMENT_TYPES.register(modEventBus);
        modEventBus.addListener(Registry::registerPackets);

        modContainer.registerConfig(ModConfig.Type.CLIENT, MesonaConfig.SPEC);
        modContainer.registerExtensionPoint(IConfigScreenFactory.class, MesonaConfigScreen.createFactory());
    }

    private static void registerPackets(RegisterPayloadHandlersEvent event) {
        event.registrar("0.4")
                .playToClient(SweepEffectBatchPacket.TYPE, SweepEffectBatchPacket.STREAM_CODEC, SweepEffectBatchPacket::handle)
                .playToClient(BellMarkSyncPacket.TYPE, BellMarkSyncPacket.STREAM_CODEC, BellMarkSyncPacket::handle);
    }
}