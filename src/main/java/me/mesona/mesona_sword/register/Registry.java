package me.mesona.mesona_sword.register;

import me.mesona.mesona_sword.network.SweepAttackPacket;
import me.mesona.mesona_sword.network.SweepEffectBatchPacket;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

public class Registry {

    public static void init(IEventBus modEventBus) {
        GrassSwordItem.ITEMS.register(modEventBus);
        ModDataComponent.COMPONENTS.register(modEventBus);
        modEventBus.addListener(Registry::registerPackets);
    }

    private static void registerPackets(RegisterPayloadHandlersEvent event) {
        event.registrar("0.2")
                .playToServer(SweepAttackPacket.TYPE, SweepAttackPacket.STREAM_CODEC, SweepAttackPacket::handle)
                .playToClient(SweepEffectBatchPacket.TYPE, SweepEffectBatchPacket.STREAM_CODEC, SweepEffectBatchPacket::handle);
    }
}
