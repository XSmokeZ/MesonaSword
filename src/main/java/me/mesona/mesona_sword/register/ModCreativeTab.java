package me.mesona.mesona_sword.register;

import me.mesona.mesona_sword.MesonaSword;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModCreativeTab {
    // 在 ITEMS 注册器下面加
public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
        DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MesonaSword.MODID);

public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MESONA_TAB = CREATIVE_MODE_TABS.register(
        "mesona_tab",
        () -> CreativeModeTab.builder()
                .title(Component.translatable("itemGroup.mesona_sword"))
                .icon(() -> new ItemStack(ModItem.MESONA_SWORD.get()))
                .displayItems((params, output) -> {
                    output.accept(ModItem.MESONA_SWORD.get());
                })
                .build()
);
}
