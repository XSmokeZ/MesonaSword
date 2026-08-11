package me.mesona.mesona_sword.network;

import io.netty.buffer.ByteBuf;
import me.mesona.mesona_sword.GrassSwordItem;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SweepAttackPacket() implements CustomPacketPayload {

    public static final Type<SweepAttackPacket> TYPE =
        new Type<>(ResourceLocation.fromNamespaceAndPath("mesona_sword", "sweep_attack"));

    public static final StreamCodec<ByteBuf, SweepAttackPacket> STREAM_CODEC =
        StreamCodec.unit(new SweepAttackPacket());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SweepAttackPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;

            // 服务端验证：必须主手持草剑
            if (!(player.getMainHandItem().getItem() instanceof GrassSwordItem sword)) return;

            // 服务端验证：必须是横扫模式
            if (sword.getMode(player.getMainHandItem()) != GrassSwordItem.SwordMode.SWEEP) return;

//            // 服务端验证：检查冷却，防止连发
//            if (player.getAttackStrengthScale(0.5F) < 0.9F) return;

            // 执行横扫
            GrassSwordItem.sweepDamage(player.level(), player, player);
        });
    }
}