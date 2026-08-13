package me.mesona.mesona_sword.listener;

import me.mesona.mesona_sword.MesonaSword;
import me.mesona.mesona_sword.network.SweepAttackPacket;
import me.mesona.mesona_sword.register.GrassSwordItem;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = MesonaSword.MODID, value = Dist.CLIENT)
public class SweepHandler {

    @SubscribeEvent
    public static void onLeftClickEmpty(PlayerInteractEvent.LeftClickEmpty event) {
        Player player = event.getEntity();
        if (!player.level().isClientSide) return;

        // 客户端只检测是否主手持草剑，然后发包
        // 所有权限验证在服务端处理
        if (player.getMainHandItem().getItem() instanceof GrassSwordItem) {
            PacketDistributor.sendToServer(new SweepAttackPacket());
        }
    }

    @SubscribeEvent
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        Player player = event.getEntity();
        if (!player.level().isClientSide) return;

        // 客户端只检测是否主手持草剑，然后发包
        // 所有权限验证在服务端处理
        if (player.getMainHandItem().getItem() instanceof GrassSwordItem) {
            PacketDistributor.sendToServer(new SweepAttackPacket());
        }
    }
}