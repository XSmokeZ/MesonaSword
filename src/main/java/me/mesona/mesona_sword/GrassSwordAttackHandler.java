package me.mesona.mesona_sword;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@EventBusSubscriber(modid = MesonaSword.MODID)
public class GrassSwordAttackHandler {

    @SubscribeEvent
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        Player player = event.getEntity();
        if (player.level().isClientSide) return;

        ItemStack stack = player.getMainHandItem();
        if (!(stack.getItem() instanceof GrassSwordItem sword)) return;

        // 只处理横扫模式
        if (sword.getMode(stack) != GrassSwordItem.SwordMode.SWEEP) return;

        GrassSwordItem.sweepDamage(player.level(), player, player);
    }
}