package me.mesona.mesona_sword.listener;


import me.mesona.mesona_sword.register.GrassSwordItem;
import me.mesona.mesona_sword.register.TextUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import java.util.List;

import static me.mesona.mesona_sword.MesonaSword.MODID;

@EventBusSubscriber(modid = MODID, value = Dist.CLIENT)
public class TooltipHandler {

    @SubscribeEvent
    public static void onTooltipEvent(ItemTooltipEvent event) {

        ItemStack stack = event.getItemStack();

        if (!(stack.getItem() instanceof GrassSwordItem)) return;

        GrassSwordItem.SwordMode swordMode = GrassSwordItem.getMode(stack);

        List<Component> lines = event.getToolTip();
        for (int i = 0; i < lines.size(); i++) {
            Component line = lines.get(i);
            if (line.contains(Component.translatable("attribute.name.generic.attack_damage"))) {
                switch (swordMode) {
                    case NORMAL -> {
                        lines.set(i, Component.literal(I18n.get("tooltip.mesona_sword.big_number")).withStyle(ChatFormatting.AQUA).append(Component.literal(I18n.get("tooltip.mesona_sword.attack_damage.desc")).withStyle(ChatFormatting.DARK_GREEN)));
                    }
                    case SWEEP -> {
                        lines.set(i, Component.literal(I18n.get("tooltip.mesona_sword.void_damage")).withStyle(ChatFormatting.DARK_RED).append(Component.literal(I18n.get("tooltip.mesona_sword.attack_damage.desc")).withStyle(ChatFormatting.DARK_GREEN)));
                    }
                    case EXECUTE -> {
                        lines.set(i, Component.literal(TextUtil.makeFabulous(I18n.get("tooltip.mesona_sword.infinity"))).append(Component.literal(I18n.get("tooltip.mesona_sword.attack_damage.desc")).withStyle(ChatFormatting.DARK_GREEN)));
                    }
                }
            }
        }
    }
}
