package me.mesona.mesona_sword.network;

import io.netty.buffer.ByteBuf;
import me.mesona.mesona_sword.MesonaSword;
import me.mesona.mesona_sword.client.effect.SweepEffectRenderer;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;

public record SweepEffectBatchPacket(List<EffectData> effects) implements CustomPacketPayload {

    public record EffectData(double x, double y, double z, float yaw, float scale) {
        public static final StreamCodec<ByteBuf, EffectData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.DOUBLE, EffectData::x,
            ByteBufCodecs.DOUBLE, EffectData::y,
            ByteBufCodecs.DOUBLE, EffectData::z,
            ByteBufCodecs.FLOAT, EffectData::yaw,
            ByteBufCodecs.FLOAT, EffectData::scale,
            EffectData::new
        );
    }

    public static final Type<SweepEffectBatchPacket> TYPE =
        new Type<>(ResourceLocation.fromNamespaceAndPath(MesonaSword.MODID, "sweep_effect_batch"));

    public static final StreamCodec<ByteBuf, SweepEffectBatchPacket> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.collection(ArrayList::new, EffectData.STREAM_CODEC), SweepEffectBatchPacket::effects,
        SweepEffectBatchPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SweepEffectBatchPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            for (EffectData data : packet.effects()) {
                SweepEffectRenderer.addEffect(
                    new Vec3(data.x(), data.y(), data.z()),
                    data.yaw(),
                    data.scale()
                );
            }
        });
    }
}