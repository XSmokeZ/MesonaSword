package me.mesona.mesona_sword.network;

import io.netty.buffer.ByteBuf;
import me.mesona.mesona_sword.MesonaSword;
import me.mesona.mesona_sword.listener.BellMarkRenderer;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public record BellMarkSyncPacket(Action action, List<Integer> entityIds) implements CustomPacketPayload {

    public enum Action {
        ADD, REMOVE, SYNC
    }

    public static final Type<BellMarkSyncPacket> TYPE =
        new Type<>(ResourceLocation.fromNamespaceAndPath(MesonaSword.MODID, "bell_mark_sync"));

    public static final StreamCodec<ByteBuf, BellMarkSyncPacket> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.INT.map(
            i -> Action.values()[i],
            Action::ordinal
        ), BellMarkSyncPacket::action,
        ByteBufCodecs.collection(ArrayList::new, ByteBufCodecs.INT), BellMarkSyncPacket::entityIds,
        BellMarkSyncPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(BellMarkSyncPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            switch (packet.action()) {
                case ADD -> {
                    for (int id : packet.entityIds()) {
                        BellMarkRenderer.addMark(id);
                    }
                }
                case REMOVE -> {
                    for (int id : packet.entityIds()) {
                        BellMarkRenderer.removeMark(id);
                    }
                }
                case SYNC -> BellMarkRenderer.syncMarks(new HashSet<>(packet.entityIds()));
            }
        });
    }
}